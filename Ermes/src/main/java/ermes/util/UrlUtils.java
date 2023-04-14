package ermes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {

    public static boolean contains(String text, String containsText) {
        Pattern pattern = Pattern.compile(containsText, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        return matcher.find();
    }

    // Get the url from a string
    public static String getUrlAsContainedSubstring(String text, String containsText) {
        Pattern pattern = Pattern.compile(containsText, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String substring = text.substring(matcher.start());
            if (contains(substring, " ")) {
                return substring.substring(0, substring.indexOf(" "));
            }

            return substring;
        }

        return "";
    }

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
}
