package eu.kandru.jko.bitbucket.slackifier.slack.messages;

import com.atlassian.bitbucket.avatar.AvatarRequest;
import com.atlassian.bitbucket.avatar.AvatarService;
import com.atlassian.bitbucket.event.pull.PullRequestEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequestTaskSearchRequest;
import com.atlassian.bitbucket.task.TaskCount;
import com.atlassian.bitbucket.task.TaskState;
import com.atlassian.bitbucket.user.Person;
import com.atlassian.sal.api.component.ComponentLocator;
import com.ullink.slack.simpleslackapi.SlackAttachment;

public class PreparedPullRequestMessage implements SlackPreparedMessage {

  private PullRequestEvent event;
  private String message;
  private NavBuilder navBuilder;

  public PreparedPullRequestMessage(PullRequestEvent event, String message, NavBuilder navBuilder) {
    this.event = event;
    this.message = message;
    this.navBuilder = navBuilder;
  }

  @Override
  public String getMessage() {
    return "";
  }

  @Override
  public SlackAttachment getAttachment() {
    SlackAttachment attachment = new SlackAttachment();
    PullRequest pr = event.getPullRequest();
    attachment.addMiscField("author_name", event.getUser().getDisplayName());
    attachment.addMiscField("author_icon", getAvatar(event.getUser()));
    attachment.setFallback(message);
    attachment.setTitle(pr.getTitle());
    attachment.setTitleLink(
        navBuilder.repo(pr.getToRef().getRepository()).pullRequest(pr.getId()).overview().buildAbsolute());
    attachment.setText(pr.getDescription());
    attachment.setColor(SlackColorCodes.COLOR_DEFAULT);
    attachment.addField("Von", pr.getFromRef().getDisplayId(), true);
    attachment.addField("Nach", pr.getToRef().getDisplayId(), true);
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
