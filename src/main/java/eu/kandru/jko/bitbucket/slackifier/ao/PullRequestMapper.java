package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.bitbucket.pull.PullRequest;
import com.ullink.slack.simpleslackapi.SlackChannel;

@Transactional
public interface PullRequestMapper {
  SlackChannel getChannel(PullRequest request);

  PullRequest getPullRequest(SlackChannel channel);

  void createMapping(PullRequest pr, SlackChannel channel);
}
