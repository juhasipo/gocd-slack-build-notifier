package in.ashwanthkumar.gocd.slack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.DefaultHttpRequestUtil;
import in.ashwanthkumar.gocd.slack.util.HttpRequestUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class GoNotificationMessage {
    private Logger LOG = Logger.getLoggerFor(GoNotificationMessage.class);

    private final HttpRequestUtil httpRequestUtil;

    public GoNotificationMessage() {
        httpRequestUtil = new DefaultHttpRequestUtil();
    }

    public GoNotificationMessage(HttpRequestUtil httpRequestUtil) {
        this.httpRequestUtil = httpRequestUtil;
    }

    /**
     * Raised when we can't find information about our build in the array
     * returned by the server.
     */
    static public class BuildDetailsNotFoundException extends Exception {
        public BuildDetailsNotFoundException(String pipelineName,
                                             int pipelineCounter)
        {
            super(String.format("could not find details for %s/%d",
                                pipelineName, pipelineCounter));
        }
    }

    static class Stage {
        @SerializedName("name")
        private String name;

        @SerializedName("counter")
        private String counter;

        @SerializedName("state")
        private String state;

        @SerializedName("result")
        private String result;

        @SerializedName("create-time")
        private String createTime;

        @SerializedName("last-transition-time")
        private String lastTransitionTime;
    }

    static class Pipeline {
        @SerializedName("name")
        private String name;

        @SerializedName("counter")
        private String counter;

        @SerializedName("stage")
        private Stage stage;
    }

    @SerializedName("pipeline")
    private Pipeline pipeline;


    public String goServerUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, pipeline.name, pipeline.counter, pipeline.stage.name, pipeline.stage.counter)).normalize().toASCIIString();
    }

    public String goHistoryUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/api/pipelines/%s/history", host, pipeline.name)).normalize().toASCIIString();
    }

    public String fullyQualifiedJobName() {
        return pipeline.name + "/" + pipeline.counter + "/" + pipeline.stage.name + "/" + pipeline.stage.counter;
    }

    public String getPipelineName() {
        return pipeline.name;
    }

    public String getPipelineCounter() {
        return pipeline.counter;
    }

    public String getStageName() {
        return pipeline.stage.name;
    }

    public String getStageCounter() {
        return pipeline.stage.counter;
    }

    public String getStageState() {
        return pipeline.stage.state;
    }

    public String getStageResult() {
        return pipeline.stage.result;
    }

    public String getCreateTime() {
        return pipeline.stage.createTime;
    }

    public String getLastTransitionTime() {
        return pipeline.stage.lastTransitionTime;
    }

    public JsonObject fetchDetailsForBuild(Rules rules, int counter)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        URL url = new URL(goHistoryUrl(rules.getGoServerHost()));
        JsonArray history = httpRequestUtil.fetchRecentPipelineHistory(rules, url);
        // Search through the builds in our recent history, and hope that
        // we can find the build we want.
        for (int i = 0, size = history.size(); i < size; i++) {
            JsonObject build = history.get(i).getAsJsonObject();
            if (build.get("counter").getAsInt() == counter)
                return build;
        }
        throw new BuildDetailsNotFoundException(getPipelineName(), counter);
    }

    public void tryToFixStageResult(Rules rules)
    {
        String currentStatus = pipeline.stage.state.toUpperCase();
        String currentResult = pipeline.stage.result.toUpperCase();
        if (currentStatus.equals("BUILDING") && currentResult.equals("UNKNOWN")) {
            pipeline.stage.result = "BUILDING";
            return;
        }
        // We only need to double-check certain messages; the rest are
        // trusty-worthy.
        if (!currentResult.equals("PASSED") && !currentResult.equals("FAILED"))
            return;

        // Fetch our previous build.  If we can't get it, just give up;
        // this is a low-priority tweak.
        JsonObject previous = null;
        int wanted = Integer.parseInt(getPipelineCounter()) - 1;
        try {
            previous = fetchDetailsForBuild(rules, wanted);
        } catch(Exception e) {
            LOG.warn(String.format("Error getting previous build: " +
                                   e.getMessage()));
            return;
        }

        // Figure out whether the previous stage passed or failed.
        JsonArray stages = previous.get("stages").getAsJsonArray();
        JsonObject lastStage = stages.get(stages.size() - 1).getAsJsonObject();

        String previousResult = "";
        // If a multi-stage pipeline fails at not-the-last stage, then the last
        // stage will not have run, and its result will be null
        if (lastStage.get("result") != null) {
            previousResult = lastStage.get("result").getAsString().toUpperCase();
        }

        // Fix up our build status.  This is slightly asymmetrical, because
        // we want to be quicker to praise than to blame.  Also, I _think_
        // that the typical representation of stageResult is initial caps
        // only, but our callers should all be using toUpperCase on it in
        // any event.
        //LOG.info("current: "+currentResult + ", previous: "+previousResult);
        if (currentResult.equals("PASSED") && !previousResult.equals("PASSED"))
            pipeline.stage.result = "Fixed";
        else if (currentResult.equals("FAILED") &&
                 previousResult.equals("PASSED"))
            pipeline.stage.result = "Broken";
    }

    public JsonObject fetchDetails(Rules rules)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        return fetchDetailsForBuild(rules, Integer.parseInt(getPipelineCounter()));
    }
}
