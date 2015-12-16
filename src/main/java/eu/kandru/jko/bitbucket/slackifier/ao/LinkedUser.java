package eu.kandru.jko.bitbucket.slackifier.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface LinkedUser extends Entity {

  int getUserId();

  void setUserId(int userId);

  String getSlackId();

  void setSlackId(String slackId);

  boolean getResolved();

  void setResolved(boolean resolved);

}
