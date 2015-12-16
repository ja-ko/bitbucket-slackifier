package eu.kandru.jko.bitbucket.slackifier.slack;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import com.ullink.slack.simpleslackapi.replies.SlackReply;

import eu.kandru.jko.bitbucket.slackifier.ao.SettingsService;
import eu.kandru.jko.bitbucket.slackifier.slack.messages.SlackPreparedMessage;

public class SlackService {
  private static final Logger LOG = LoggerFactory.getLogger(SlackService.class);
  private SettingsService settingsService;

  private SlackSession session;

  public SlackService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  public void connect() {
    try {
      String authToken = settingsService.getApiKey();
      if (authToken.equals(""))
        return;
      session = SlackSessionFactory.createWebSocketSlackSession(authToken);
      session.connect();
    } catch (IOException e) {
      LOG.error("unable to connect", e);
    }
  }

  public boolean tryConnect(String authToken) {
    try {
      boolean result;
      SlackSession test = SlackSessionFactory.createWebSocketSlackSession(authToken);
      test.connect();
      result = test.isConnected();
      test.disconnect();
      return result;
    } catch (IOException e) {
      LOG.warn("connection test failed", e);
      return false;
    }
  }

  public void disconnect() {
    if (!isConnected())
      return;
    try {
      session.disconnect();
    } catch (IOException e) {
      LOG.error("Unable to disconnect", e);
    }
  }

  public boolean isConnected() {
    if (session == null)
      return false;
    return session.isConnected();
  }

  public SlackChannel createChannel(String name) {
    if (!isConnected())
      return null;
    SlackMessageHandle<SlackChannelReply> handle = session.joinChannel(name);
    handle.waitForReply(2, TimeUnit.SECONDS);
    SlackChannelReply reply = handle.getReply();
    if (reply == null)
      return null;
    if (!reply.isOk())
      return null;
    return reply.getSlackChannel();
  }

  public SlackChannel getChannelByName(String name) {
    if (!isConnected())
      return null;
    return session.findChannelByName(name);
  }

  public SlackChannel getChannelById(String id) {
    if (!isConnected())
      return null;
    return session.findChannelById(id);
  }

  public boolean inviteToChannel(SlackChannel channel, SlackUser user) {
    if (!isConnected())
      return false;
    if (user == null)
      return false;
    SlackMessageHandle<SlackChannelReply> handle = session.inviteToChannel(channel, user);
    return checkHandle(handle);
  }

  public boolean archiveChannel(SlackChannel channel) {
    if (!isConnected())
      return false;
    SlackMessageHandle<SlackReply> handle = session.archiveChannel(channel);
    return checkHandle(handle);
  }

  public SlackUser findUserByName(String name) {
    if (!isConnected())
      return null;
    SlackUser user = session.findUserByUserName(name);
    return user;
  }

  public SlackUser findUserByEmail(String email) {
    if (!isConnected())
      return null;
    SlackUser user = session.findUserByEmail(email);
    return user;
  }

  public boolean sendMessage(SlackChannel channel, String message) {
    if (!isConnected())
      return false;
    SlackMessageHandle<SlackMessageReply> handle = session.sendMessage(channel, message, null, getConfig());
    return checkHandle(handle);
  }

  private boolean checkHandle(SlackMessageHandle<?> handle) {
    handle.waitForReply(2, TimeUnit.SECONDS);
    if (handle.getReply() == null)
      return false;
    return handle.getReply().isOk();
  }

  public boolean sendMessage(SlackChannel channel, SlackPreparedMessage message) {
    if (!isConnected())
      return false;
    SlackMessageHandle<SlackMessageReply> handle =
        session.sendMessage(channel, message.getMessage(), message.getAttachment(), getConfig());
    return checkHandle(handle);
  }

  private SlackChatConfiguration getConfig() {
    return SlackChatConfiguration.getConfiguration().withName("Bitbucket Bot");
  }

  public SlackUser findUserById(String id) {
    if (!isConnected())
      return null;
    return session.findUserById(id);
  }
}
