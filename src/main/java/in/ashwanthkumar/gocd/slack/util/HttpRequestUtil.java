package in.ashwanthkumar.gocd.slack.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import java.io.IOException;
import java.net.URL;

public interface HttpRequestUtil {

    JsonElement fetchPipelineConfig(Rules rules, URL url) throws IOException;

    JsonArray fetchRecentPipelineHistory(Rules rules, URL url) throws IOException;
}
