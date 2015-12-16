package eu.kandru.jko.bitbucket.slackifier.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.ullink.slack.simpleslackapi.SlackUser;

import eu.kandru.jko.bitbucket.slackifier.ao.LinkedUserService;
import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;
import eu.kandru.jko.bitbucket.slackifier.util.ResourceConstants;

public class SlackUserChangeServlet extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(SlackUserChangeServlet.class);

  private SoyTemplateRenderer soyTemplateRenderer;
  private NavBuilder navBuilder;
  private SlackService slackService;
  private LinkedUserService linkedUserService;

  public SlackUserChangeServlet(SoyTemplateRenderer soyTemplateRenderer, NavBuilder navBuilder,
      SlackService slackService, LinkedUserService linkedUserService) {
    this.soyTemplateRenderer = soyTemplateRenderer;
    this.navBuilder = navBuilder;
    this.slackService = slackService;
    this.linkedUserService = linkedUserService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    AuthenticationContext authenticationContext = ComponentLocator.getComponent(AuthenticationContext.class);
    ApplicationUser currentUser = authenticationContext.getCurrentUser();

    if (currentUser == null) {
      resp.sendRedirect(navBuilder.login().next().buildAbsolute());
      return;
    }

    resp.setContentType("text/html");

    Map<String, Object> context = new HashMap<>();
    context.put("user", currentUser);
    SlackUser slackUser = linkedUserService.getSlackUser(currentUser);
    String slackName = slackUser == null ? "" : slackUser.getUserName();
    context.put("slackName", slackName);
    context.put("postUrl", navBuilder.pluginServlets().path("account", "slackname").buildAbsolute());

    soyTemplateRenderer.render(resp.getWriter(), ResourceConstants.SOY_TEMPLATES,
        ResourceConstants.SOY_SLACK_USER_SERVLET, Collections.unmodifiableMap(context));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    AuthenticationContext authenticationContext = ComponentLocator.getComponent(AuthenticationContext.class);
    ApplicationUser currentUser = authenticationContext.getCurrentUser();

    if (currentUser == null) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    SlackUser slackUser = slackService.findUserByName(req.getParameter("slackName"));
    if (slackUser == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    linkedUserService.linkUser(slackUser, currentUser);
    resp.sendRedirect(navBuilder.profile().buildAbsolute());
  }

}
