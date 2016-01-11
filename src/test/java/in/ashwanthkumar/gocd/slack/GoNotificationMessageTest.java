package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class GoNotificationMessageTest {

    public static final String PIPELINE_NAME = "PL";
    public static final String STAGE_NAME = "STG";

    private History pipelineHistory;
    private GoNotificationMessage.PipelineInfo pipeline;
    private String expectedStatus;

    public GoNotificationMessageTest(History pipelineHistory, GoNotificationMessage.PipelineInfo pipeline, String expectedStatus) {
        this.pipelineHistory = pipelineHistory;
        this.pipeline = pipeline;
        this.expectedStatus = expectedStatus;
    }

    private ServerFactory mockServerFactory(final Server server) {
        return new ServerFactory() {
            @Override
            public Server getServer(Rules rules) {
                return server;
            }
        };
    }

    @Parameterized.Parameters(name = "Test #{index}: Pipeline {1} should return correct status {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{
                given(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "building"))),
                thenExpectStatus("building")
        }, {
                given(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "failed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                given(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), "Failed"),
                        stage(STAGE_NAME,           counter(1), "Failed"),
                        stage("other-stage-name-2", counter(1), "Failed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                given(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "Failed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "Passed"))),
                thenExpectStatus("Fixed")
        }, {
                given(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), "Failed"))),
                thenExpectStatus("Broken")
        }, {
                given(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), "passed"))),
                whenFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), "Failed"))),
                thenExpectStatus("Broken")
        }
        });
    }

    @Test
    public void test() throws IOException {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipeline
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is(expectedStatus));
    }

    private static History given(Pipeline... pipelines) {
        History history = new History();
        history.pipelines = pipelines;
        return history;
    }

    private static Pipeline pipeline(String name, int counter, Stage... stages) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        pipeline.stages = stages;

        return pipeline;
    }

    private static Stage stage(String name, int counter, String status) {
        Stage stage = new Stage();
        stage.name = name;
        stage.counter = counter;
        stage.result = status;
        return stage;
    }

    private static GoNotificationMessage.PipelineInfo whenFinished(Pipeline pipeline) {
        GoNotificationMessage.PipelineInfo info = new GoNotificationMessage.PipelineInfo();
        info.name = pipeline.name;
        info.counter = Integer.toString(pipeline.counter);
        info.stage = new GoNotificationMessage.StageInfo();

        Stage stage = pipeline.stages[0];
        info.stage.counter = Integer.toString(stage.counter);
        info.stage.name = stage.name;
        info.stage.state = stage.result;
        info.stage.result = stage.result;
        return info;
    }

    private static String thenExpectStatus(String value) {
        return value;
    }

    private static int counter(int value) {
        return value;
    }

}