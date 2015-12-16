package eu.kandru.jko.bitbucket.slackifier.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface MappedPullRequest extends Entity {
  int getRepositoryId();

  void setRepositoryId(int id);

  long getPullRequestId();

  void setPullRequestId(long id);

  String getChannelId();

  void setChannelId(String id);
}
