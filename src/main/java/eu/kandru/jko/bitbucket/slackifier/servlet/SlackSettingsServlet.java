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

import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

import eu.kandru.jko.bitbucket.slackifier.ao.SettingsService;
import eu.kandru.jko.bitbucket.slackifier.slack.SlackService;
import eu.kandru.jko.bitbucket.slackifier.util.ResourceConstants;

public class SlackSettingsServlet extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(SlackSettingsServlet.class);

  private SettingsService settingsService;
  private SoyTemplateRenderer soyTemplateRenderer;
  private NavBuilder navBuilder;
  private PermissionService permissionService;
  private SlackService slackService;

  public SlackSettingsServlet(SettingsService settingsService, SoyTemplateRenderer soyTemplateRenderer,
      NavBuilder navBuilder, PermissionService permissionService, SlackService slackService) {
    this.settingsService = settingsService;
    this.soyTemplateRenderer = soyTemplateRenderer;
    this.navBuilder = navBuilder;
    this.permissionService = permissionService;
    this.slackService = slackService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!permissionService.hasGlobalPermission(Permission.SYS_ADMIN)) {
      resp.sendRedirect(navBuilder.login().sysadmin().next().buildAbsolute());
      return;
    }

    resp.setContentType("text/html");

    String apiKey = settingsService.getApiKey();

    Map<String, Object> context = new HashMap<>();
    context.put("apiKey", apiKey);
    context.put("postUrl", navBuilder.pluginServlets().path("admin", "slack", "settings").buildAbsolute());

    soyTemplateRenderer.render(resp.getWriter(), ResourceConstants.SOY_TEMPLATES,
        ResourceConstants.SOY_SLACK_SETTINGS_SERVLET, Collections.unmodifiableMap(context));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!permissionService.hasGlobalPermission(Permission.SYS_ADMIN)) {
      resp.sendRedirect(navBuilder.login().sysadmin().next().buildAbsolute());
      return;
    }

    String apiKey = req.getParameter("slackApi");

    String oldKey = settingsService.getApiKey();
    if (oldKey != null && !oldKey.equals(apiKey) && slackService.isConnected())
      slackService.disconnect();
    settingsService.setApiKey(apiKey);
    if (apiKey != null)
      slackService.connect();
    resp.sendRedirect(navBuilder.admin().buildAbsolute());
  }

}
