package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationMessageTest {

    private static final String PIPELINE_NAME = "pipeline";

    private static Pipeline pipeline(String name, int counter) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        return pipeline;
    }

    private static GoNotificationMessage.PipelineInfo info(String name, int counter) {
        GoNotificationMessage.PipelineInfo pipeline = new GoNotificationMessage.PipelineInfo();
        pipeline.counter = "10";
        pipeline.name = PIPELINE_NAME;
        return pipeline;
    }

    @Test
    public void shouldFetchPipelineDetails() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[] {
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9),
                pipeline(PIPELINE_NAME, 10),
                pipeline(PIPELINE_NAME, 11),
                pipeline(PIPELINE_NAME, 12)
        };
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        Pipeline result = message.fetchDetails(new Rules());

        assertThat(result.name, is(PIPELINE_NAME));
        assertThat(result.counter, is(10));
    }

    @Test(expected = GoNotificationMessage.BuildDetailsNotFoundException.class)
    public void shouldFetchPipelineDetailsNotFound() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[] {
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9)
        };
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        message.fetchDetails(new Rules());
    }

    @Test(expected = GoNotificationMessage.BuildDetailsNotFoundException.class)
    public void shouldFetchPipelineDetailsNothingFound() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[] {
                pipeline("something-different", 10)
        };
        when(server.getPipelineHistory("something-different")).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                mockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        message.fetchDetails(new Rules());
    }


    private ServerFactory mockServerFactory(final Server server) {
        return new ServerFactory() {
            @Override
            public Server getServer(Rules rules) {
                return server;
            }
        };
    }

}