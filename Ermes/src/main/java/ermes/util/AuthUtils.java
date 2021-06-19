package ermes.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

public class AuthUtils {

    /*
    * Returns the error message if an error occurred while authenticating,
    * otherwise it returns null to signal that there wasn't any error.
    */
    public static String isAuthOrErrorMessage(HttpServletRequest request) {
        String authErrorMessage = (String) request.getAttribute(AUTH_ERROR);
        if (StringUtils.isNotEmpty(authErrorMessage))
            return authErrorMessage;

        return null;
    }

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

    public static final String AUTH_ERROR = "error";
}
