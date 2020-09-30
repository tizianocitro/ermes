package ermes.facebook;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import ermes.response.SocialResponse;
import ermes.response.data.FacebookAuthorizationResponse;
import ermes.response.data.PublishResponse;
import ermes.util.SocialUtil;

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
		this.accessToken=new DefaultFacebookClient(Version.LATEST).obtainUserAccessToken(key, secret, callbackUrl, code);
		facebookClient=new DefaultFacebookClient(accessToken.getAccessToken(), Version.LATEST);
	}
	
	@Override
	public boolean verifyConnection(String key) {
		if(isTokenExpired() || !verifyApplicationInfo(key))
			return false;
		
		return true;
	}
	
	@Override
	public boolean isTokenExpired() {
		if(accessToken==null)
			return true;
		
		DebugTokenInfo tokenInfo=facebookClient.debugToken(accessToken.getAccessToken());
		if(!tokenInfo.isValid())
			return true;
			
		return false;
	}
	
	@Override
	public boolean verifyApplicationInfo(String key) {
		DebugTokenInfo tokenInfo=facebookClient.debugToken(accessToken.getAccessToken());
		String applicationKey=tokenInfo.getAppId();
		if(!applicationKey.equals(key)) {
			return false;
		}
	
		return true;
	}
	
	@Override
	public boolean verifyCode(String code) {
		if(!SocialUtil.checkString(code))
			return false;
		
		return true;
	}
	
	@Override
	public SocialResponse<FacebookAuthorizationResponse> authorization() {
		//Create the response
		SocialResponse<FacebookAuthorizationResponse> socialResponse=new SocialResponse<FacebookAuthorizationResponse>();
		
		//Get the debug token info
		DebugTokenInfo tokenInfo=facebookClient.debugToken(accessToken.getAccessToken());

		//Build Facebook response
		FacebookAuthorizationResponse facebookAccessTokenResponse=new FacebookAuthorizationResponse();
		facebookAccessTokenResponse.setAccessToken(accessToken.getAccessToken());
		facebookAccessTokenResponse.setExpires(tokenInfo.getExpiresAt().toString());
		facebookAccessTokenResponse.setApplicationKey(tokenInfo.getAppId());
		facebookAccessTokenResponse.setApplicationName(tokenInfo.getApplication());
		facebookAccessTokenResponse.setUserId(tokenInfo.getUserId());
				
		//Build the response
		socialResponse.success(SocialResponse.CODE, SocialResponse.SUCCES_MESSAGE)
				.setData(facebookAccessTokenResponse);
		
		return socialResponse;
	}
	
	//Return the name of a service
	@Override
	public String getServiceName(StringBuffer path, String key) {	
		Pattern pattern=Pattern.compile(key, Pattern.CASE_INSENSITIVE);
		Matcher matcher=pattern.matcher(path);
		if(matcher.find())
			return path.substring(matcher.start());
		
		return null;
	}
	
	//Return user's representation
	@Override
	public User getUser(String fields) {
		return facebookClient.fetchObject("/me", User.class, Parameter.with("fields", fields));
	}
	
	//Publish a status on a user's page
	@Override
	public SocialResponse<PublishResponse> postStatusOnPage(String pageName, String statusText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Check parameters
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		if(!SocialUtil.checkString(pageName) || !SocialUtil.checkString(statusText)) 
			return socialResponse.error(SocialResponse.CODE, errorMessage);
			
		//Get the type of the status because Facebook didn't handle link by itself
		String statusType=getStatusType(statusText);
		
		//Get user's pages
		Connection<Page> result=getPages();
		
		try {			
			//Find the page with name equals to pageName
			Page page=findPageByName(result, pageName);
			if(page!=null) {
				//Get page's access token and id in order to publish on it
				String pageAccessToken=page.getAccessToken();;					
				String pageID=page.getId();
				
				//Build the message in case the status contains a link which come with message
				String message=statusText;
				
				//Build link in case the status' text contains a link
				if(statusType.equals(FacebookService.FACEBOOK_STATUS_LINK)) {
					statusText=getUrlToPublish(statusText);
					
					//Clear the message if the link does not come with a message
					if(message.equals(statusText))
						message="";
				}
				
				FacebookClient pageClient=new DefaultFacebookClient(pageAccessToken, Version.LATEST);
				FacebookType facebookResponse=pageClient.publish(pageID + "/feed", FacebookType.class, 
						//In case it's a link show the preview of the link
						//In case it's a simple message publish the status with message only
						Parameter.with(statusType, statusText),
						//In case the link come with a message
						Parameter.with("message", message));
				
				//Build the successful response
				socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getPostUrl(facebookResponse)));
				
				return socialResponse;
			}
		}
		catch(FacebookOAuthException e) {
			//Facebook throws this exception in case of duplicate status or in case of invalid link, just two examples
			e.printStackTrace();
			
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, errorMessage);
	}
	
	//Return the type of the status which is going to be published
	private String getStatusType(String statusText) {
		if(SocialUtil.contains(statusText, SocialUtil.HTTPS) || SocialUtil.contains(statusText, SocialUtil.HTTP))
			return FacebookService.FACEBOOK_STATUS_LINK;
		
		return FacebookService.FACEBOOK_STATUS_MESSAGE;
	}
	
	//Get the url from a status' text
	private String getUrlToPublish(String statusText) {
		//Get the https url
		String url=SocialUtil.getSubstring(statusText, SocialUtil.HTTPS);
		if(SocialUtil.checkString(url))
			return url;
		
		//Get the http url, eventually
		url=SocialUtil.getSubstring(statusText, SocialUtil.HTTP);
		if(SocialUtil.checkString(url))
			return url;
		
		return "";
	}
	
	//Publish an image on a user's page given the url
	@Override
	public SocialResponse<PublishResponse> postImageOnPage(String pageName, String imageUrl, String statusText) {
		//Check parameters
		if(!SocialUtil.checkString(imageUrl) || !SocialUtil.checkString(pageName)) 
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//The message is not needed if it is not specified
		if(statusText==null)
			statusText="";
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			SocialUtil.saveMedia(imageUrl);
			
			//Get image's path and name
			URL url=new URL(imageUrl);
			String fileName=url.getFile();
			String imageName=fileName.substring(fileName.lastIndexOf("/")+1);
			String imageFilePath=SocialUtil.PATH + imageName;
			
			return postImage(pageName, imageFilePath, imageName, statusText);
		} 
		catch(IOException e) {
			//Throw in case of issue with the image
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage);
	}
	
	//Publish an image on a user's page
	private SocialResponse<PublishResponse> postImage(String pageName, String imageFilePath, String imageName, String statusText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			//Get user's pages
			Connection<Page> result=getPages();
			
			//Find image's format
			String imageFormat=SocialUtil.getImageFormat(imageFilePath);
	
			//Convert image to byte array
			byte[] imageAsBytes=SocialUtil.fetchBytesFromImage(imageFilePath, imageFormat);
				
			//Find the page by the given name in order to get the id and the access token of the needed page
			Page page=findPageByName(result, pageName);
			if(page!=null) {
				//Get page's access token and id in order to publish on it
				String pageAccessToken=page.getAccessToken();;					
				String pageID=page.getId();
	
				//Publish the image
				FacebookClient pageClient=new DefaultFacebookClient(pageAccessToken, Version.LATEST);
				FacebookType facebookResponse=pageClient.publish(pageID + "/photos", FacebookType.class, 
						BinaryAttachment.with(imageName, imageAsBytes, "image/" + imageFormat),
						Parameter.with("message", statusText));
				
				//Build the successful response
				socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getPostUrl(facebookResponse)));
				
				return socialResponse;
			}
		}
		catch(RuntimeException e) {
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, errorMessage);
	}
	
	//Publish a video on a user's page given the url
	@Override
	public SocialResponse<PublishResponse> postVideoOnPage(String pageName, String videoUrl, String statusText) {
		//Check parameters
		if(!SocialUtil.checkString(videoUrl) || !SocialUtil.checkString(pageName)) 
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//The message is not needed if it is not specified
		if(statusText==null)
			statusText="";
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			SocialUtil.saveMedia(videoUrl);
			
			//Get image's path and name
			URL url=new URL(videoUrl);
			String fileName=url.getFile();
			String videoName=fileName.substring(fileName.lastIndexOf("/")+1);
			String videoFilePath=SocialUtil.PATH + videoName;
			
			return postVideo(pageName, videoFilePath, videoName, statusText);
		} 
		catch(IOException e) {
			//Throw in case of issue with the image (File not found, No protocol...)
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage);
	}
	
	//Publish an image on a user's page
	private SocialResponse<PublishResponse> postVideo(String pageName, String videoFilePath, String videoName, String statusText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			//Get user's pages
			Connection<Page> result=getPages();
			
			//Convert image to byte array
			byte[] videoAsBytes=SocialUtil.fetchBytesFromVideo(videoFilePath);
			
			//Find the page by the given name in order to get the id and the access token of the needed page
			Page page=findPageByName(result, pageName);
			if(page!=null) {
				//Get page's access token and id in order to publish on it
				String pageAccessToken=page.getAccessToken();;					
				String pageID=page.getId();
	
				//Publish the image
				FacebookClient pageClient=new DefaultFacebookClient(pageAccessToken, Version.LATEST);
				FacebookType facebookResponse=pageClient.publish(pageID + "/videos", FacebookType.class,
						BinaryAttachment.with(videoName, videoAsBytes),
						Parameter.with("description", statusText));
				
				//Build the successful response
				socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getPostUrl(facebookResponse)));
				
				return socialResponse;
			}
		}
		catch(FacebookOAuthException e) {
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, errorMessage);
	}
	
	//Get the url of a published post
	private String getPostUrl(FacebookType response) {
		return FacebookService.FACEBOOK_DOMAIN + response.getId();
	}
	
	//Return user's pages
	@Override
	public Connection<Page> getPages() {
		return facebookClient.fetchConnection("me/accounts", Page.class);
	}
	
	//Return a page found by its name
	private Page findPageByName(Connection<Page> pages, String pageName) {
		for(List<Page> feedPages: pages) {
			for(Page page: feedPages) {
				if(page.getName().equals(pageName)) {
					return page;
				}
			}
		}
		
		return null;
	}
	
	//Permissions management
	@Override
	public FacebookServicePermission createPermissions() {
		return permissionFactory.createPermissions();
	}
	
	//Create a FacebookServicePermission from a list of permissions passed with the request
	@Override
	public FacebookServicePermission managePermission(String permissions) {
		FacebookServicePermission servicePermission=createPermissions();
		
		List<String> permissionsList=readPermissions(permissions);
		for(String permission: permissionsList)
			servicePermission.addPermission(permission);
		
		return servicePermission;
	}
	
	//Read the permissions from the request and return them into a list which can be used to manage them
	@Override
	public List<String> readPermissions(String permissions) {
		List<String> permissionsList=new ArrayList<String>();
		int index=0;
		
		Pattern pattern=Pattern.compile(",", Pattern.CASE_INSENSITIVE);
		Matcher matcher=pattern.matcher(permissions);
		while(matcher.find()) {
			permissionsList.add(permissions.substring(index, matcher.start()).toUpperCase());
			index=matcher.start()+1;	
		}
		
		if(index<permissions.length())
			permissionsList.add(permissions.substring(index).toUpperCase());
		
		return permissionsList;
	}
	
	@Override
	public String requestPermissions(String key, String secret, String callbackUrl, FacebookServicePermission permissions) {
		ScopeBuilder scopeBuilder=new ScopeBuilder();
		
		FacebookServicePermission userPermissions=getPermissions();
		
		//If null I need all the needed permissions
		if(userPermissions==null) {
			for(ServicePermission permission: permissions.getPermissions())
				scopeBuilder.addPermission(permission.getPermission());
		}
		else {
			for(ServicePermission permission: permissions.getPermissions())
				if(!userPermissions.isGranted(permission) || !userPermissions.contains(permission))
					scopeBuilder.addPermission(permission.getPermission());
		}
		
		//Request the permissions
		return new DefaultFacebookClient(Version.LATEST).getLoginDialogUrl(key, callbackUrl, scopeBuilder);
	}
	
	@Override
	public boolean verifyPermissions(FacebookServicePermission permissions) {
		FacebookServicePermission userPermissions=getPermissions();
		
		return userPermissions.grantedPermissions(permissions);
	}
	
	//Get the permissions from Facebook
	private FacebookServicePermission getPermissions() {
		FacebookServicePermission permissions=createPermissions();
		
		//Get permissions from Facebook
		JsonObject json=facebookClient.fetchObject("me/permissions", JsonObject.class);
		if(json==null)
			return null;
		
		//Build the permissions
		JsonValue val=json.get("data");
		JsonArray array=val.asArray();		
		for(JsonValue object: array) {
			String permission=object.asObject().get("permission").asString().toUpperCase();
			String status=object.asObject().get("status").asString();
			
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
		this.facebookClient=facebookClient;
	}

	@Override
	public AccessToken getAccessToken() {
		return accessToken;
	}

	@Override
	public void setAccessToken(AccessToken accessToken) {
		this.accessToken=accessToken;
		facebookClient=new DefaultFacebookClient(accessToken.getAccessToken(), Version.LATEST);
	}

	private FacebookClient facebookClient;
	private AccessToken accessToken;
	
	@Autowired
	private FacebookServicePermission permissionFactory;
}
