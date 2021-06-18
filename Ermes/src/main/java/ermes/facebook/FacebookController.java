package ermes.facebook;

import ermes.response.ErmesResponse;
import ermes.response.data.PublishResponse;
import ermes.response.data.facebook.FacebookAuthorizationResponse;
import ermes.util.AuthUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping(value = "/facebook")
public class FacebookController {

    private static final Logger logger = LogManager.getLogger(FacebookController.class);

    @ApiOperation(value = "Richiedi autorizzazione.",
            notes = "Questo servizio permette di ottenere l'autorizzazione necessaria per l'utilizo dei servizi.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- permissions: i permessi necessari per l'utilizzo dei servizi")
    @GetMapping("/authorization")
    public ResponseEntity<ErmesResponse<FacebookAuthorizationResponse>> authorization(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "I permessi necessari per l'utilizzo dei servizi") @RequestParam(value = "permissions", required = false) String permissions,
            HttpServletRequest request) {
        logger.debug("Servizio facebook/authorization");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<FacebookAuthorizationResponse>().error(ErmesResponse.CODE, authErrorMessage));

        return ResponseEntity.ok(facebook.authorization());
    }

    @ApiOperation(httpMethod = "POST",
            value = "Pubblica un'immagine",
            notes = "Questo servizio permette la pubblicazione di un'immagine su una pagina.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- permissions: i permessi necessari alla pubblicazione (attualmente: pages_manage_posts,pages_read_engagement,pages_show_list)\n"
                    + "- page_name: il nome della pagina su cui pubblicare\n"
                    + "- image_url: l'url dell'immagine da pubblicare\n"
                    + "- text: il testo in allegato all'immagine da pubblicare")
    @RequestMapping(value = "/postImage", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postImage(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "I permessi necessari alla pubblicazione") @RequestParam(value = "permissions", required = false) String permissions,
            @ApiParam(value = "Il nome della pagina su cui pubblicare") @RequestParam(value = "page_name", required = false) String pageName,
            @ApiParam(value = "L'url dell'immagine da pubblicare") @RequestParam(value = "image_url", required = false) String imageUrl,
            @ApiParam(value = "Il testo in allegato all'immagine da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio facebook/postImage");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String page = (String) request.getAttribute("page_name");
        String image = (String) request.getAttribute("image_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(facebook.postImageOnPage(page, image, message));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Pubblica uno status",
            notes = "Questo servizio permette la pubblicazione di uno status su una pagina.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- permissions: i permessi necessari alla pubblicazione (attualmente: pages_manage_posts,pages_read_engagement,pages_show_list)\n"
                    + "- page_name: il nome della pagina su cui pubblicare\n"
                    + "- text: il testo dello status da pubblicare")
    @RequestMapping(value = "/postStatus", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postStatus(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "I permessi necessari alla pubblicazione") @RequestParam(value = "permissions", required = false) String permissions,
            @ApiParam(value = "Il nome della pagina su cui pubblicare") @RequestParam(value = "page_name", required = false) String pageName,
            @ApiParam(value = "Il testo dello status da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio facebook/postStatus");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String page = (String) request.getAttribute("page_name");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(facebook.postStatusOnPage(page, message));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Pubblica un video",
            notes = "Questo servizio permette la pubblicazione di un video su una pagina.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- key: la key dell'applicazione\n"
                    + "- secret: il secret dell'applicazione\n"
                    + "- token: il valore associato al token\n"
                    + "- permissions: i permessi necessari alla pubblicazione (attualmente: pages_manage_posts,pages_read_engagement,pages_show_list)\n"
                    + "- page_name: il nome della pagina su cui pubblicare\n"
                    + "- video_url: l'url del video da pubblicare\n"
                    + "- text: il testo in allegato al video da pubblicare")
    @RequestMapping(value = "/postVideo", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<PublishResponse>> postVideo(
            @ApiParam(value = "La key dell'applicazione") @RequestParam(value = "key", required = false) String key,
            @ApiParam(value = "Il secret dell'applicazione") @RequestParam(value = "secret", required = false) String secret,
            @ApiParam(value = "Il valore associato al token") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "I permessi necessari alla pubblicazione") @RequestParam(value = "permissions", required = false) String permissions,
            @ApiParam(value = "Il nome della pagina su cui pubblicare") @RequestParam(value = "page_name", required = false) String pageName,
            @ApiParam(value = "L'url del video da pubblicare") @RequestParam(value = "video_url", required = false) String videoUrl,
            @ApiParam(value = "Il testo in allegato al video da pubblicare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio facebook/postVideo");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<PublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String page = (String) request.getAttribute("page_name");
        String video = (String) request.getAttribute("video_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(facebook.postVideoOnPage(page, video, message));
    }

    @Autowired
    private FacebookService facebook;
}

