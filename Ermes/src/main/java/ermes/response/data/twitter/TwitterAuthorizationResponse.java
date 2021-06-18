package ermes.response.data.twitter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Modella la risposta per il servizio di autorizzazione.")
public class TwitterAuthorizationResponse {

    public TwitterAuthorizationResponse() {
    }

    public TwitterAuthorizationResponse(String accessToken, String accessTokenSecret, String userId,
			String applicationKey, String applicationSecret) {
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.applicationKey = applicationKey;
        this.applicationSecret = applicationSecret;
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @ApiModelProperty(notes = "L'access token.", position = 1)
    private String accessToken;

    @ApiModelProperty(notes = "Il secret associato all'access token.", position = 2)
    private String accessTokenSecret;

    @ApiModelProperty(notes = "La key dell'applicazione associata all'access token.", position = 3)
    private String applicationKey;

    @ApiModelProperty(notes = "Il secret dell'applicazione associata all'access token.", position = 4)
    private String applicationSecret;

    @ApiModelProperty(notes = "L'id dell'utente associato all'access token.", position = 5)
    private String userId;
}
