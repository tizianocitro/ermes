package ermes.twitter;

import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ermes.response.SocialResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.TwitterAuthorizationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
@RequestMapping(value="/twitter")
public class TwitterController {
	private static final Logger logger=LogManager.getLogger(TwitterController.class);
	
	@ApiOperation(value="Richiedi autorizzazione.",
			notes="Questo servizio permette di ottenere l'autorizzazione necessaria per l'utilizzo dei servizi.\n"
					+ "Fornire i seguenti parametri:\n"
					+ "- key: la key dell'applicazione\n"
					+ "- secret: il secret dell'applicazione")
	@GetMapping("/authorization")
	public ResponseEntity<SocialResponse<TwitterAuthorizationResponse>> authorization(
			@ApiParam(value="La key dell'applicazione") @RequestParam(value="key", required=false) String key,
			@ApiParam(value="Il secret dell'applicazione") @RequestParam(value="secret", required=false) String secret,
			HttpServletRequest request) {
		logger.debug("Servizio twitter/authorization");

		//Check errors
		String errorMessage="";
		if((errorMessage=(String) request.getAttribute(TwitterService.TWITTER_ERROR))!=null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new SocialResponse<TwitterAuthorizationResponse>().error(SocialResponse.CODE, errorMessage));
		
		return ResponseEntity.ok(twitter.authorization());
	}
	
	@ApiOperation(httpMethod="POST", 
			value="Pubblica un'immagine",
			notes="Questo servizio permette la pubblicazione di un'immagine sul profilo.\n"
					+ "Fornire i seguenti parametri:\n"
					+ "- key: la key dell'applicazione\n"
					+ "- secret: il secret dell'applicazione\n"
					+ "- image_url: l'url dell'immagine da pubblicare\n"
					+ "- text: il testo in allegato all'immagine da pubblicare")
	@RequestMapping(value="/postImage", method={RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<SocialResponse<PublishResponse>> postImage (
			@ApiParam(value="La key dell'applicazione") @RequestParam(value="key", required=false) String key,
			@ApiParam(value="Il secret dell'applicazione") @RequestParam(value="secret", required=false) String secret,
			@ApiParam(value="L'url dell'immagine da pubblicare") @RequestParam(value="image_url", required=false) String imageUrl,
			@ApiParam(value="Il testo in allegato all'immagine da pubblicare") @RequestParam(value="text", required=false) String text,
			HttpServletRequest request) {
		logger.debug("Servizio twitter/postImage");
		
		//Check errors
		String errorMessage="";
		if((errorMessage=(String) request.getAttribute(TwitterService.TWITTER_ERROR))!=null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage));
		
		//Get parameters from request
		String image=(String) request.getAttribute("image_url");
		String message=(String) request.getAttribute("text");

		return ResponseEntity.ok(twitter.postImage(image, message));
	}
	
	@ApiOperation(httpMethod="POST", 
			value="Pubblica un tweet",
			notes="Questo servizio permette la pubblicazione di un tweet sul profilo.\n"
					+ "Fornire i seguenti parametri:\n"
					+ "- key: la key dell'applicazione\n"
					+ "- secret: il secret dell'applicazione\n"
					+ "- text: il testo del tweet da pubblicare")
	@RequestMapping(value="/postTweet", method={RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<SocialResponse<PublishResponse>> postTweet (
			@ApiParam(value="La key dell'applicazione") @RequestParam(value="key", required=false) String key,
			@ApiParam(value="Il secret dell'applicazione") @RequestParam(value="secret", required=false) String secret,
			@ApiParam(value="Il testo del tweet da pubblicare") @RequestParam(value="text", required=false) String text,
			HttpServletRequest request) {
		logger.debug("Servizio twitter/postTweet");
		
		//Check errors
		String errorMessage="";
		if((errorMessage=(String) request.getAttribute(TwitterService.TWITTER_ERROR))!=null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage));
		
		//Get parameters from request
		String tweet=(String) request.getAttribute("text");
		
		return ResponseEntity.ok(twitter.postTweet(tweet));
	}
	
	@ApiOperation(httpMethod="POST", 
			value="Pubblica un video",
			notes="Questo servizio permette la pubblicazione di un video sul profilo.\n"
					+ "Fornire i seguenti parametri:\n"
					+ "- key: la key dell'applicazione\n"
					+ "- secret: il secret dell'applicazione\n"
					+ "- video_url: l'url del video da pubblicare\n"
					+ "- text: il testo in allegato al video da pubblicare")
	@RequestMapping(value="/postVideo", method={RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<SocialResponse<PublishResponse>> postVideo (
			@ApiParam(value="La key dell'applicazione") @RequestParam(value="key", required=false) String key,
			@ApiParam(value="Il secret dell'applicazione") @RequestParam(value="secret", required=false) String secret,
			@ApiParam(value="L'url del video da pubblicare") @RequestParam(value="video_url", required=false) String videoUrl,
			@ApiParam(value="Il testo in allegato al video da pubblicare") @RequestParam(value="text", required=false) String text,
			HttpServletRequest request) {
		logger.debug("Servizio twitter/postVideo");
		
		//Check errors
		String errorMessage="";
		if((errorMessage=(String) request.getAttribute(TwitterService.TWITTER_ERROR))!=null)
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage));
		
		//Get parameters from request
		String video=(String) request.getAttribute("video_url");
		String message=(String) request.getAttribute("text");

		return ResponseEntity.ok(twitter.postVideo(video, message));
	}
	
	@Autowired
	private TwitterService twitter;
}
