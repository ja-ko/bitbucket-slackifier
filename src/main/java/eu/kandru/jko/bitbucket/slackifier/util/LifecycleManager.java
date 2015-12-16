package eu.kandru.jko.bitbucket.slackifier.util;

import com.atlassian.sal.api.lifecycle.LifecycleAware;

import eu.kandru.jko.bitbucket.slackifier.ao.SettingsService;
import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;

public class LifecycleManager implements LifecycleAware {

  private SlackService slackService;
  private SettingsService settingsService;

  public LifecycleManager(SlackService slackService, SettingsService settingsService) {
    this.slackService = slackService;
    this.settingsService = settingsService;
  }

  @Override
  public void onStart() {
    if (!"".equals(settingsService.getApiKey()))
      slackService.connect();
  }

  @Override
  public void onStop() {
    if (slackService.isConnected())
      slackService.disconnect();
  }

}
