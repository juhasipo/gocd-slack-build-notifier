package in.ashwanthkumar.gocd.slack.jsonapi;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

public interface ServerFactory {

    Server getServer(Rules rules);

}
