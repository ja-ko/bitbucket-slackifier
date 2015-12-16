package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.external.ActiveObjects;

public class SettingsServiceImpl implements SettingsService {

  private ActiveObjects ao;

  public SettingsServiceImpl(ActiveObjects ao) {
    this.ao = ao;
  }

  @Override
  public String getApiKey() {
    Settings[] settings = ao.find(Settings.class);
    if (settings.length == 0)
      return "";
    return settings[0].getApiKey();
  }

  @Override
  public void setApiKey(String apiKey) {
    Settings[] settings = ao.find(Settings.class);
    Settings settingsObject;
    if (settings.length == 0)
      settingsObject = ao.create(Settings.class);
    else
      settingsObject = settings[1];
    settingsObject.setApiKey(apiKey);
    settingsObject.save();
  }
}
