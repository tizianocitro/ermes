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

import ermes.util.AuthUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.scope.ScopeBuilder;
import ermes.facebook.FacebookService.FacebookServicePermission;

@Configuration
public class FacebookFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(FacebookFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        logger.debug("Entro nel filtro");

        // Request, response and session management
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpSessionFactory.getObject();

        // Build the callback url
        String callbackUrl = domain + facebook.getServiceName(httpRequest.getRequestURL(), FacebookService.FACEBOOK_ID);
        try {
            // Check if the access token is given
            if (facebook.isTokenGiven(httpRequest.getParameter(FacebookService.FACEBOOK_ACCESS_TOKEN))) {
                logger.debug("Creo la connessione con l'access token ricevuto");

                // Create connection with Facebook with a given access token
                facebook.createConnection(httpRequest.getParameter(FacebookService.FACEBOOK_ACCESS_TOKEN));

                // Check if the token is not valid or expired
                facebook.isTokenExpiredOrNotValid();

                // Permission management
                FacebookServicePermission permissions =
                        facebook.managePermissions(httpRequest.getParameter(FacebookService.FACEBOOK_PERMISSIONS));

                //If the user has not the needed permissions return an error
                if (!facebook.verifyPermissions(permissions)) {
                    logger.debug("Permessi revocati");

                    // Set the error message
                    httpRequest.setAttribute(FacebookService.FACEBOOK_ERROR, PERMISSIONS_REVOKED);

                    // Pass the needed parameters to the service
                    AuthUtils.passParametersToService(httpRequest);

                    // Invalidate the session before leaving
                    session.invalidate();

                    logger.debug("Esco dal filtro a causa di permessi revocati");

                    chain.doFilter(httpRequest, httpResponse);
                } else { // End permissions' check
                    // Pass the needed parameters to the service
                    AuthUtils.passParametersToService(httpRequest);

                    // Invalidate the session before leaving
                    session.invalidate();

                    logger.debug("Esco dal filtro con access token ricevuto");

                    chain.doFilter(httpRequest, httpResponse);
                }
            } else { // End if access token is given
                // If authentication is needed
                if (!facebook.verifyCode(httpRequest.getParameter(FacebookService.FACEBOOK_CODE))) {
                    // Check the error in case of access denied
                    String accessDenied = httpRequest.getParameter(FacebookService.FACEBOOK_ERROR);
                    if (facebook.verifyPermissionsDenied(accessDenied)) {
                        logger.debug("Permessi rifiutati");

                        // Set the error message
                        httpRequest.setAttribute(FacebookService.FACEBOOK_ERROR, accessDenied);

                        // Pass the needed parameters to the service
                        AuthUtils.passParametersToService(httpRequest);

                        // Invalidate the session before leaving
                        session.invalidate();

                        logger.debug("Esco dal filtro a causa di permessi rifiutati");

                        chain.doFilter(httpRequest, httpResponse);
                    } else {
                        // Store request parameters for multiple redirections
                        AuthUtils.storeParameters(session, httpRequest);

                        logger.debug("Reindirizzamento a Facebook");

                        httpResponse.sendRedirect(
                                facebook.getAuthUrl(httpRequest.getParameter(FacebookService.FACEBOOK_KEY),
                                httpRequest.getParameter(FacebookService.FACEBOOK_SECRET), callbackUrl, new ScopeBuilder()));
                    }
                } else { // First else
                    // Get the code from Facebook
                    String code = httpRequest.getParameter(FacebookService.FACEBOOK_CODE);

                    logger.debug("Ho ottenuto il codice da Facebook");

                    // Get parameters in order to create connection with Facebook
                    String sessionKey = (String) session.getAttribute(FacebookService.FACEBOOK_KEY);
                    String sessionSecret = (String) session.getAttribute(FacebookService.FACEBOOK_SECRET);
                    String sessionPermissions = (String) session.getAttribute(FacebookService.FACEBOOK_PERMISSIONS);

                    logger.debug("Creo la connessione");

                    // Create connection with Facebook
                    facebook.createConnection(sessionKey, sessionSecret, callbackUrl, code);

                    // Permission management
                    FacebookServicePermission permissions = facebook.managePermissions(sessionPermissions);

                    // If the user has not the needed permissions request them, else continue
                    if (!facebook.verifyPermissions(permissions)) {
                        logger.debug("Richiedo permessi");

                        httpResponse.sendRedirect(
                                facebook.requestPermissions(sessionKey, sessionSecret, callbackUrl, permissions));
                    } else { // Second else
                        // Pass the parameters needed to the service
                        AuthUtils.retrieveStoredParameters(session, httpRequest);

                        // Invalidate the session before leaving
                        session.invalidate();

                        logger.debug("Esco dal filtro");

                        chain.doFilter(httpRequest, httpResponse);
                    } // End second else
                } // End first else
            } // End access token check
        } catch (FacebookOAuthException | IllegalArgumentException | NullPointerException e) {
            httpRequest.setAttribute(FacebookService.FACEBOOK_ERROR, e.getMessage());

            // Invalidate the session before leaving
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

    public static final String FACEBOOK_FILTER = "/facebook/*";

    public static final String PERMISSIONS_REVOKED = "Permissions revoked, check if the user revoked the needed permissions";
}