package ermes.util;

import ermes.twitter.TwitterService;

import javax.servlet.http.HttpServletRequest;

public class AuthUtils {

    public static String isAuthOrErrorMessage(HttpServletRequest request) {
        String errorMessage = "";
        if ((errorMessage = (String) request.getAttribute(TwitterService.TWITTER_ERROR)) != null)
            return errorMessage;

        return null;
    }
}
