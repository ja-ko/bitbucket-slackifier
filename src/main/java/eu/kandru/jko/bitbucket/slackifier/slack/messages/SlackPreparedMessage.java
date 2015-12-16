package eu.kandru.jko.bitbucket.slackifier.slack.messages;

import com.ullink.slack.simpleslackapi.SlackAttachment;

public interface SlackPreparedMessage {
  String getMessage();

  SlackAttachment getAttachment();
}
