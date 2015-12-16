package eu.kandru.jko.bitbucket.slackifier.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.bitbucket.user.UserService;
import com.ullink.slack.simpleslackapi.SlackUser;

import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;

public class LinkedUserServiceImpl implements LinkedUserService {

  private ActiveObjects ao;
  private UserService userService;
  private SlackService slackService;

  public LinkedUserServiceImpl(ActiveObjects ao, UserService userService, SlackService slackService) {
    this.ao = ao;
    this.userService = userService;
    this.slackService = slackService;
  }

  @Override
  public ApplicationUser getAppUser(SlackUser user) {
    LinkedUser[] users = ao.find(LinkedUser.class, "SLACK_ID LIKE ?", user.getId());
    if (users.length == 0) {
      return createDefaultAppUser(user);
    }
    LinkedUser found = users[0];
    if (!found.getResolved())
      return createDefaultAppUser(user);
    else
      return userService.getUserById(found.getUserId());
  }

  private ApplicationUser createDefaultAppUser(SlackUser user) {
    ApplicationUser appUser = userService.findUserByNameOrEmail(user.getUserMail());
    if (appUser == null)
      createLinkedUser(user.getId(), -1, false);
    else
      createLinkedUser(user.getId(), appUser.getId(), true);
    return appUser;
  }

  @Override
  public SlackUser getSlackUser(ApplicationUser user) {
    LinkedUser[] users = ao.find(LinkedUser.class, "USER_ID = ?", user.getId());
    if (users.length == 0) {
      return createDefaultSlackUser(user);
    }
    LinkedUser found = users[0];
    if (!found.getResolved())
      return createDefaultSlackUser(user);
    else
      return slackService.findUserById(found.getSlackId());
  }

  private SlackUser createDefaultSlackUser(ApplicationUser user) {
    SlackUser slackUser = slackService.findUserByEmail(user.getEmailAddress());
    if (slackUser == null)
      createLinkedUser(null, user.getId(), false);
    else
      createLinkedUser(slackUser.getId(), user.getId(), true);
    return slackUser;
  }

  @Override
  public void linkUser(SlackUser slack, ApplicationUser app) {
    createLinkedUser(slack.getId(), app.getId(), true);
  }

  private LinkedUser createLinkedUser(String slackId, int userId, boolean resolved) {
    ao.deleteWithSQL(LinkedUser.class, "USER_ID = ?", userId);
    LinkedUser result = ao.create(LinkedUser.class);
    result.setSlackId(slackId);
    result.setUserId(userId);
    result.setResolved(resolved);
    result.save();
    return result;
  }

}
