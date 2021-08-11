package ermes.twitter;

public class TwitterUtils {

    // Useful to get a better looking error message,
    // it's coded for Twitter bad looking error message.
    public static String format(String twitterMessage) {
        if (twitterMessage.contains("\n")) {
            twitterMessage = twitterMessage.replaceAll("\n", "");
        }

        if (twitterMessage.contains(".message -")) {
            twitterMessage = twitterMessage.replaceAll(".message -", ". Message:");
        }

        if (twitterMessage.contains(".code -")) {
            twitterMessage = twitterMessage.replaceAll(".code -", ". Code: ");
        }

        return twitterMessage;
    }
}
