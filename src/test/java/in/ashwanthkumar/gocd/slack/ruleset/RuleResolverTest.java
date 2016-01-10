package in.ashwanthkumar.gocd.slack.ruleset;

import com.google.gson.JsonParser;
import in.ashwanthkumar.gocd.slack.util.HttpRequestUtil;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleResolverTest {

    @Test
    public void shouldFetchRules() throws Exception {
        HttpRequestUtil httpRequestUtil = mock(HttpRequestUtil.class);
        RuleResolver resolver = new RuleResolver(httpRequestUtil);

        when(httpRequestUtil.fetchPipelineConfig(any(Rules.class), any(URL.class)))
            .thenReturn(new JsonParser().parse(ALL_PARAMS));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "stage");

        assertThat(rules, notNullValue());
        assertThat(rules.getPipelineRules().size(), is(1));

        PipelineRule pipelineRule = rules.getPipelineRules().get(0);

        assertThat(pipelineRule.getStatus().size(), is(4));

        PipelineRule foundRules = rules.find("pipeline", "stage", "failed").get();
        assertThat(foundRules.getChannel(), is("test-channel"));
        assertThat(foundRules.getNameRegex(), is("pipeline"));
        assertThat(foundRules.getStageRegex(), is("stage.*"));
    }

    @Test
    public void shouldFetchRulesDisabledForStage() throws Exception {
        HttpRequestUtil httpRequestUtil = mock(HttpRequestUtil.class);
        RuleResolver resolver = new RuleResolver(httpRequestUtil);

        when(httpRequestUtil.fetchPipelineConfig(any(Rules.class), any(URL.class)))
                .thenReturn(new JsonParser().parse(DISABLED_FOR_STAGE));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "current-stage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "current-stage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(true));
    }

    @Test
    public void shouldFetchRulesDisabledForPipeline() throws Exception {
        HttpRequestUtil httpRequestUtil = mock(HttpRequestUtil.class);
        RuleResolver resolver = new RuleResolver(httpRequestUtil);

        when(httpRequestUtil.fetchPipelineConfig(any(Rules.class), any(URL.class)))
                .thenReturn(new JsonParser().parse(DISABLED_FOR_PIPELINE));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "current-stage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "current-stage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(false));
    }

    @Test
    public void shouldFetchRulesNoStatuses() throws Exception {
        HttpRequestUtil httpRequestUtil = mock(HttpRequestUtil.class);
        RuleResolver resolver = new RuleResolver(httpRequestUtil);

        when(httpRequestUtil.fetchPipelineConfig(any(Rules.class), any(URL.class)))
                .thenReturn(new JsonParser().parse(NO_STATUSES));

        Rules defaultRules = new Rules()
                .setGoServerHost("http://localhost");
        Rules rules = resolver.resolvePipelineRule(defaultRules, "pipeline", "defaultStage");

        assertThat(rules, notNullValue());

        assertThat(rules.find("pipeline", "defaultStage", "failed").isDefined(), is(false));
        assertThat(rules.find("pipeline", "other-stage", "failed").isDefined(), is(false));
    }


    private static final String ALL_PARAMS = "{\n" +
            "    \"_links\": {\n" +
            "        \"self\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/Test\"\n" +
            "        },\n" +
            "        \"doc\": {\n" +
            "            \"href\": \"http://api.go.cd/#pipeline_config\"\n" +
            "        },\n" +
            "        \"find\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/:name\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"label_template\": \"${COUNT}\",\n" +
            "    \"enable_pipeline_locking\": false,\n" +
            "    \"name\": \"Test\",\n" +
            "    \"template\": null,\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"name\": \"go_slack_channel\",\n" +
            "            \"value\": \"test-channel\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_statuses\",\n" +
            "            \"value\": \"failed|broken|fixed|building\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_stages\",\n" +
            "            \"value\": \"stage.*\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"environment_variables\": [],\n" +
            "    \"materials\": [],\n" +
            "    \"stages\": [\n" +
            "        {\n" +
            "            \"name\": \"defaultStage\",\n" +
            "            \"environment_variables\": [\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"tracking_tool\": null,\n" +
            "    \"timer\": null\n" +
            "}\n";

    private static final String DISABLED_FOR_PIPELINE = "{\n" +
            "    \"_links\": {\n" +
            "        \"self\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/Test\"\n" +
            "        },\n" +
            "        \"doc\": {\n" +
            "            \"href\": \"http://api.go.cd/#pipeline_config\"\n" +
            "        },\n" +
            "        \"find\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/:name\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"label_template\": \"${COUNT}\",\n" +
            "    \"enable_pipeline_locking\": false,\n" +
            "    \"name\": \"Test\",\n" +
            "    \"template\": null,\n" +
            "    \"parameters\": [\n" +
            "    ],\n" +
            "    \"environment_variables\": [],\n" +
            "    \"materials\": [],\n" +
            "    \"stages\": [\n" +
            "        {\n" +
            "            \"name\": \"defaultStage\",\n" +
            "            \"environment_variables\": [\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"tracking_tool\": null,\n" +
            "    \"timer\": null\n" +
            "}\n";

    private static final String DISABLED_FOR_STAGE = "{\n" +
            "    \"_links\": {\n" +
            "        \"self\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/Test\"\n" +
            "        },\n" +
            "        \"doc\": {\n" +
            "            \"href\": \"http://api.go.cd/#pipeline_config\"\n" +
            "        },\n" +
            "        \"find\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/:name\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"label_template\": \"${COUNT}\",\n" +
            "    \"enable_pipeline_locking\": false,\n" +
            "    \"name\": \"Test\",\n" +
            "    \"template\": null,\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"name\": \"go_slack_channel\",\n" +
            "            \"value\": \"test-channel\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_statuses\",\n" +
            "            \"value\": \"failed|broken|fixed|building\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_stages\",\n" +
            "            \"value\": \"other-stage\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"environment_variables\": [],\n" +
            "    \"materials\": [],\n" +
            "    \"stages\": [\n" +
            "        {\n" +
            "            \"name\": \"defaultStage\",\n" +
            "            \"environment_variables\": [\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"tracking_tool\": null,\n" +
            "    \"timer\": null\n" +
            "}\n";

    private static final String NO_STATUSES = "{\n" +
            "    \"_links\": {\n" +
            "        \"self\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/Test\"\n" +
            "        },\n" +
            "        \"doc\": {\n" +
            "            \"href\": \"http://api.go.cd/#pipeline_config\"\n" +
            "        },\n" +
            "        \"find\": {\n" +
            "            \"href\": \"http://localhost:8155/go/api/admin/pipelines/:name\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"label_template\": \"${COUNT}\",\n" +
            "    \"enable_pipeline_locking\": false,\n" +
            "    \"name\": \"Test\",\n" +
            "    \"template\": null,\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"name\": \"go_slack_channel\",\n" +
            "            \"value\": \"test-channel\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_statuses\",\n" +
            "            \"value\": \"\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"go_slack_stages\",\n" +
            "            \"value\": \"defaultStage\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"environment_variables\": [],\n" +
            "    \"materials\": [],\n" +
            "    \"stages\": [\n" +
            "        {\n" +
            "            \"name\": \"defaultStage\",\n" +
            "            \"environment_variables\": [\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"tracking_tool\": null,\n" +
            "    \"timer\": null\n" +
            "}\n";

}