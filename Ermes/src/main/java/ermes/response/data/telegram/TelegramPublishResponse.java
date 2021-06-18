package ermes.response.data.telegram;

import ermes.response.data.PublishResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Modella la risposta per i servizi di pubblicazione per Telegram.")
public class TelegramPublishResponse extends PublishResponse {

    public TelegramPublishResponse() {
    }

    public TelegramPublishResponse(String link, String chatId) {
        super(link);
        this.chatId = chatId;
    }


    @Override
    public String toString() {
        return getClass().getName() + " [chatId=" + chatId + ", link=" + getLink() + "]";
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @ApiModelProperty(notes = "L'id della chat su cui si è effettuata la pubblicazione.", position = 1)
    private String chatId;
}
