package ermes.facebook;

import java.util.List;
import java.util.Set;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Page;
import com.restfb.types.User;
import ermes.response.ErmesResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.facebook.FacebookAuthorizationResponse;

public interface FacebookService {

    // Authentication management
    public String getAuthUrl(String key, String secret, String callbackUrl, ScopeBuilder scopeBuilder);

    public void createConnection(String key, String secret, String callbackUrl, String code);

    public void createConnection(String token);

    public AccessToken extendAccessToken(String key, String secret, AccessToken accessToken);

    public boolean verifyConnection(String key);

    public boolean isTokenExpiredOrNotValid();

    public boolean isTokenGiven(String token);

    public boolean verifyApplicationInfo(String key);

    public boolean verifyCode(String code);

    public boolean verifyPermissionsDenied(String denied);

    public String getServiceName(StringBuffer path, String key);

    // Authorization
    public ErmesResponse<FacebookAuthorizationResponse> authorization();

    // Interaction
    public User getUser(String fields);

    public Connection<Page> getPages();

    public ErmesResponse<PublishResponse> postStatusOnPage(String pageName, String messageText);

    public ErmesResponse<PublishResponse> postImageOnPage(String pageName, String imageUrl, String messageText);

    public ErmesResponse<PublishResponse> postVideoOnPage(String pageName, String videoUrl, String statusText);

    // Getter and setter
    public FacebookClient getFacebookClient();

    public void setFacebookClient(FacebookClient facebookClient);

    public AccessToken getAccessToken();

    public void setAccessToken(AccessToken accessToken);

    // Permissions management
    public FacebookServicePermission createPermissions();

    public FacebookServicePermission managePermissions(String permissions);

    public List<String> readPermissions(String permissions);

    public String requestPermissions(String key, String secret, String callbackUrl, FacebookServicePermission permissions);

    public boolean verifyPermissions(FacebookServicePermission permissions);

    // Inner interface
    public interface FacebookServicePermission {

        // Permissions management
        public FacebookServicePermission createPermissions();

        public void addPermission(ServicePermission permission);

        public void addPermission(String permission, String status);

        public void addPermission(String permission);

        public void addPermission(FacebookPermissions permission);

        public boolean grantedPermissions(FacebookServicePermission perms);

        public boolean isGranted(ServicePermission permission);

        public boolean contains(ServicePermission permission);

        // Getter and setter
        public Set<ServicePermission> getPermissions();

        public void setPermissions(Set<ServicePermission> permissions);

        // Inner interface
        public interface ServicePermission {

            // Getter and setter
            public FacebookPermissions getPermission();

            public void setPermission(FacebookPermissions permission);

            public String getStatus();

            public void setStatus(String status);

            public static final String DECLINED = "declined";
            public static final String GRANTED = "granted";
            public static final String WAITING = "waiting";
        }
    }

    public static final String FACEBOOK_ID = "facebook";
    public static final String FACEBOOK_CODE = "code";
    public static final String FACEBOOK_ERROR = "error";
    public static final String FACEBOOK_KEY = "key";
    public static final String FACEBOOK_SECRET = "secret";
    public static final String FACEBOOK_PERMISSIONS = "permissions";
    public static final String FACEBOOK_ACCESS_TOKEN = "token";

    // Status types
    public static final String FACEBOOK_STATUS_MESSAGE = "message";
    public static final String FACEBOOK_STATUS_LINK = "link";

    public static final String FACEBOOK_DOMAIN = "https://facebook.com/";
}
