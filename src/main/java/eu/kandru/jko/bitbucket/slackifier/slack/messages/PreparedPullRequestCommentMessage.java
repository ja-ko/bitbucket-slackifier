package eu.kandru.jko.bitbucket.slackifier.slack.messages;

import com.atlassian.bitbucket.avatar.AvatarRequest;
import com.atlassian.bitbucket.avatar.AvatarService;
import com.atlassian.bitbucket.comment.CommentAction;
import com.atlassian.bitbucket.event.pull.PullRequestCommentEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.user.Person;
import com.atlassian.sal.api.component.ComponentLocator;
import com.ullink.slack.simpleslackapi.SlackAttachment;

public class PreparedPullRequestCommentMessage implements SlackPreparedMessage {

  private PullRequestCommentEvent event;
  private NavBuilder navBuilder;

  public PreparedPullRequestCommentMessage(PullRequestCommentEvent event, NavBuilder navBuilder) {
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
    PullRequest pr = event.getPullRequest();
    String text = event.getUser().getDisplayName() + " hat einen Kommentar " + getVerb();
    attachment.addMiscField("author_name", event.getUser().getDisplayName());
    attachment.addMiscField("author_icon", getAvatar(event.getUser()));
    attachment.setFallback(text);
    attachment.setColor(SlackColorCodes.COLOR_DEFAULT);
    attachment.setTitle("Kommentar zu PullRequest " + pr.getTitle());
    attachment.setTitleLink(navBuilder.repo(pr.getToRef().getRepository()).pullRequest(pr.getId())
        .comment(event.getComment().getId()).buildAbsolute());
    attachment.setText(event.getComment().getText());
    attachment.addMiscField("author", event.getUser().getDisplayName());
    return attachment;
  }

  private String getVerb() {
    if (event.getCommentAction() == CommentAction.ADDED)
      return "hinzugef\u00FCgt.";
    else if (event.getCommentAction() == CommentAction.EDITED)
      return "bearbeitet.";
    else if (event.getCommentAction() == CommentAction.DELETED)
      return "gel\u00F6scht.";
    else if (event.getCommentAction() == CommentAction.REPLIED)
      return "beantwortet.";
    else
      return "kaputt bekommen.";
  }

  private String getAvatar(Person user) {
    AvatarService avatarService = ComponentLocator.getComponent(AvatarService.class);
    AvatarRequest request = new AvatarRequest(true, 32, true);
    return avatarService.getUrlForPerson(user, request);
  }


}
