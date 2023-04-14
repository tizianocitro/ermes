package ermes.twitter;

import javax.servlet.http.HttpServletRequest;

import ermes.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
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
import ermes.response.ErmesResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.twitter.TwitterAuthorizationResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
@RequestMapping(value = "/twitter")
public class TwitterController {

    private static final Logger logger = LogManager.getLogger(TwitterController.class);

    @ApiOperation(value = "Richiedi autorizzazione.",
            notes = "Questo servizio permette di ottenere l'autorizzazione necessaria per l'utilizzo dei servizi.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione")
    @GetMapping("/authorization")
    public ResponseEntity<ErmesResponse<TwitterAuthorizationResponse>> authorization(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            HttpServletRequest request) {
        logger.debug("Servizio twitter/authorization");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<TwitterAuthorizationResponse>().error(ErmesResponse.CODE, authErrorMessage));
        }

		return ResponseEntity.ok(twitter.authorization());
    }

    @ApiOperation(httpMethod = "POST",
            value = "Pubblica un'immagine",
            notes = "Questo servizio permette la pubblicazione di un'immagine sul profilo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- token_secret: il secret associato al token\n"
                    + "- user_id: l'id dell'utente associato al token\n"
                    + "- image_url: l'url dell'immagine da pubblicare\n"
                    + "- text: il testo in allegato all'immagine da pubblicare")
    @RequestMapping(value = "/postImage", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postImage(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "Il secret associato al token") @RequestParam(value = "token_secret", required = false) String tokenSecret,
            @ApiParam(value = "L'id dell'utente associato al token") @RequestParam(value = "user_id", required = false) String userId,
            @ApiParam(value = "L'url dell'immagine da pubblicare") @RequestParam(value = "image_url", required = false) String imageUrl,
            @ApiParam(value = "Il testo in allegato all'immagine da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio twitter/postImage");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
		if (StringUtils.isNotEmpty(authErrorMessage)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));
        }

		// Get parameters from request
        String image = (String) request.getAttribute("image_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(twitter.postImage(image, message));
    }

	@ApiOperation(httpMethod = "POST",
            value = "Pubblica un tweet",
            notes = "Questo servizio permette la pubblicazione di un tweet sul profilo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- token_secret: il secret associato al token\n"
                    + "- user_id: l'id dell'utente associato al token\n"
                    + "- text: il testo del tweet da pubblicare")
    @RequestMapping(value = "/postTweet", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postTweet(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "Il secret associato al token") @RequestParam(value = "token_secret", required = false) String tokenSecret,
            @ApiParam(value = "L'id dell'utente associato al token") @RequestParam(value = "user_id", required = false) String userId,
            @ApiParam(value = "Il testo del tweet da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio twitter/postTweet");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));
        }

		// Get parameters from request
        String tweet = (String) request.getAttribute("text");

        return ResponseEntity.ok(twitter.postTweet(tweet));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Pubblica un video",
            notes = "Questo servizio permette la pubblicazione di un video sul profilo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- token_secret: il secret associato al token\n"
                    + "- user_id: l'id dell'utente associato al token\n"
                    + "- video_url: l'url del video da pubblicare\n"
                    + "- text: il testo in allegato al video da pubblicare")
    @RequestMapping(value = "/postVideo", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postVideo(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "Il secret associato al token") @RequestParam(value = "token_secret", required = false) String tokenSecret,
            @ApiParam(value = "L'id dell'utente associato al token") @RequestParam(value = "user_id", required = false) String userId,
            @ApiParam(value = "L'url del video da pubblicare") @RequestParam(value = "video_url", required = false) String videoUrl,
            @ApiParam(value = "Il testo in allegato al video da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio twitter/postVideo");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));
        }

		// Get parameters from request
        String video = (String) request.getAttribute("video_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(twitter.postVideo(video, message));
    }

    @Autowired
    private TwitterService twitter;
}
