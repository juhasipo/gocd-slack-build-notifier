package in.ashwanthkumar.gocd.slack;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.ruleset.RuleResolver;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.JSONUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static in.ashwanthkumar.utils.lang.StringUtils.isEmpty;
import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);

    public static final String EXTENSION_TYPE = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String REQUEST_NOTIFICATION_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String REQUEST_NOTIFICATION_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String REQUEST_NOTIFICATION_CONFIGURATION_VIEW = "go.plugin-settings.get-view";
    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    public static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";

    private GoApplicationAccessor goApplicationAccessor;

    public GoNotificationPlugin() {
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATION_CONFIGURATION)) {
            return handleNotificationConfig();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATION_CONFIGURATION_VIEW)) {
            try {
                return handleSCMView();
            } catch (IOException e) {
                String message = "Failed to find template: " + e.getMessage();
                return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, message);
            }
        } else if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATION_VALIDATE_CONFIGURATION)) {
            return handleValidatePluginSettingsConfiguration(goPluginApiRequest);
        }
        return renderJSON(404, null);
    }

    private GoPluginApiResponse handleNotificationConfig() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("serverhost", createField("Server Host", null, false, true, false, "0"));
        response.put("webhookurl", createField("Webhook URL", null, true, false, false, "1"));
        response.put("displayname", createField("Display Name", null, false, true, false, "2"));
        response.put("iconurl", createField("Icon URL", null, false, false, false, "3"));
        response.put("defaultchannel", createField("Default Channel", null, false, false, false, "5"));
        response.put("adminusername", createField("Admin Username", null, false, false, false, "6"));
        response.put("adminpassword", createField("Admin Password", null, false, false, true, "7"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleSCMView() throws IOException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("displayValue", "Slack");
        response.put("template", getFileContents("/notification.template.html"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    public Rules getDefaultRules() {
        Map<String, Object> responseBodyMap = getRulesFromGo();
        return new Rules()
                .setEnabled(false)
                .setGoLogin((String)responseBodyMap.get("adminusername"))
                .setGoPassword((String)responseBodyMap.get("adminpassword"))
                .setWebHookUrl((String)responseBodyMap.get("webhookurl"))
                .setSlackDisplayName((String)responseBodyMap.get("displayname"))
                .setSlackUserIcon((String)responseBodyMap.get("iconurl"))
                .setGoServerHost((String)responseBodyMap.get("serverhost"));
    }

    private Map<String, Object> getRulesFromGo() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", "slack.notifier");
        GoApiResponse response = goApplicationAccessor.submit(createGoApiRequest(GET_PLUGIN_SETTINGS, JSONUtils.toJSON(requestMap)));

        return response.responseBody() == null ?
                new HashMap<String, Object>() :
                (Map<String, Object>) JSONUtils.fromJSON(response.responseBody());
    }

    private String getFileContents(String filePath) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(filePath), "UTF-8");
    }

    private GoApiRequest createGoApiRequest(final String api, final String responseBody) {
        return new GoApiRequest() {
            @Override
            public String api() {
                return api;
            }

            @Override
            public String apiVersion() {
                return "1.0";
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return pluginIdentifier();
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return responseBody;
            }
        };
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isPartOfIdentity, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<String, Object>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("part-of-identity", isPartOfIdentity);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, goSupportedVersions);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        GoNotificationMessage message = parseNotificationMessage(goPluginApiRequest);
        int responseCode = SUCCESS_RESPONSE_CODE;

        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            Rules defaultRules = getDefaultRules();

            RuleResolver resolver = new RuleResolver(new Server(defaultRules));
            Rules pipelineRules = resolver.resolvePipelineRule(
                    defaultRules,
                    message.getPipelineName(),
                    message.getStageName()
            );

            response.put("status", "success");
            LOGGER.info(message.fullyQualifiedJobName() + " has " + message.getStageState() + "/" + message.getStageResult());
            pipelineRules.getPipelineListener().notify(message);
        } catch (Exception e) {
            LOGGER.info(message.fullyQualifiedJobName() + " failed with error", e);
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            if (!isEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private GoNotificationMessage parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), GoNotificationMessage.class);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }
}
