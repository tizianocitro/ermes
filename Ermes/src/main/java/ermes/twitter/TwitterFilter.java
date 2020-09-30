package ermes.twitter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ermes.util.SocialUtil;
import twitter4j.TwitterException;

@Configuration
public class TwitterFilter implements Filter {
	private static final Logger logger=LogManager.getLogger(TwitterFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Entro nel filtro");

		//Request, response and session management
		HttpServletRequest httpRequest=(HttpServletRequest) request;
		HttpServletResponse httpResponse=(HttpServletResponse) response;
		HttpSession session=httpSessionFactory.getObject();

		//Build the callback url
		String callbackUrl=domain + twitter.getServiceName(httpRequest.getRequestURL(), TwitterService.TWITTER_ID);
	
		try {
			//If authentication or token are needed
			if(!twitter.verifyConnectionParameters(httpRequest.getParameter(TwitterService.TWITTER_TOKEN), 
					httpRequest.getParameter(TwitterService.TWITTER_VERIFIER))) {
		
				//Store request parameters for multiple redirections
				SocialUtil.storeParameters(session, httpRequest);
					
				logger.debug("Reindirizzamento a Twitter");
					
				httpResponse.sendRedirect(twitter.getAuthUrl(httpRequest.getParameter(TwitterService.TWITTER_KEY), 
						httpRequest.getParameter(TwitterService.TWITTER_SECRET), callbackUrl));				
			}
			else { //First else
				//Obtain the verifier from Twitter
				String verifier=httpRequest.getParameter(TwitterService.TWITTER_VERIFIER);
					
				logger.debug("Creo la connessione");
					
				twitter.configAccessToken(verifier);
						
				//Pass needed parameters to the service
				SocialUtil.setResponseAttribute(session, httpRequest);
					
				logger.debug("Esco dal filtro");
					
				//Invalidate the session before leaving
				session.invalidate();

				chain.doFilter(httpRequest, httpResponse);
			} //End first else	
		} //End try
		catch(TwitterException | IllegalStateException e) {			
			httpRequest.setAttribute(TwitterService.TWITTER_ERROR, SocialUtil.format(e.getMessage()));
			
			//Invalidate the session before leaving
			session.invalidate();
			
			chain.doFilter(httpRequest, httpResponse);
		}
	}
	
	@Autowired
	private TwitterService twitter;
	
	@Autowired
	ObjectFactory<HttpSession> httpSessionFactory;
	
	@Value("${ermes.domain}")
	private String domain;
	
	public static final String TWITTER_FILTER="/twitter/*";
}
