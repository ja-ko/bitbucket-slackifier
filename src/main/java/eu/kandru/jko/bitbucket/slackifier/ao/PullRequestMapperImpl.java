package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.ullink.slack.simpleslackapi.SlackChannel;

import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;

public class PullRequestMapperImpl implements PullRequestMapper {
  private ActiveObjects ao;
  private PullRequestService pullRequestService;
  private SlackService slackService;

  public PullRequestMapperImpl(ActiveObjects ao, PullRequestService pullRequestService, SlackService slackService) {
    this.ao = ao;
    this.pullRequestService = pullRequestService;
    this.slackService = slackService;
  }

  @Override
  public SlackChannel getChannel(PullRequest request) {
    MappedPullRequest[] mappedRequests = ao.find(MappedPullRequest.class, "REPOSITORY_ID = ? AND PULL_REQUEST_ID = ?",
        request.getToRef().getRepository().getId(), request.getId());
    if (mappedRequests.length == 0)
      return null;
    return slackService.getChannelById(mappedRequests[0].getChannelId());
  }

  @Override
  public PullRequest getPullRequest(SlackChannel channel) {
    MappedPullRequest[] mappedRequests = ao.find(MappedPullRequest.class, "CHANNEL_ID = ?", channel.getId());
    if (mappedRequests.length == 0)
      return null;
    return pullRequestService.getById(mappedRequests[0].getRepositoryId(), mappedRequests[0].getPullRequestId());
  }

  @Override
  public void createMapping(PullRequest pr, SlackChannel channel) {
    if (getPullRequest(channel) != null || getChannel(pr) != null)
      return;

    MappedPullRequest mapping = ao.create(MappedPullRequest.class);
    mapping.setRepositoryId(pr.getToRef().getRepository().getId());
    mapping.setPullRequestId(pr.getId());
    mapping.setChannelId(channel.getId());
    mapping.save();
  }
}
