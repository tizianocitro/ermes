package ermes.response.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Modella la risposta per i servizi di pubblicazione.")
public class PublishResponse {
	public PublishResponse() {

	}
	
	public PublishResponse(String link) {
		this.link=link;
	}

	@Override
	public String toString() {
		return getClass().getName() + " [link=" + link + "]";
	}

	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link=link;
	}
	
	@ApiModelProperty(notes="Il link alla pubblicazione.", position=1)
	private String link;
	
	public static final String SUCCES_MESSAGE="Pubblicazione effettuata con successo";
	public static final String FAIL_MESSAGE="Si è verificato un errore durante la pubblicazione";
}
