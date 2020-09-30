package ermes.facebook;

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
import com.restfb.exception.FacebookOAuthException;
import com.restfb.scope.ScopeBuilder;
import ermes.facebook.FacebookService.FacebookServicePermission;
import ermes.util.SocialUtil;

@Configuration
public class FacebookFilter implements Filter {
	private static final Logger logger=LogManager.getLogger(FacebookFilter.class);
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Entro nel filtro");

		//Request, response and session management
		HttpServletRequest httpRequest=(HttpServletRequest) request;
		HttpServletResponse httpResponse=(HttpServletResponse) response;
		HttpSession session=httpSessionFactory.getObject();
		
		//Build the callback url
		String callbackUrl=domain + facebook.getServiceName(httpRequest.getRequestURL(), FacebookService.FACEBOOK_ID);
	
		try {
			//If authentication or token are needed
			if(!facebook.verifyCode(httpRequest.getParameter(FacebookService.FACEBOOK_CODE))) {
				//Store request parameters for multiple redirections
				SocialUtil.storeParameters(session, httpRequest);
					
				logger.debug("Reindirizzamento a Facebook");
					
				httpResponse.sendRedirect(facebook.getAuthUrl(httpRequest.getParameter(FacebookService.FACEBOOK_KEY), 
						httpRequest.getParameter(FacebookService.FACEBOOK_SECRET), callbackUrl, new ScopeBuilder()));				
			}
			else { //First else
				//Get the code from Facebook
				String code=httpRequest.getParameter(FacebookService.FACEBOOK_CODE);
					
				logger.debug("Ottengo il codice");
					
				//Get parameters in order to create connection with Facebook
				String sessionKey=(String) session.getAttribute(FacebookService.FACEBOOK_KEY);
				String sessionSecret=(String) session.getAttribute(FacebookService.FACEBOOK_SECRET);
				String sessionPermissions=(String) session.getAttribute(FacebookService.FACEBOOK_PERMISSIONS);
					
				logger.debug("Creo la connessione");
					
				//Create connection with Facebook
				facebook.createConnection(sessionKey, sessionSecret, callbackUrl, code);
								
				//Permission management
				FacebookServicePermission permissions=facebook.managePermission(sessionPermissions);
					
				//If the user has not the needed permissions request them, else continue
				if(!facebook.verifyPermissions(permissions)) {
					logger.debug("Richiedo permessi");
						
					httpResponse.sendRedirect(facebook.requestPermissions(sessionKey, sessionSecret, callbackUrl, permissions));
				}
				else { //Second else
					//Pass the parameters needed to the service
					SocialUtil.setResponseAttribute(session, httpRequest);
					
					logger.debug("Esco dal filtro");
						
					//Invalidate the session before leaving
					session.invalidate();

					chain.doFilter(httpRequest, httpResponse);
				} //End second else
			} //End first else
		} //End Access Token verification
		catch(FacebookOAuthException | IllegalArgumentException | NullPointerException e) {			
			httpRequest.setAttribute(FacebookService.FACEBOOK_ERROR, e.getMessage());
			
			//Invalidate the session before leaving
			session.invalidate();
			
			chain.doFilter(httpRequest, httpResponse);
		}
	}
	
	@Autowired
	private FacebookService facebook;
	
	@Autowired
	ObjectFactory<HttpSession> httpSessionFactory;
	
	@Value("${ermes.domain}")
	private String domain;
	
	public static final String FACEBOOK_FILTER="/facebook/*";
}