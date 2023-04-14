package ermes.facebook;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ermes.util.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.FacebookClient.DebugTokenInfo;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonArray;
import com.restfb.json.JsonObject;
import com.restfb.json.JsonValue;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import com.restfb.types.User;
import ermes.facebook.FacebookService.FacebookServicePermission.ServicePermission;
import ermes.response.ErmesResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.facebook.FacebookAuthorizationResponse;
import ermes.util.MediaUtils;

@Service
public class FacebookConnector implements FacebookService {

    public FacebookConnector() {
    }

    @Override
    public String getAuthUrl(String key, String secret, String callbackUrl, ScopeBuilder scopeBuilder) {
        return new DefaultFacebookClient(Version.LATEST).getLoginDialogUrl(key, callbackUrl, scopeBuilder);
    }

    @Override
    public void createConnection(String key, String secret, String callbackUrl, String code) {
        this.accessToken = new DefaultFacebookClient(Version.LATEST)
                .obtainUserAccessToken(key, secret, callbackUrl, code);

        // Get extended token
        this.accessToken = extendAccessToken(key, secret, this.accessToken);
        facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.LATEST);
    }

    @Override
    public void createConnection(String token) {
        // Build the access token from a given value
        String queryString = ACCESS_TOKEN_QUERY_STRING + token;
        AccessToken accessToken = AccessToken.fromQueryString(queryString);

        // Build connection
        this.accessToken = accessToken;
        facebookClient = new DefaultFacebookClient(this.accessToken.getAccessToken(), Version.LATEST);
    }

    @Override
    public AccessToken extendAccessToken(String key, String secret, AccessToken accessToken) {
        // Extend the access token
        return new DefaultFacebookClient(Version.LATEST)
                .obtainExtendedAccessToken(key, secret, accessToken.getAccessToken());
    }

    @Override
    public boolean verifyConnection(String key) {
        return !isTokenExpiredOrNotValid() && verifyApplicationInfo(key);
    }

    @Override
    public boolean isTokenExpiredOrNotValid() {
        if (accessToken == null) {
            return true;
        }

        DebugTokenInfo tokenInfo = facebookClient.debugToken(accessToken.getAccessToken());

        return !tokenInfo.isValid();
    }

    @Override
    public boolean isTokenGiven(String token) {
        return StringUtils.isNotEmpty(token);
    }

    @Override
    public boolean verifyApplicationInfo(String key) {
        DebugTokenInfo tokenInfo = facebookClient.debugToken(accessToken.getAccessToken());
        String applicationKey = tokenInfo.getAppId();

        return applicationKey.equals(key);
    }

    @Override
    public boolean verifyCode(String code) {
        return StringUtils.isNotEmpty(code);
    }

    @Override
    public boolean verifyPermissionsDenied(String denied) {
        return StringUtils.isNotEmpty(denied);
    }

    @Override
    public ErmesResponse<FacebookAuthorizationResponse> authorization() {
        ErmesResponse<FacebookAuthorizationResponse> response = new ErmesResponse<>();

        // Get the debug token info
        DebugTokenInfo tokenInfo = facebookClient.debugToken(accessToken.getAccessToken());

        // Build Facebook response
        FacebookAuthorizationResponse facebookAccessTokenResponse = new FacebookAuthorizationResponse();
        facebookAccessTokenResponse.setAccessToken(accessToken.getAccessToken());
        facebookAccessTokenResponse.setExpires(tokenInfo.getExpiresAt().toString());
        facebookAccessTokenResponse.setApplicationKey(tokenInfo.getAppId());
        facebookAccessTokenResponse.setApplicationName(tokenInfo.getApplication());
        facebookAccessTokenResponse.setUserId(tokenInfo.getUserId());

        // Build the response
        return response.success(ErmesResponse.CODE, ErmesResponse.SUCCESS_MESSAGE)
                .setData(facebookAccessTokenResponse);
    }

    // Return the name of a service
    @Override
    public String getServiceName(StringBuffer path, String key) {
        Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return path.substring(matcher.start());
        }

        return null;
    }

    // Return user's representation
    @Override
    public User getUser(String fields) {
        return facebookClient.fetchObject("/me", User.class, Parameter.with("fields", fields));
    }

    // Publish a status on a user's page
    @Override
    public ErmesResponse<PublishResponse> postStatusOnPage(String pageName, String statusText) {
        ErmesResponse<PublishResponse> response = new ErmesResponse<>();

        // Check input parameters
        String errorMessage = PublishResponse.FAIL_MESSAGE;
        if (StringUtils.isEmpty(pageName) || StringUtils.isEmpty(statusText)) {
            return response.error(ErmesResponse.CODE, errorMessage);
        }

        // Get the type of the status because Facebook didn't handle link by itself
        String statusType = getStatusType(statusText);

        // Get user's pages
        Connection<Page> result = getPages();
        try {
            // Find the page with name equals to pageName
            Page page = findPageByName(result, pageName);
            if (page != null) {
                // Get page's access token and id in order to publish on it
                String pageAccessToken = page.getAccessToken();
                String pageId = page.getId();

                // Build the message in case the status contains a link which come with message
                String message = statusText;

                // Build link in case the status' text contains a link
                if (statusType.equals(FacebookService.FACEBOOK_STATUS_LINK)) {
                    statusText = getUrlToPublish(statusText);

                    // Clear the message if the link does not come with a message
                    if (message.equals(statusText)) {
                        message = "";
                    }
                }

                FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
                FacebookType facebookResponse = pageClient.publish(
                        pageId + "/feed",
                        FacebookType.class,
                        // In case it's a link show the preview of the link
                        // In case it's a simple message publish the status with message only
                        Parameter.with(statusType, statusText),
                        // In case the link come with a message
                        Parameter.with("message", message));

                // Build the successful response
                return response.success(ErmesResponse.CODE, PublishResponse.SUCCESS_MESSAGE)
                        .setData(new PublishResponse(getPostUrl(facebookResponse)));
            }
        } catch (FacebookOAuthException e) {
            // Facebook throws this exception in case of duplicate status or in case of invalid link (just two examples)
            e.printStackTrace();

            errorMessage = e.getMessage();
        }

        // Build the error response
        return response.error(ErmesResponse.CODE, errorMessage);
    }

    // Return the type of the status which is going to be published
    private String getStatusType(String statusText) {
        if (UrlUtils.contains(statusText, UrlUtils.HTTPS) || UrlUtils.contains(statusText, UrlUtils.HTTP)) {
            return FacebookService.FACEBOOK_STATUS_LINK;
        }

        return FacebookService.FACEBOOK_STATUS_MESSAGE;
    }

    // Get the url from a status' text
    private String getUrlToPublish(String statusText) {
        // Get the https url
        String url = UrlUtils.getUrlAsContainedSubstring(statusText, UrlUtils.HTTPS);
        if (StringUtils.isNotEmpty(url)) {
            return url;
        }

        // Get the http url if it is not https
        url = UrlUtils.getUrlAsContainedSubstring(statusText, UrlUtils.HTTP);
        if (StringUtils.isNotEmpty(url)) {
            return url;
        }

        return "";
    }

    // Publish an image on a user's page given the url
    @Override
    public ErmesResponse<PublishResponse> postImageOnPage(String pageName, String imageUrl, String statusText) {
        // Check input parameters
        if (StringUtils.isEmpty(imageUrl) || StringUtils.isEmpty(pageName)) {
            return new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, PublishResponse.FAIL_MESSAGE);
        }

        // The message is not needed if it is not specified
        if (statusText == null) {
            statusText = "";
        }

        String errorMessage = PublishResponse.FAIL_MESSAGE;
        try {
            MediaUtils.saveMedia(imageUrl);

            // Get image's path and name
            URL url = new URL(imageUrl);
            String fileName = url.getFile();
            String imageName = fileName.substring(fileName.lastIndexOf("/") + 1);
            String imageFilePath = MediaUtils.PATH + imageName;

            return postImage(pageName, imageFilePath, imageName, statusText);
        } catch (IOException e) {
            // Throw in case of issue with the image
            errorMessage = e.getMessage();
        }

        return new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, errorMessage);
    }

    // Publish an image on a user's page
    private ErmesResponse<PublishResponse> postImage(String pageName, String imageFilePath, String imageName, String statusText) {
        ErmesResponse<PublishResponse> response = new ErmesResponse<>();
        String errorMessage = PublishResponse.FAIL_MESSAGE;
        try {
            // Get user's pages
            Connection<Page> result = getPages();

            // Find image's format
            String imageFormat = MediaUtils.getImageFormat(imageFilePath);

            // Convert image to byte array
            byte[] imageAsBytes = MediaUtils.fetchBytesFromImage(imageFilePath, imageFormat);

            // Find the page by the given name in order to get the id and the access token of the needed page
            Page page = findPageByName(result, pageName);
            if (page != null) {
                // Get page's access token and id in order to publish on it
                String pageAccessToken = page.getAccessToken();
                String pageId = page.getId();

                // Publish the image
                FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
                FacebookType facebookResponse = pageClient.publish(
                        pageId + "/photos",
                        FacebookType.class,
                        BinaryAttachment.with(imageName, imageAsBytes, "image/" + imageFormat),
                        Parameter.with("message", statusText));

                // Build the successful response
                return response.success(ErmesResponse.CODE, PublishResponse.SUCCESS_MESSAGE)
                        .setData(new PublishResponse(getPostUrl(facebookResponse)));
            }
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
        }

        // Build the error response
        return response.error(ErmesResponse.CODE, errorMessage);
    }

    // Publish a video on a user's page given the url
    @Override
    public ErmesResponse<PublishResponse> postVideoOnPage(String pageName, String videoUrl, String statusText) {
        // Check input parameters
        if (StringUtils.isEmpty(videoUrl) || StringUtils.isEmpty(pageName)) {
            return new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, PublishResponse.FAIL_MESSAGE);
        }

        // The message is not needed if it is not specified
        if (statusText == null) {
            statusText = "";
        }

        String errorMessage = PublishResponse.FAIL_MESSAGE;
        try {
            MediaUtils.saveMedia(videoUrl);

            // Get video's path and name
            URL url = new URL(videoUrl);
            String fileName = url.getFile();
            String videoName = fileName.substring(fileName.lastIndexOf("/") + 1);
            String videoFilePath = MediaUtils.PATH + videoName;

            return postVideo(pageName, videoFilePath, videoName, statusText);
        } catch (IOException e) {
            // Throw in case of issue with the video (File not found, No protocol...)
            errorMessage = e.getMessage();
        }

        return new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, errorMessage);
    }

    // Publish an image on a user's page
    private ErmesResponse<PublishResponse> postVideo(String pageName, String videoFilePath, String videoName, String statusText) {
        ErmesResponse<PublishResponse> response = new ErmesResponse<>();
        String errorMessage = PublishResponse.FAIL_MESSAGE;
        try {
            // Get user's pages
            Connection<Page> result = getPages();

            // Convert video to byte array
            byte[] videoAsBytes = MediaUtils.fetchBytesFromVideo(videoFilePath);

            // Find the page by the given name in order to get the id and the access token of the needed page
            Page page = findPageByName(result, pageName);
            if (page != null) {
                // Get page's access token and id in order to publish on it
                String pageAccessToken = page.getAccessToken();
                String pageId = page.getId();

                // Publish the video
                FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.LATEST);
                FacebookType facebookResponse = pageClient.publish(
                        pageId + "/videos",
                        FacebookType.class,
                        BinaryAttachment.with(videoName, videoAsBytes),
                        Parameter.with("description", statusText));

                // Build the successful response
                return response.success(ErmesResponse.CODE, PublishResponse.SUCCESS_MESSAGE)
                        .setData(new PublishResponse(getPostUrl(facebookResponse)));
            }
        } catch (FacebookOAuthException e) {
            errorMessage = e.getMessage();
        }

        // Build the error response
        return response.error(ErmesResponse.CODE, errorMessage);
    }

    // Get the url of a published post
    private String getPostUrl(FacebookType response) {
        return FacebookService.FACEBOOK_DOMAIN + response.getId();
    }

    // Return user's pages
    @Override
    public Connection<Page> getPages() {
        return facebookClient.fetchConnection("me/accounts", Page.class);
    }

    // Return a page found by its name
    private Page findPageByName(Connection<Page> pages, String pageName) {
        for (List<Page> feedPages : pages) {
            for (Page page : feedPages) {
                if (page.getName().equals(pageName)) {
                    return page;
                }
            }
        }

        return null;
    }

    // Permissions management
    @Override
    public FacebookServicePermission createPermissions() {
        return permissionFactory.createPermissions();
    }

    // Create a FacebookServicePermission from a list of permissions passed with the request
    @Override
    public FacebookServicePermission managePermissions(String permissions) {
        FacebookServicePermission servicePermission = createPermissions();
        List<String> permissionsList = readPermissions(permissions);
        for (String permission : permissionsList) {
            servicePermission.addPermission(permission);
        }

        return servicePermission;
    }

    // Read the permissions from the request and return them into a list which can be used to manage them
    @Override
    public List<String> readPermissions(String permissions) {
        List<String> permissionsList = new ArrayList<>();
        int index = 0;

        Pattern pattern = Pattern.compile(",", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(permissions);
        while (matcher.find()) {
            permissionsList.add(permissions.substring(index, matcher.start()).toUpperCase());
            index = matcher.start() + 1;
        }

        if (index < permissions.length()) {
            permissionsList.add(permissions.substring(index).toUpperCase());
        }

        return permissionsList;
    }

    @Override
    public String requestPermissions(String key, String secret, String callbackUrl, FacebookServicePermission permissions) {
        ScopeBuilder scopeBuilder = new ScopeBuilder();
        FacebookServicePermission userPermissions = getPermissions();

        // If null I need all the needed permissions
        if (userPermissions == null) {
            for (ServicePermission permission : permissions.getPermissions()) {
                scopeBuilder.addPermission(permission.getPermission());
            }
        } else {
            for (ServicePermission permission : permissions.getPermissions()) {
                if (!userPermissions.isGranted(permission) || !userPermissions.contains(permission)) {
                    scopeBuilder.addPermission(permission.getPermission());
                }
            }
        }

        // Request the permissions
        return new DefaultFacebookClient(Version.LATEST).getLoginDialogUrl(key, callbackUrl, scopeBuilder);
    }

    @Override
    public boolean verifyPermissions(FacebookServicePermission permissions) {
        return getPermissions().grantedPermissions(permissions);
    }

    // Get the permissions from Facebook
    private FacebookServicePermission getPermissions() {
        FacebookServicePermission permissions = createPermissions();

        // Get permissions from Facebook
        JsonObject json = facebookClient.fetchObject("me/permissions", JsonObject.class);
        if (json == null) {
            return null;
        }

        // Build the permissions
        JsonValue val = json.get("data");
        JsonArray array = val.asArray();
        for (JsonValue object : array) {
            String permission = object.asObject().get("permission").asString().toUpperCase();
            String status = object.asObject().get("status").asString();
            permissions.addPermission(permission, status);
        }

        return permissions;
    }

    @Override
    public FacebookClient getFacebookClient() {
        return facebookClient;
    }

    @Override
    public void setFacebookClient(FacebookClient facebookClient) {
        this.facebookClient = facebookClient;
    }

    @Override
    public AccessToken getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
        facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.LATEST);
    }

    private FacebookClient facebookClient;
    private AccessToken accessToken;

    // Required to build the access token
    private static final String ACCESS_TOKEN_QUERY_STRING = "access_token=";

    @Autowired
    private FacebookServicePermission permissionFactory;
}
