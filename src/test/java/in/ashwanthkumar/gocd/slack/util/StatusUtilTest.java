package in.ashwanthkumar.gocd.slack.util;

import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StatusUtilTest {

    @Test
    public void testStatusStringToSet() throws Exception {
        Set<PipelineStatus> statuses = StatusUtils.statusStringToSet("building|passed");

        Set<PipelineStatus> expected = new HashSet<PipelineStatus>(Arrays.asList(PipelineStatus.BUILDING, PipelineStatus.PASSED));
        assertThat(statuses, is(expected));
    }

    @Test
    public void testStatusStringToSetEmptyInMiddle() throws Exception {
        Set<PipelineStatus> statuses = StatusUtils.statusStringToSet("building||passed");

        Set<PipelineStatus> expected = new HashSet<PipelineStatus>(Arrays.asList(PipelineStatus.BUILDING, PipelineStatus.PASSED));
        assertThat(statuses, is(expected));
    }

    @Test
    public void testStatusStringToSetNone() throws Exception {
        Set<PipelineStatus> statuses = StatusUtils.statusStringToSet("");

        Set<PipelineStatus> expected = new HashSet<PipelineStatus>();
        assertThat(statuses, is(expected));
    }

    @Test
    public void testStatusStringToSetNull() throws Exception {
        Set<PipelineStatus> statuses = StatusUtils.statusStringToSet(null);

        Set<PipelineStatus> expected = new HashSet<PipelineStatus>();
        assertThat(statuses, is(expected));
    }

}