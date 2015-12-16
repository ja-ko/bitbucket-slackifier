package eu.kandru.jko.bitbucket.slackifier.slack.messages;

import com.atlassian.bitbucket.avatar.AvatarRequest;
import com.atlassian.bitbucket.avatar.AvatarService;
import com.atlassian.bitbucket.event.pull.PullRequestDeclinedEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.user.Person;
import com.atlassian.sal.api.component.ComponentLocator;
import com.ullink.slack.simpleslackapi.SlackAttachment;

public class PreparedPullRequestDeclinedMessage implements SlackPreparedMessage {

  private PullRequestDeclinedEvent event;
  private NavBuilder navBuilder;

  public PreparedPullRequestDeclinedMessage(PullRequestDeclinedEvent event, NavBuilder navBuilder) {
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
    String text = "Der Pullrequest wurde abgelehnt. Channel wird archiviert.";
    attachment.addMiscField("author_name", event.getUser().getDisplayName());
    attachment.addMiscField("author_icon", getAvatar(event.getUser()));
    attachment.setFallback(text);
    attachment.setColor(SlackColorCodes.COLOR_DECLINED);
    attachment.setTitle("Pullrequest abgelehnt");
    attachment.setTitleLink(navBuilder.repo(event.getPullRequest().getToRef().getRepository())
        .pullRequest(event.getPullRequest().getId()).overview().buildAbsolute());
    attachment.setText(text);
    return attachment;
  }

  private String getAvatar(Person user) {
    AvatarService avatarService = ComponentLocator.getComponent(AvatarService.class);
    AvatarRequest request = new AvatarRequest(true, 32, true);
    return avatarService.getUrlForPerson(user, request);
  }
}
