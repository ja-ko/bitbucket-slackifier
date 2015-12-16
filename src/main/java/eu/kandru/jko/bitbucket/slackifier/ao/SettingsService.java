package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface SettingsService {
  String getApiKey();

  void setApiKey(String apiKey);
}
