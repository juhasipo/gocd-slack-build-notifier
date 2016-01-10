# gocd-slack-build-notifier
Slack based GoCD build notifier

![Demo](images/gocd-slack-notifier-demo.png)

## Setup
Download jar from releases & place it in /plugins/external & restart Go Server.

## Configuration

Configuration is done by adding pipeline parameters to the pipelines which should use Slack.
General and default configurations are in plugin settings. Pipeline configurations are
set via pipeline parameters.

General and default configurations:

- `Admin Username` - Login for a Go user who is authorized to access the REST API.
- `Admin Password` - Password for the user specified above. You might want to create a less privileged user for this plugin.
- `Server Host` - FQDN of the Go Server. All links on the slack channel will be relative to this host.
- `Webhook URL` - Slack Webhook URL
- `Icon URL` - Message icon URL
- `Default Channel` - Override the default channel where we should send the notifications in slack. You can also give a value starting with `@` to send it to any specific user.

Pipeline configurations:

- `go_slack_channel` - If not defined, default channel is used. Prefix with `#` for channels, `@` for users.
- `go_slack_statuses` - Pipe (`|`) separated list of case-insensitive statuses. `building|failed|passed`
- `go_slack_stages` - Regex to match the stage name

## License

http://www.apache.org/licenses/LICENSE-2.0
