package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationMessageTest {

    public static final String TEST_PIPELINE_NAME = "pipeline-name";
    public static final String TEST_STAGE_NAME = "stage-name";

    private ServerFactory mockServerFactory(final Server server) {
        return new ServerFactory() {
            @Override
            public Server getServer(Rules rules) {
                return server;
            }
        };
    }

    @Test
    public void shouldReturnBuilding() throws Exception {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                        stage(TEST_STAGE_NAME, 1, "failed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(
                        pipeline(TEST_PIPELINE_NAME, 1),
                        stage(TEST_STAGE_NAME, 2, "building")
                )
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("building"));
    }

    @Test
    public void shouldReturnFixed() throws Exception {
        Server server = mock(Server.class);

        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                                stage(TEST_STAGE_NAME, 1, "failed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(
                        pipeline(TEST_PIPELINE_NAME, 1),
                        stage(TEST_STAGE_NAME, 2, "Passed")
                )
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("Fixed"));
    }

    @Test
    public void shouldReturnFixedManyStages() throws Exception {
        Server server = mock(Server.class);

        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                        stage("other-stage-name-1", 1, "Failed"),
                        stage(TEST_STAGE_NAME, 1, "Failed"),
                        stage("other-stage-name-2", 1, "Failed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(pipeline(TEST_PIPELINE_NAME, 1), stage(TEST_STAGE_NAME, 4, "Passed"))
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("Fixed"));
    }

    @Test
    public void shouldReturnBroken() throws Exception {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                        stage(TEST_STAGE_NAME, 1, "passed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(
                        pipeline(TEST_PIPELINE_NAME, 1),
                        stage(TEST_STAGE_NAME, 2, "Failed")
                )
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("Broken"));
    }

    @Test
    public void shouldReturnBrokenPastPipelinePassed() throws Exception {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                        stage(TEST_STAGE_NAME, 1, "passed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(
                        pipeline(TEST_PIPELINE_NAME, 2),
                        stage(TEST_STAGE_NAME, 2, "Failed")
                )
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("Broken"));
    }

    @Test
    public void shouldReturnFixedPastPipelineFailed() throws Exception {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(TEST_PIPELINE_NAME))
                .thenReturn(history(pipeline(TEST_PIPELINE_NAME, 1,
                        stage(TEST_STAGE_NAME, 1, "Failed")
                )));

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                pipelineStatus(
                        pipeline(TEST_PIPELINE_NAME, 2),
                        stage(TEST_STAGE_NAME, 2, "Passed")
                )
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is("Fixed"));
    }

    private History history(Pipeline... pipelines) {
        History history = new History();
        history.pipelines = pipelines;
        return history;
    }

    private Pipeline pipeline(String name, int counter, Stage... stages) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        pipeline.stages = stages;

        return pipeline;
    }

    private Stage stage(String name, int counter, String status) {
        Stage stage = new Stage();
        stage.name = name;
        stage.counter = counter;
        stage.result = status;
        return stage;
    }

    private GoNotificationMessage.PipelineInfo pipelineStatus(Pipeline p, Stage s) {
        GoNotificationMessage.PipelineInfo info = new GoNotificationMessage.PipelineInfo();
        info.name = p.name;
        info.counter = Integer.toString(p.counter);
        info.stage = new GoNotificationMessage.StageInfo();
        info.stage.counter = Integer.toString(s.counter);
        info.stage.name = s.name;
        info.stage.state = s.result;
        info.stage.result = s.result;
        return info;
    }

}