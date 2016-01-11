package in.ashwanthkumar.gocd.slack.util;

import in.ashwanthkumar.utils.lang.option.Option;

public class Options {

    public static <T> Option<T> empty() {
        return Option.option(null);
    }

}
