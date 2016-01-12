package in.ashwanthkumar.gocd.slack.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class FileUtils {

    public static final String FILE_ENCODING = "UTF-8";

    public static String getFileContents(String filePath) throws IOException {
        return IOUtils.toString(FileUtils.class.getResourceAsStream(filePath), FILE_ENCODING);
    }

}
