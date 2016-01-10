package in.ashwanthkumar.gocd.slack.util;

import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StatusUtils {

    public static Set<PipelineStatus> statusStringToSet(String slackStatuses) {
        if (slackStatuses == null || slackStatuses.trim().isEmpty()) {
            return Collections.emptySet();
        }

        Set<PipelineStatus> statuses = new HashSet<PipelineStatus>();
        for (String pipelineStatus : slackStatuses.split("\\|")) {
            if (!pipelineStatus.isEmpty()) {
                statuses.add(PipelineStatus.valueOf(pipelineStatus.toUpperCase()));
            }
        }
        return statuses;
    }

}
