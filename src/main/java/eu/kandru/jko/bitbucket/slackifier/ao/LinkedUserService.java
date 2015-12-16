package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.ullink.slack.simpleslackapi.SlackUser;

@Transactional
public interface LinkedUserService {
  ApplicationUser getAppUser(SlackUser user);

  SlackUser getSlackUser(ApplicationUser user);

  void linkUser(SlackUser slack, ApplicationUser app);
}
