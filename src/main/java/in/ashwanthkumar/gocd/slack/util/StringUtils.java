package in.ashwanthkumar.gocd.slack.util;

public class StringUtils {

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean notEmpty(String value) {
        return !isEmpty(value);
    }

}
