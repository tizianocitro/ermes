package ermes.telegram;

import javax.servlet.http.HttpServletRequest;

import ermes.response.data.PublishResponse;
import ermes.util.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ermes.response.ErmesResponse;
import ermes.response.data.telegram.TelegramPublishResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin
@RestController
@RequestMapping(value = "/telegram")
public class TelegramController {

    private static final Logger logger = LogManager.getLogger(TelegramController.class);

    @ApiOperation(httpMethod = "POST",
            value = "Invia un'immagine",
            notes = "Questo servizio permette di inviare di un'immagine su un canale o gruppo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- token: il token del bot\n"
                    + "- chat_id: l'id della chat\n"
                    + "- image_url: l'url dell'immagine da inviare\n"
                    + "- text: il testo in allegato all'immagine da inviare")
    @RequestMapping(value = "/sendPhoto", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<TelegramPublishResponse>> sendPhoto(
            @ApiParam(value = "Il token del bot") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "L'id della chat") @RequestParam(value = "chat_id", required = false) String chatId,
            @ApiParam(value = "L'url dell'immagine da inviare") @RequestParam(value = "image_url", required = false) String imageUrl,
            @ApiParam(value = "Il testo in allegato all'immagine da inviare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio telegram/sendPhoto");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<TelegramPublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String id = (String) request.getAttribute("chat_id");
        String image = (String) request.getAttribute("image_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(telegram.sendPhoto(id, image, message));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Invia un messaggio",
            notes = "Questo servizio permette di inviare di un messaggio su un canale o gruppo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- token: il token del bot\n"
                    + "- chat_id: l'id del canale o gruppo\n"
                    + "- text: il testo del messaggio da inviare")
    @RequestMapping(value = "/sendMessage", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<TelegramPublishResponse>> sendMessage(
            @ApiParam(value = "Il token del bot") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "L'id della chat") @RequestParam(value = "chat_id", required = false) String chatId,
            @ApiParam(value = "Il testo in allegato all'immagine da inviare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio telegram/sendMessage");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<TelegramPublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String id = (String) request.getAttribute("chat_id");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(telegram.sendMessage(id, message));
    }

    @ApiOperation(httpMethod = "POST",
            value = "Invia un video",
            notes = "Questo servizio permette di inviare un video su un canale o gruppo.\n"
                    + "Fornire i seguenti parametri:\n"
                    + "- token: il token del bot\n"
                    + "- chat_id: l'id della chat\n"
                    + "- video_url: l'url del video da inviare\n"
                    + "- text: il testo in allegato al video da inviare")
    @RequestMapping(value = "/sendVideo", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<ErmesResponse<TelegramPublishResponse>> sendVideo(
            @ApiParam(value = "Il token del bot") @RequestParam(value = "token", required = false) String token,
            @ApiParam(value = "L'id della chat") @RequestParam(value = "chat_id", required = false) String chatId,
            @ApiParam(value = "L'url del video da inviare") @RequestParam(value = "video_url", required = false) String videoUrl,
            @ApiParam(value = "Il testo in allegato al video da inviare") @RequestParam(value = "text", required = false) String text,
            HttpServletRequest request) {
        logger.debug("Servizio telegram/sendVideo");

        String authErrorMessage = AuthUtils.isAuthOrErrorMessage(request);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErmesResponse<TelegramPublishResponse>().error(ErmesResponse.CODE, authErrorMessage));

        // Get parameters from request
        String id = (String) request.getAttribute("chat_id");
        String video = (String) request.getAttribute("video_url");
        String message = (String) request.getAttribute("text");

        return ResponseEntity.ok(telegram.sendVideo(id, video, message));
    }

    @Autowired
    private TelegramService telegram;
}
