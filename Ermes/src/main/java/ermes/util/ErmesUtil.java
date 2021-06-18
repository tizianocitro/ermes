package ermes.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ErmesUtil {

    // Save media on specified path
    public static void saveMedia(String mediaUrl) throws IOException {
        URL url = new URL(mediaUrl);
        String fileName = url.getFile();
        String format = fileName.substring(fileName.lastIndexOf("/") + 1);

        // Where to save the media
        String destination = PATH + format;

        InputStream inputStream = url.openStream();
        OutputStream outputStream = new FileOutputStream(destination);

        byte[] b = new byte[2048];
        int length;

        while ((length = inputStream.read(b)) != -1)
            outputStream.write(b, 0, length);

        inputStream.close();
        outputStream.close();
    }

    public static byte[] fetchBytesFromImage(String imageFile, String format) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(imageFile));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, format, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Return the image's format
    public static String getImageFormat(String imageFile) throws RuntimeException {
        File file = new File(imageFile);
        ImageInputStream imageInputStream;

        try {
            imageInputStream = ImageIO.createImageInputStream(file);

            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
            if (!iterator.hasNext())
                throw new RuntimeException(READER_ERROR + " " + imageFile);

            ImageReader reader = iterator.next();

            return reader.getFormatName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] fetchBytesFromVideo(String videoFile) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int n;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(videoFile));

            while (-1 != (n = fileInputStream.read(buffer)))
                baos.write(buffer, 0, n);

            fileInputStream.close();

            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Return the video's format
    public static String getVideoFormat(String videoPath) {
        return videoPath.substring(videoPath.lastIndexOf("."));
    }

    // Store parameters in session
    public static void storeParameters(HttpSession session, HttpServletRequest httpRequest) {
        Enumeration<String> parametersNames = httpRequest.getParameterNames();
        while (parametersNames.hasMoreElements()) {
            String parameterName = parametersNames.nextElement();

            session.setAttribute(parameterName, httpRequest.getParameter(parameterName));
        }
    }

    public static void manageRequest(HttpSession session, HttpServletRequest httpRequest) {
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();

            httpRequest.setAttribute(attributeName, session.getAttribute(attributeName));
        }
    }

    public static void manageRequest(HttpServletRequest httpRequest) {
        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();

            httpRequest.setAttribute(paramName, httpRequest.getParameter(paramName));
        }
    }

    // Equivalent to StringUtils.isNotEmpty() (it must be changed)
    public static boolean checkString(String check) {
        return check != null && !check.equals("");
    }

    public static boolean contains(String text, String containsText) {
        Pattern pattern = Pattern.compile(containsText, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        return matcher.find();
    }

    // Get the url from a string
    public static String getSubstring(String text, String containsText) {
        Pattern pattern = Pattern.compile(containsText, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String substring = text.substring(matcher.start());

            if (contains(substring, " "))
                return substring.substring(0, substring.indexOf(" "));
            else
                return substring;
        }

        return "";
    }

    /*
    * The path for saving media on Eclipse it's ./src/main/resources/static/media/
    * and in log4j2.properties change it to appender.file.fileName=./log/ermes.log
    */
    public static final String PATH = "Ermes/src/main/resources/static/media/";

    // Exception's message
    public static final String READER_ERROR = "Nessun ImageReader trovato, controlla la correttezza del file";

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
}
