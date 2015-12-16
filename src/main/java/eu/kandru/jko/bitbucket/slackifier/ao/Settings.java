package eu.kandru.jko.bitbucket.slackifier.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface Settings extends Entity {
  String getApiKey();

  void setApiKey(String apiKey);
}
