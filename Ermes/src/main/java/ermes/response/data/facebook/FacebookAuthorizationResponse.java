package ermes.response.data.facebook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Modella la risposta per il servizio di autorizzazione.")
public class FacebookAuthorizationResponse {
	public FacebookAuthorizationResponse() {

	}
	
	public FacebookAuthorizationResponse(String accessToken, String applicationKey, String applicationName, 
			String expires, String userId) {
		this.accessToken=accessToken;
		this.applicationKey=applicationKey;
		this.applicationName=applicationName;
		this.expires=expires;
		this.userId=userId;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken=accessToken;
	}
	
	public String getApplicationKey() {
		return applicationKey;
	}
	
	public void setApplicationKey(String applicationKey) {
		this.applicationKey=applicationKey;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName=applicationName;
	}
	
	public String getExpires() {
		return expires;
	}
	
	public void setExpires(String expires) {
		this.expires=expires;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId=userId;
	}
	
	@ApiModelProperty(notes="L'access token.", position=1)
	private String accessToken;
	
	@ApiModelProperty(notes="La key dell'applicazione associata all'access token.", position=2)
	private String applicationKey;
	
	@ApiModelProperty(notes="Il nome dell'applicazione associata all'access token.", position=3)
	private String applicationName;
	
	@ApiModelProperty(notes="La data di scadenza dell'access token.", position=4)
	private String expires;
	
	@ApiModelProperty(notes="L'id dell'utente associato all'access token.", position=5)
	private String userId;
}
