package in.ashwanthkumar.gocd.slack.ruleset;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class RulesReader {
    private Logger LOG = Logger.getLoggerFor(RulesReader.class);

    private static Rules read() {
        return new RulesReader().load();
    }

    private static Rules read(File file) {
        return new RulesReader().load(file);
    }

    private static Rules read(String file) {
        return new RulesReader().load(ConfigFactory.parseResources(file));
    }

    private Rules load(Config config) {
        Config configWithFallback = config.withFallback(ConfigFactory.load(getClass().getClassLoader()));
        return Rules.fromConfig(configWithFallback.getConfig("gocd.slack"));
    }

    private Rules load() {
        return load(ConfigFactory.load());
    }

    private Rules load(File file) {
        return load(ConfigFactory.parseFile(file));
    }
}
