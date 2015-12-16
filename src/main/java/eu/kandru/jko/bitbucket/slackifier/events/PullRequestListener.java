package eu.kandru.jko.bitbucket.slackifier.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.event.pull.PullRequestCommentEvent;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestMergedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantApprovedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantUnapprovedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantsUpdatedEvent;
import com.atlassian.bitbucket.event.pull.PullRequestUpdatedEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.event.api.EventListener;
import com.ullink.slack.simpleslackapi.SlackChannel;

import eu.kandru.jko.bitbucket.slackifier.ao.LinkedUserService;
import eu.kandru.jko.bitbucket.slackifier.ao.PullRequestMapper;
import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.PreparedPullRequestApprovalMessage;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.PreparedPullRequestCommentMessage;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.PreparedPullRequestDeclinedMessage;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.PreparedPullRequestMessage;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.SlackPreparedMessage;

public class PullRequestListener {
  private SlackService slackService;
  private NavBuilder navBuilder;
  private LinkedUserService linkedUserService;
  private PullRequestMapper pullRequestMapper;

  private static final Logger LOG = LoggerFactory.getLogger(PullRequestListener.class);

  public PullRequestListener(SlackService slackService, NavBuilder navBuilder, LinkedUserService linkedUserService,
      PullRequestMapper pullRequestMapper) {
    this.slackService = slackService;
    this.navBuilder = navBuilder;
    this.linkedUserService = linkedUserService;
    this.pullRequestMapper = pullRequestMapper;
  }

  private String getChannelName(PullRequest pr) {
    String repositoryName = pr.getToRef().getRepository().getName();
    return "PR-" + repositoryName + "-" + pr.getId();
  }

  @EventListener
  public void pullRequestCreated(PullRequestOpenedEvent event) {
    PullRequest pr = event.getPullRequest();
    String channelName = getChannelName(pr);
    SlackChannel channel = slackService.createChannel(channelName);
    if (channel == null)
      return;

    pullRequestMapper.createMapping(pr, channel);

    pr.getReviewers().forEach(
        participant -> slackService.inviteToChannel(channel, linkedUserService.getSlackUser(participant.getUser())));
    slackService.inviteToChannel(channel, linkedUserService.getSlackUser(pr.getAuthor().getUser()));

    SlackPreparedMessage message =
        new PreparedPullRequestMessage(event, "@channel: Ein neuer Pull-Request wurde erstellt", navBuilder);
    slackService.sendMessage(channel, message);
    LOG.info("pull request channel created");
  }

  @EventListener
  public void pullRequestApproved(PullRequestParticipantApprovedEvent event) {
    SlackPreparedMessage message = new PreparedPullRequestApprovalMessage(event, navBuilder);
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    slackService.sendMessage(channel, message);
    LOG.info("pull request approved message sent");
  }

  @EventListener
  public void pullRequestParticipantsUpdated(PullRequestParticipantsUpdatedEvent event) {
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    event.getAddedParticipants().stream().map(participant -> linkedUserService.getSlackUser(participant))
        .filter(check -> check != null).forEach(user -> slackService.inviteToChannel(channel, user));
    LOG.info("pull request participants updated");
  }

  @EventListener
  public void pullRequestUpdated(PullRequestUpdatedEvent event) {
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    SlackPreparedMessage message =
        new PreparedPullRequestMessage(event, "Die Zusammenfassung des Pullrequests wurde verändert.", navBuilder);
    slackService.sendMessage(channel, message);
    LOG.info("pull request update message sent");
  }

  @EventListener
  public void pullRequestUnapproved(PullRequestParticipantUnapprovedEvent event) {
    SlackPreparedMessage message = new PreparedPullRequestApprovalMessage(event, navBuilder);
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    slackService.sendMessage(channel, message);
    LOG.info("pull request unapprove message sent");
  }

  @EventListener
  public void pullRequestDeclined(PullRequestDeclinedEvent event) {
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    SlackPreparedMessage message = new PreparedPullRequestDeclinedMessage(event, navBuilder);
    slackService.sendMessage(channel, message);
    slackService.archiveChannel(channel);
    LOG.info("pull request declined. channel archived.");
  }

  @EventListener
  public void pullRequestMerged(PullRequestMergedEvent event) {
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    SlackPreparedMessage message = new PreparedPullRequestMessage(event,
        "Der Pullrequest wurde von " + event.getUser().getDisplayName() + " gemergt.", navBuilder);

    slackService.sendMessage(channel, message);
    slackService.archiveChannel(channel);
    LOG.info("pull request merged. channel archived");
  }

  @EventListener
  public void pullRequestCommented(PullRequestCommentEvent event) {
    SlackChannel channel = pullRequestMapper.getChannel(event.getPullRequest());
    if (channel == null)
      return;
    SlackPreparedMessage message = new PreparedPullRequestCommentMessage(event, navBuilder);
    slackService.sendMessage(channel, message);
    LOG.info("pull request comment message sent");
  }
}
