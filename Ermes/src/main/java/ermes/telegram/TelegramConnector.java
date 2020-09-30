package ermes.telegram;

import java.io.IOException;
import java.net.URL;
import org.springframework.stereotype.Service;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;
import ermes.response.SocialResponse;
import ermes.response.data.PublishResponse;
import ermes.util.SocialUtil;

@Service
public class TelegramConnector implements TelegramService {
	public TelegramConnector() {
		
	}

	@Override
	public boolean verifyBotToken() {
		//Check if the token is valid
		if(!SocialUtil.checkString(botToken))
			return false;
		
		return true;
	}
	
	@Override
	public boolean verifyBotInfo(String token) {
		//Check if the bot has changed
		return token.equals(botToken);
	}
	
	@Override
	public void createConnection(String token) {
		botToken=token;
		telegramBot=new TelegramBot(token);
	}
	
	@Override
	public SocialResponse<PublishResponse> sendMessage(String chatId, String messageText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		if(!SocialUtil.checkString(chatId) || !SocialUtil.checkString(messageText))
			return socialResponse.error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//Needed to get the proper link to the message
		boolean notPublic=false;
		if(isChatIdFromUrl(chatId)) 
			notPublic=true;
		
		//Check if the chat is private
		chatId=manageChatId(chatId);
		
		//Build message
		SendMessage sendMessage=new SendMessage(chatId, messageText)
		        .parseMode(ParseMode.HTML)
		        .disableWebPagePreview(false)
		        .disableNotification(true); //With this the notification will be silent
	
		//Send message
		SendResponse sendResponse=telegramBot.execute(sendMessage);
		
		//Check errors
		if(SocialUtil.checkString(sendResponse.description()))
			return getErrorResponse(sendResponse);
		
		//Build successful response
		socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
			.setData(new PublishResponse(getMessageUrl(sendResponse, notPublic)));
		
		return socialResponse;
	}
	
	@Override
	public SocialResponse<PublishResponse> sendPhoto(String chatId, String imageUrl, String messageText) {
		if(!SocialUtil.checkString(chatId) || !SocialUtil.checkString(imageUrl))
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			SocialUtil.saveMedia(imageUrl);	
			
			//Get image's path
			URL url=new URL(imageUrl);
			String fileName=url.getFile();
			String imageName=fileName.substring(fileName.lastIndexOf("/"));
			String imageFilePath=SocialUtil.PATH + imageName;
			
			return sendPhotoOnChannelOrGroup(chatId, imageFilePath, messageText);
		} 
		catch(IOException e) {
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage);
	}
	
	private SocialResponse<PublishResponse> sendPhotoOnChannelOrGroup(String chatId, String imageFilePath, String messageText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Needed to get the proper link to the message
		boolean notPublic=false;
		if(isChatIdFromUrl(chatId)) 
			notPublic=true;
		
		//Check if the chat is private
		chatId=manageChatId(chatId);

		String imageFormat="";
		try {
			//Find image's format
			imageFormat=SocialUtil.getImageFormat(imageFilePath);
		}
		catch(RuntimeException e) {
			return socialResponse.error(SocialResponse.CODE, e.getMessage());
		}
		
		//Convert image to byte array
		byte[] imageAsBytes=SocialUtil.fetchBytesFromImage(imageFilePath, imageFormat);
		
		//If a message was not specified, it's not necessary to send it
		if(messageText==null)
			messageText="";
		
		//Build message
		SendPhoto sendPhoto=new SendPhoto(chatId, imageAsBytes)
				.caption(messageText)
				.parseMode(ParseMode.HTML)
				.disableNotification(true); //With this the notification will be silent
		
		//Send message
		SendResponse sendResponse=telegramBot.execute(sendPhoto);
		
		//Check errors
		if(SocialUtil.checkString(sendResponse.description()))
			return getErrorResponse(sendResponse);
		
		//Build successful response
		socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
			.setData(new PublishResponse(getMessageUrl(sendResponse, notPublic)));
		
		return socialResponse;
	}
	
	@Override
	public SocialResponse<PublishResponse> sendVideo(String chatId, String videoUrl, String messageText) {
		if(!SocialUtil.checkString(chatId) || !SocialUtil.checkString(videoUrl))
			return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, PublishResponse.FAIL_MESSAGE);
		
		//Build error message
		String errorMessage=PublishResponse.FAIL_MESSAGE;
		
		try {
			SocialUtil.saveMedia(videoUrl);	
			
			//Get image's path
			URL url=new URL(videoUrl);
			String fileName=url.getFile();
			String videoName=fileName.substring(fileName.lastIndexOf("/"));
			String videoFilePath=SocialUtil.PATH + videoName;
			
			return sendVideoOnChannelOrGroup(chatId, videoFilePath, messageText);
		} 
		catch(IOException e) {
			errorMessage=e.getMessage();
		}
		
		return new SocialResponse<PublishResponse>().error(SocialResponse.CODE, errorMessage);
	}
	
	private SocialResponse<PublishResponse> sendVideoOnChannelOrGroup(String chatId, String videoFilePath, String messageText) {
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();
		
		//Needed to get the proper link to the message
		boolean notPublic=false;
		if(isChatIdFromUrl(chatId)) 
			notPublic=true;
		
		//Check if the chat is private
		chatId=manageChatId(chatId);
		
		//Convert image to byte array
		byte[] videoAsBytes=SocialUtil.fetchBytesFromVideo(videoFilePath);
		
		//If a message was not specified, it's not necessary to send it
		if(messageText==null)
			messageText="";
		
		//Build message
		SendVideo sendVideo=new SendVideo(chatId, videoAsBytes)
				.caption(messageText)
				.parseMode(ParseMode.HTML)
				.disableNotification(true); //With this the notification will be silent
		
		//Send message
		SendResponse sendResponse=telegramBot.execute(sendVideo);
		
		//Check errors
		if(SocialUtil.checkString(sendResponse.description()))
			return getErrorResponse(sendResponse);
		
		//Build successful response
		socialResponse.success(SocialResponse.CODE, PublishResponse.SUCCES_MESSAGE)
			.setData(new PublishResponse(getMessageUrl(sendResponse, notPublic)));
		
		return socialResponse;
	}
	
	@Override
	public String manageChatId(String chatId) {
		if(isChatIdFromUrl(chatId)) {
			//If the id is passed by a message's url 
			if(idFromMessageUrl(chatId))
				return obtainChatIdFromMessageUrl(chatId);
			else
				return obtainChatId(chatId);
		}
		
		return chatId;
	}

	@Override
	public boolean isChatIdFromUrl(String chatId) {
		return SocialUtil.contains(chatId, SocialUtil.HTTPS) || SocialUtil.contains(chatId, SocialUtil.HTTP);
	}
	
	//Check if the id is given by a message url
	private boolean idFromMessageUrl(String chatId) {
		return SocialUtil.contains(chatId, TelegramService.TELEGRAM_ME_PRIVATE)
				|| SocialUtil.contains(chatId, TelegramService.TELEGRAM_ME);
	}
	
	//Build the id given a chat url in case of private chat
	private String obtainChatId(String chatId) {		
		//If it's a public channel or group
		if(SocialUtil.contains(chatId, TelegramService.TELEGRAM_PUBLIC_CHANNEL_AND_GROUP_PREFIX)) {
			return chatId.substring(chatId.lastIndexOf(TelegramService.TELEGRAM_PUBLIC_CHANNEL_AND_GROUP_PREFIX));
		}
		else { //If it's a private channel or group
			//If it's a group
			if(!SocialUtil.contains(chatId, "_")) {
				chatId=chatId.replace(TelegramService.TELEGRAM_DOMAIN + "#/im?p=", "");
				
				//This is needed in order to remove the url's prefix for private groups
				chatId=chatId.substring(1);
					
				//Add the prefix for group
				return TelegramService.TELEGRAM_PRIVATE_GROUP_PREFIX + chatId;
			}
			else {
				//If it's a channel
				chatId=chatId.replace(TelegramService.TELEGRAM_DOMAIN + "#/im?p=", "");
				
				//This is needed in order to remove the url's prefix for private channels
				chatId=chatId.substring(1);
				chatId=chatId.substring(0, chatId.lastIndexOf("_"));
					
				//Add the prefix for channel
				return TelegramService.TELEGRAM_PRIVATE_CHANNEL_PREFIX + chatId;
			}
		} //End if it's a private channel or group
	}
	
	//Build the id given a message url in case of private chat
	private String obtainChatIdFromMessageUrl(String chatId) {
		//If it's a private channel, this kind of methos cannot be used for private groups
		if(SocialUtil.contains(chatId, TelegramService.TELEGRAM_ME_PRIVATE)) {
			//Get the id
			chatId=chatId.substring(0, chatId.lastIndexOf("/"));
			chatId=chatId.substring(chatId.lastIndexOf("/")+1);
			
			//Add the prefix for private channel
			return TelegramService.TELEGRAM_PRIVATE_CHANNEL_PREFIX + chatId;
		}
		else {
			//Get the id
			chatId=chatId.substring(0, chatId.lastIndexOf("/"));
			chatId=chatId.substring(chatId.lastIndexOf("/")+1);
			
			//Add the prefix for public channel or group
			return TelegramService.TELEGRAM_PUBLIC_CHANNEL_AND_GROUP_PREFIX + chatId;
		}
	}
	
	//Get the url of a message sent to Telegram
	private String getMessageUrl(SendResponse sendResponse, boolean notPublic) {
		StringBuilder messageUrlBuilder=new StringBuilder();
				
		//Build the url in case it's a public chat
		messageUrlBuilder.append(TelegramService.TELEGRAM_ME)
			.append(sendResponse.message().chat().username())
			.append("/")
			.append(sendResponse.message().messageId());
		
		if(notPublic) {
			//Clear the builder
			messageUrlBuilder.delete(0, messageUrlBuilder.length());
			
			//Get the chat's id
			String chatId=sendResponse.message().chat().id().toString();
			if(SocialUtil.contains(chatId, TelegramService.TELEGRAM_PRIVATE_CHANNEL_PREFIX)) {
				//4 because the channels' prefix need to be omitted
				chatId=chatId.substring(4);
			}
			else {
				//1 because the grouos' prefix need to be omitted
				chatId=chatId.substring(1);
			}
			
			//Build the url in case it is not a public chat
			messageUrlBuilder.append(TelegramService.TELEGRAM_ME_PRIVATE)
				.append(chatId)
				.append("/")
				.append(sendResponse.message().messageId());			
		}
		
		return messageUrlBuilder.toString();
	}
	
	//Return the proper error response get by Telegram
	private SocialResponse<PublishResponse> getErrorResponse(SendResponse sendResponse){
		//Create the response
		SocialResponse<PublishResponse> socialResponse=new SocialResponse<PublishResponse>();

		//Check the specific error in order to customize it
		if(sendResponse.description().equalsIgnoreCase(TELEGRAM_BOT_NOT_FOUND))
			return socialResponse.error(SocialResponse.CODE, "Bot " + TELEGRAM_BOT_NOT_FOUND);
		else
			return socialResponse.error(SocialResponse.CODE, sendResponse.description());
	}
	
	@Override
	public TelegramBot getTelegramBot() {
		return telegramBot;
	}
	
	@Override
	public void setTelegramBot(TelegramBot telegramBot) {
		this.telegramBot=telegramBot;
	}
	
	@Override	
	public String getBotToken() {
		return botToken;
	}
	
	@Override
	public void setBotToken(String botToken) {
		this.botToken=botToken;
	}
	
	private TelegramBot telegramBot;
	private String botToken;
	
	//Specific errors for Telegram, needed in order to customize the error because it is not clear
	public static final String TELEGRAM_BOT_NOT_FOUND="not found";
}
