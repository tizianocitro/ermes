package ermes.twitter;

import ermes.response.SocialResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.TwitterAuthorizationResponse;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public interface TwitterService {
	//Authentication management
	public String getAuthUrl(String key, String secret, String callbackUrl) throws TwitterException;
	public boolean verifyConnection(String key, String secret);
	public boolean isTokenExpired();
	public boolean verifyApplicationInfo(String key, String secret);
	public void configAccessToken(String verifier);
	public boolean verifyConnectionParameters(String token, String verifier);
	public String getServiceName(StringBuffer path, String key);
	public SocialResponse<TwitterAuthorizationResponse> authorization();
	
	//Interaction
	public User getUser();
	public SocialResponse<PublishResponse> postTweet(String tweetText);
	public SocialResponse<PublishResponse> postImage(String imageUrl, String tweetText);
	public SocialResponse<PublishResponse> postVideo(String videoUrl, String tweetText);
	
	//Getter and setter
	public Twitter getTwitter();
	public void setTwitter(Twitter twitter);
	public AccessToken getAccessToken();
	public void setAccessToken(AccessToken accessToken);
	
	public static final String TWITTER_ID="twitter";
	public static final String TWITTER_TOKEN="oauth_token";
	public static final String TWITTER_VERIFIER="oauth_verifier";
	public static final String TWITTER_ERROR="error";
	
	//These parameters are needed in all the requests
	public static final String TWITTER_KEY="key";
	public static final String TWITTER_SECRET="secret";

	public static final String TWITTER_DOMAIN="https://twitter.com/";
}
