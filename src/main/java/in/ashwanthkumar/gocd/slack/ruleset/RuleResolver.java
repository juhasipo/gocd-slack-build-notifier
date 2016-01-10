package in.ashwanthkumar.gocd.slack.ruleset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.util.HttpRequestUtil;
import in.ashwanthkumar.gocd.slack.util.JSONUtils;
import in.ashwanthkumar.gocd.slack.util.StatusUtils;
import in.ashwanthkumar.utils.lang.option.Option;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class RuleResolver {

    private static Logger LOGGER = Logger.getLoggerFor(RuleResolver.class);

    private HttpRequestUtil httpRequestUtil;

    public RuleResolver(HttpRequestUtil httpRequestUtil) {
        this.httpRequestUtil = httpRequestUtil;
    }

    public Rules resolvePipelineRule(Rules defaultRules, String currentPipeline, String currentStage)
            throws URISyntaxException, IOException
    {
        URL url = new URL(goPipelinesUrl(defaultRules.getGoServerHost(), currentPipeline));
        JsonElement rootElement = httpRequestUtil.fetchPipelineConfig(defaultRules, url);

        JsonObject json = rootElement.getAsJsonObject();
        JsonArray pipelineParams = json.get("parameters").getAsJsonArray();

        Map<String, String> properties = JSONUtils.jsonArrayToMap(pipelineParams);
        String slackChannel = properties.get("go_slack_channel");
        String slackStatuses = properties.get("go_slack_statuses");
        String stageRegex = Option.option(properties.get("go_slack_stages")).getOrElse(currentStage);

        PipelineRule pipelineRule = new PipelineRule()
                .setNameRegex(currentPipeline)
                .setStageRegex(stageRegex)
                .setStatus(StatusUtils.statusStringToSet(slackStatuses))
                .setChannel(slackChannel);

        LOGGER.info(String.format("Resolved rules for pipeline %s: %s", currentPipeline, pipelineRule.toString()));

        return new Rules()
                .setGoPassword(defaultRules.getGoPassword())
                .setGoLogin(defaultRules.getGoLogin())
                .setGoServerHost(defaultRules.getGoServerHost())
                .setWebHookUrl(defaultRules.getWebHookUrl())
                .setSlackUserIcon(defaultRules.getSlackUserIcon())
                .setEnabled(defaultRules.isEnabled())
                .setSlackChannel(slackChannel)
                .setPipelineRules(Collections.singletonList(pipelineRule));
    }

    String goPipelinesUrl(String host, String pipeline) throws URISyntaxException {
        return new URI(String.format("%s/go/api/admin/pipelines/%s", host, pipeline)).normalize().toASCIIString();
    }

}
