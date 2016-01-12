package in.ashwanthkumar.gocd.slack.ruleset;

import in.ashwanthkumar.gocd.slack.PipelineListener;
import in.ashwanthkumar.gocd.slack.SlackPipelineListener;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.ArrayList;
import java.util.List;

public class Rules {
    private boolean enabled;
    private String webHookUrl;
    private String slackChannel;
    private String slackDisplayName;
    private String slackUserIconURL;
    private String goServerHost;
    private String goLogin;
    private String goPassword;

    private List<PipelineRule> pipelineRules = new ArrayList<PipelineRule>();

    public boolean isEnabled() {
        return enabled;
    }

    public Rules setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public Rules setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
        return this;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public Rules setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public String getSlackDisplayName() {
        return slackDisplayName;
    }

    public Rules setSlackDisplayName(String displayName) {
        this.slackDisplayName = displayName;
        return this;
    }

    public String getSlackUserIcon() {
        return slackUserIconURL;
    }

    public Rules setSlackUserIcon(String iconURL) {
        this.slackUserIconURL = iconURL;
        return this;
    }

    public List<PipelineRule> getPipelineRules() {
        return pipelineRules;
    }

    public Rules setPipelineRules(List<PipelineRule> pipelineRules) {
        this.pipelineRules = pipelineRules;
        return this;
    }

    public String getGoServerHost() {
        return goServerHost;
    }

    public Rules setGoServerHost(String goServerHost) {
        this.goServerHost = goServerHost;
        return this;
    }

    public String getGoLogin() {
        return goLogin;
    }

    public Rules setGoLogin(String goLogin) {
        this.goLogin = goLogin;
        return this;
    }

    public String getGoPassword() {
        return goPassword;
    }

    public Rules setGoPassword(String goPassword) {
        this.goPassword = goPassword;
        return this;
    }

    public PipelineListener resolvePipelineListener() {
        return new SlackPipelineListener(this);
    }

    public Option<PipelineRule> find(final String pipeline, final String stage, final String pipelineStatus) {
        return Lists.find(pipelineRules, new Predicate<PipelineRule>() {
            public Boolean apply(PipelineRule input) {
                return input.matches(pipeline, stage, pipelineStatus);
            }
        });
    }

}
