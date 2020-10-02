package ermes.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import ermes.response.SocialResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.twitter.TwitterAuthorizationResponse;
import ermes.util.SocialUtil;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Service
public class TwitterConnector implements TwitterService {
	public TwitterConnector()  {
	
	}
	
	@Override
	public String getAuthUrl(String key, String secret, String callbackUrl) throws TwitterException {
		//Build the connection
		createConnection(key, secret);

		return twitter.getOAuthRequestToken(callbackUrl).getAuthenticationURL();
	}
	
	private void createConnection(String key, String secret) {
		//Build the configuration in order to get a proper TwitterFactory
		ConfigurationBuilder builder=new ConfigurationBuilder();
		builder.setOAuthConsumerKey(key);
		builder.setOAuthConsumerSecret(secret);
		Configuration configuration=builder.build();
		
		TwitterFactory factory=new TwitterFactory(configuration);
		twitter=factory.getInstance();		
	}
	
	@Override
	public boolean verifyConnection(String key, String secret) {
		if(isTokenExpired() || !verifyApplicationInfo(key, secret))
			return false;
		
		return true;
	}
	
	@Override
	public boolean isTokenExpired() {
		if(accessToken==null)
			return true;
		else {
			try {
				//Check if the access token is still valid
				twitter.verifyCredentials();
			} 
			catch(TwitterException | NullPointerException | IllegalStateException e) {				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isTokenGiven(String token, String tokenSecret) {
		//Check if the needed parameter to create the access token are not missing
		if(!SocialUtil.checkString(token) || !SocialUtil.checkString(tokenSecret))
			return false;
		
		return true;
	}
	
	@Override
	public boolean verifyApplicationInfo(String key, String secret) {
		//Get the current configuration
		Configuration configuration=twitter.getConfiguration();
		String consumerKey=configuration.getOAuthConsumerKey();
		String consumerSecret=configuration.getOAuthConsumerSecret();
		
		//Check if the application has changed
		if(!consumerKey.equals(key) || !consumerSecret.equals(secret))
			return false;
		
		return true;
	}
	
	@Override
	public void configAccessToken(String verifier) {
		try {
			accessToken=twitter.getOAuthAccessToken(verifier);
			twitter.setOAuthAccessToken(accessToken);
		} 
		catch(TwitterException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void configAccessToken(String key, String secret, AccessToken accessToken) {
		//Build the connection
		createConnection(key, secret);
		
		this.accessToken=accessToken;
		twitter.setOAuthAccessToken(accessToken);
	}
	
	@Override
	public AccessToken buildAccessToken(String token, String tokenSecret, String userId) {
		//Check the id
		long id=DEFAULT_USER_ID;
		try {
			if(SocialUtil.checkString(userId))
				id=Long.parseLong(userId);
		}
		catch(NumberFormatException e) {
			throw new NumberFormatException(USER_ID_NOT_VALID + " \'" + userId + "\'");
		}
		
		return new AccessToken(token, tokenSecret, id);
	}
	
	@Override
	public boolean verifyConnectionParameters(String token, String verifier) {
		if(!SocialUtil.checkString(token) || !SocialUtil.checkString(verifier))
			return false;
		
		return true;
	}
	
	@Override
	public SocialResponse<TwitterAuthorizationResponse> authorization() {
		//Create the response
		SocialResponse<TwitterAuthorizationResponse> socialResponse=new SocialResponse<TwitterAuthorizationResponse>();
		
		//Build Twitter response
		TwitterAuthorizationResponse twitterAccessTokenResponse=new TwitterAuthorizationResponse();
		twitterAccessTokenResponse.setAccessToken(accessToken.getToken());
		twitterAccessTokenResponse.setAccessTokenSecret(accessToken.getTokenSecret());
		twitterAccessTokenResponse.setUserId(String.valueOf(accessToken.getUserId()));

		//Get the current configuration in order to get key and secret
		Configuration configuration=twitter.getConfiguration();
		String consumerKey=configuration.getOAuthConsumerKey();
		String consumerSecret=configuration.getOAuthConsumerSecret();
		
		//Set key and secret into response
		twitterAccessTokenResponse.setApplicationKey(consumerKey);
		twitterAccessTokenResponse.setApplicationSecret(consumerSecret);
		
		//Build response
		socialResponse.success(SocialResponse.CODE, SocialResponse.SUCCES_MESSAGE)
			.setData(twitterAccessTokenResponse);
		
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
	public User getUser() {
		User me=null;
		
		try {
			me=twitter.showUser(twitter.getScreenName());
		} 
		catch(IllegalStateException | TwitterException e) {
			e.printStackTrace();
		}
		
		return me;
	}
	
	//Publish a tweet
	@Override
	public SocialResponse<PublishResponse> postTweet(String tweetText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Check parameters
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		if(!SocialUtil.checkString(tweetText))
			return socialResponse.error(SocialResponse.CODE, SocialUtil.format(errorMessage));
		
		try {
			//Publish the tweet
			StatusUpdate statusUpdate=new StatusUpdate(tweetText);
			Status status=twitter.updateStatus(statusUpdate);
			
			//Build the successful response
			socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getTweetUrl(status)));
			
			return socialResponse;
		} 
		catch(TwitterException e) {
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, SocialUtil.format(errorMessage));
	}
	
	//Get the url of a published tweet
	private String getTweetUrl(Status status) {
		return new StringBuilder(TwitterService.TWITTER_DOMAIN)
				.append(status.getUser().getScreenName())
				.append("/status/")
				.append(status.getId())
				.toString();		
	}
	
	//Publish an image
	@Override
	public SocialResponse<PublishResponse> postImage(String imageUrl, String tweetText) {
		//Check parameters
		if(!SocialUtil.checkString(imageUrl))
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		//The text is not needed if it is not specified
		if(tweetText==null)
			tweetText="";
		
		try {
			SocialUtil.saveMedia(imageUrl);
			
			URL url=new URL(imageUrl);
			String fileName=url.getFile();
			String imageName=fileName.substring(fileName.lastIndexOf("/"));
			
			return postImageByUrl(tweetText, imageName);
		} 
		catch(IOException e) {
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, SocialUtil.format(errorMessage));
	}
	
	private SocialResponse<PublishResponse> postImageByUrl(String tweetText, String imageName) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		String imageFilePath=SocialUtil.PATH + imageName;
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {			
			StatusUpdate statusUpdate=new StatusUpdate(tweetText);
			File file=new File(imageFilePath);
			statusUpdate.setMedia(file);
			
			//Publish the image
			Status status=twitter.updateStatus(statusUpdate);
			
			//Build the successful response
			socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getImageTweetUrl(status)));
			
			return socialResponse;
		} 
		catch(TwitterException e) {
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, SocialUtil.format(errorMessage));
	}
	
	//Publish an image
	@Override
	public SocialResponse<PublishResponse> postVideo(String videoUrl, String tweetText) {
		//Check parameters
		if(!SocialUtil.checkString(videoUrl))
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//The text is not needed if it is not specified
		if(tweetText==null)
			tweetText="";
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			SocialUtil.saveMedia(videoUrl);
			
			URL url=new URL(videoUrl);
			String fileName=url.getFile();
			String videoName=fileName.substring(fileName.lastIndexOf("/"));
			
			return postVideoByUrl(tweetText, videoName);
		} 
		catch(IOException e) {
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, SocialUtil.format(errorMessage));
	}
	
	private SocialResponse<PublishResponse> postVideoByUrl(String tweetText, String videoName) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		String videoFilePath=SocialUtil.PATH + videoName;
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {			
			StatusUpdate statusUpdate=new StatusUpdate(tweetText);
			
			//Upload the video
			File file=new File(videoFilePath);
			FileInputStream fileInputStream=new FileInputStream(file);
			statusUpdate.setMediaIds(twitter.uploadMediaChunked(videoFilePath, fileInputStream).getMediaId());
			
			//Publish the video
			Status status=twitter.updateStatus(statusUpdate);
			
			//Build the successful response
			socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
					.setData(new PublishResponse(getImageTweetUrl(status)));
			
			return socialResponse;
		} 
		catch(TwitterException e) {
			errorMessage=e.getMessage();
		}
		catch(FileNotFoundException e) {
			errorMessage=e.getMessage();
		}
		
		//Build the error response
		return socialResponse.error(SocialResponse.CODE, SocialUtil.format(errorMessage));
	}
	
	//Get the url of a published tweet with an image
	private String getImageTweetUrl(Status status) {
		return status.getText().substring(status.getText().lastIndexOf(" ")+1);
	}
	
	@Override
	public Twitter getTwitter() {
		return twitter;
	}
	
	@Override
	public void setTwitter(Twitter twitter) {
		this.twitter=twitter;
	}
	
	@Override
	public AccessToken getAccessToken() {
		return accessToken;
	}
	
	@Override
	public void setAccessToken(AccessToken accessToken) {
		this.accessToken=accessToken;
		twitter.setOAuthAccessToken(accessToken);
	}
	
	private Twitter twitter;
	private AccessToken accessToken;
	
	private static final long DEFAULT_USER_ID=-1l;
	public static final String USER_ID_NOT_VALID="The number format of user_id parameter is not valid";
}
