package ermes.response.data.telegram;

import ermes.response.data.PublishResponse;

public class TelegramPublishResponse extends PublishResponse {
	public TelegramPublishResponse() {

	}
	
	public TelegramPublishResponse(String link, String chatId) {
		super(link);
		this.chatId=chatId;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId=chatId;
	}

	private String chatId;
}
