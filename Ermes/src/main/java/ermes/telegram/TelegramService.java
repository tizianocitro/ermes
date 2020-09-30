package ermes.telegram;

import com.pengrad.telegrambot.TelegramBot;
import ermes.response.SocialResponse;
import ermes.response.data.PublishResponse;

public interface TelegramService {
	//Connection management
	public boolean verifyBotToken();
	public boolean verifyBotInfo(String token);
	public void createConnection(String token);
	public boolean isChatIdFromUrl(String chatId);
	public String manageChatId(String chatId);
	
	//Interaction
	public SocialResponse<PublishResponse> sendMessage(String chatId, String messageText);
	public SocialResponse<PublishResponse> sendPhoto(String chatId, String imageUrl, String messageText);
	public SocialResponse<PublishResponse> sendVideo(String chatId, String videoUrl, String messageText);
	
	//Getter and setter
	public TelegramBot getTelegramBot();
	public void setTelegramBot(TelegramBot telegramBot);
	public String getBotToken();
	public void setBotToken(String botToken);
	
	//Domains
	public static final String TELEGRAM_ME="https://t.me/";
	public static final String TELEGRAM_ME_PRIVATE="https://t.me/c/";
	public static final String TELEGRAM_DOMAIN="https://web.telegram.org/";
	
	//Telegram prefixs
	public static final String TELEGRAM_PRIVATE_CHANNEL_PREFIX="-100";
	public static final String TELEGRAM_PRIVATE_GROUP_PREFIX="-";
	public static final String TELEGRAM_PUBLIC_CHANNEL_AND_GROUP_PREFIX="@";

	public static final String TELEGRAM_TOKEN="token";
	public static final String TELEGRAM_ERROR="error";
	
	//Custom error
	public static final String TELEGRAM_NOT_VALID_BOT_TOKEN="The parameter \'token\' cannot be null or an empty string";
}
