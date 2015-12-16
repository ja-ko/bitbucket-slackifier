package eu.kandru.jko.bitbucket.slackifier.slack.messages;

import com.atlassian.bitbucket.avatar.AvatarRequest;
import com.atlassian.bitbucket.avatar.AvatarService;
import com.atlassian.bitbucket.event.pull.PullRequestParticipantStatusUpdatedEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.pull.PullRequestAction;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequestTaskSearchRequest;
import com.atlassian.bitbucket.task.TaskCount;
import com.atlassian.bitbucket.task.TaskState;
import com.atlassian.bitbucket.user.Person;
import com.atlassian.sal.api.component.ComponentLocator;
import com.ullink.slack.simpleslackapi.SlackAttachment;

public class PreparedPullRequestApprovalMessage implements SlackPreparedMessage {

  private PullRequestParticipantStatusUpdatedEvent event;
  private NavBuilder navBuilder;

  public PreparedPullRequestApprovalMessage(PullRequestParticipantStatusUpdatedEvent event, NavBuilder navBuilder) {
    super();
    this.event = event;
    this.navBuilder = navBuilder;
  }

  @Override
  public String getMessage() {
    return "";
  }

  @Override
  public SlackAttachment getAttachment() {
    SlackAttachment attachment = new SlackAttachment();
    String text;
    String color;
    if (event.getAction() == PullRequestAction.APPROVED) {
      text = event.getParticipant().getUser().getDisplayName() + " hat den Pullrequest genehmigt.";
      color = SlackColorCodes.COLOR_APPROVED;
    } else if (event.getAction() == PullRequestAction.UNAPPROVED) {
      text = "Die Genehmigung wurde von " + event.getParticipant().getUser().getDisplayName() + " zurückgezogen.";
      color = SlackColorCodes.COLOR_UNAPPROVED;
    } else
      return null;
    String title = "PullRequest Genehmigung";
    if (event.getAction() == PullRequestAction.UNAPPROVED)
      title += " zurückgezogen";
    attachment.addMiscField("author_name", event.getUser().getDisplayName());
    attachment.addMiscField("author_icon", getAvatar(event.getUser()));
    attachment.setFallback(text);
    attachment.setColor(color);
    attachment.setTitle(title);
    attachment.setTitleLink(navBuilder.repo(event.getPullRequest().getToRef().getRepository())
        .pullRequest(event.getPullRequest().getId()).overview().buildAbsolute());
    attachment.setText(text);
    long numApproved = event.getPullRequest().getReviewers().stream().filter(reviewer -> reviewer.isApproved()).count();
    long numReviewers = event.getPullRequest().getReviewers().size();
    attachment.addField("Genehmigt", numApproved + "/" + numReviewers, true);
    TaskCount tasks = getTaskCount();
    attachment.addField("Offene Tasks", Long.toString(tasks.getCount(TaskState.OPEN)), true);
    return attachment;
  }

  private TaskCount getTaskCount() {
    PullRequestTaskSearchRequest request = new PullRequestTaskSearchRequest.Builder(event.getPullRequest()).build();
    return ComponentLocator.getComponent(PullRequestService.class).countTasks(request);
  }

  private String getAvatar(Person user) {
    AvatarService avatarService = ComponentLocator.getComponent(AvatarService.class);
    AvatarRequest request = new AvatarRequest(true, 32, true);
    return avatarService.getUrlForPerson(user, request);
  }
}
