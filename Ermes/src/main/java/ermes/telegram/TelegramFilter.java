package ermes.telegram;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ermes.util.ErmesUtil;

@Configuration
public class TelegramFilter implements Filter {
	private static final Logger logger=LogManager.getLogger(TelegramFilter.class);
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Entro nel filtro");
		
		//Request and response management
		HttpServletRequest httpRequest=(HttpServletRequest) request;
		HttpServletResponse httpResponse=(HttpServletResponse) response;
		
		logger.debug("Gestisco connessione");

		//Get the token from the request
		String botToken=httpRequest.getParameter(TelegramService.TELEGRAM_TOKEN);

		//Check if the token is possibly valid, at least
		if(!ErmesUtil.checkString(botToken)) {
			logger.debug("Token passato non valido");

			httpRequest.setAttribute(TelegramService.TELEGRAM_ERROR, TelegramService.TELEGRAM_NOT_VALID_BOT_TOKEN);
		}
		else {	
			if(!telegram.verifyBotToken() || !telegram.verifyBotInfo(botToken)) {
				logger.debug("Nuovo bot oppure Token non valido");
				
				//Create the bot
				telegram.createConnection(botToken);
				
				logger.debug("Bot Token: " + telegram.getBotToken());
			}
			
			//Pass the needed parameters to the service
			ErmesUtil.manageRequest(httpRequest);
		}
		
		logger.debug("Esco dal filtro");

		chain.doFilter(httpRequest, httpResponse);
	}
	
	@Autowired
	private TelegramService telegram;
	
	public static final String TELEGRAM_FILTER="/telegram/*";
}
