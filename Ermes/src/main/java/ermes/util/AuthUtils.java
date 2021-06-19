package ermes.util;

import ermes.twitter.TwitterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

public class AuthUtils {

    public static String isAuthOrErrorMessage(HttpServletRequest request) {
        String errorMessage = "";
        if ((errorMessage = (String) request.getAttribute(TwitterService.TWITTER_ERROR)) != null)
            return errorMessage;

        return null;
    }

    // Store parameters in session
    public static void storeParameters(HttpSession session, HttpServletRequest httpRequest) {
        Enumeration<String> parametersNames = httpRequest.getParameterNames();
        while (parametersNames.hasMoreElements()) {
            String parameterName = parametersNames.nextElement();

            session.setAttribute(parameterName, httpRequest.getParameter(parameterName));
        }
    }

    public static void retrieveStoredParameters(HttpSession session, HttpServletRequest httpRequest) {
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();

            httpRequest.setAttribute(attributeName, session.getAttribute(attributeName));
        }
    }

    public static void passParametersToService(HttpServletRequest httpRequest) {
        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();

            httpRequest.setAttribute(paramName, httpRequest.getParameter(paramName));
        }
    }
}
