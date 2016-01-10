package in.ashwanthkumar.gocd.slack.util;

import com.google.gson.*;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultHttpRequestUtil implements HttpRequestUtil {

    private static Logger LOGGER = Logger.getLoggerFor(DefaultHttpRequestUtil.class);

    @Override
    public JsonElement fetchPipelineConfig(Rules rules, URL url) throws IOException {
        LOGGER.info("Fetching current pipeline config");

        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        addCredentials(rules, request);

        request.setRequestProperty("Accept", "application/vnd.go.cd.v1+json");

        request.connect();

        JsonParser parser = new JsonParser();
        return parser.parse(new InputStreamReader((InputStream) request.getContent()));
    }

    @Override
    public JsonArray fetchRecentPipelineHistory(Rules rules, URL url)
            throws IOException
    {
            // Based on
            // https://github.com/matt-richardson/gocd-websocket-notifier/blob/master/src/main/java/com/matt_richardson/gocd/websocket_notifier/PipelineDetailsPopulator.java
            // http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java

            HttpURLConnection request = (HttpURLConnection) url.openConnection();

            addCredentials(rules, request);

            request.connect();

            JsonParser parser = new JsonParser();
            JsonElement rootElement = parser.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject json = rootElement.getAsJsonObject();
            return json.get("pipelines").getAsJsonArray();
    }

    private void addCredentials(Rules rules, HttpURLConnection request) {
        // Add in our HTTP authorization credentials if we have them.
        String username = rules.getGoLogin();
        String password = rules.getGoPassword();
        if (StringUtils.notEmpty(username) && StringUtils.notEmpty(password)) {
            String userpass = username + ":" + password;
            String basicAuth = "Basic "
                    + DatatypeConverter.printBase64Binary(userpass.getBytes());
            request.setRequestProperty("Authorization", basicAuth);
        }
    }
}
