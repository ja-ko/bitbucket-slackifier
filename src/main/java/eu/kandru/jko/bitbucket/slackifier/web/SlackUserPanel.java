package eu.kandru.jko.bitbucket.slackifier.web;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.bitbucket.user.ApplicationUser;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.ullink.slack.simpleslackapi.SlackUser;

import eu.kandru.jko.bitbucket.slackifier.ao.LinkedUserService;
import eu.kandru.jko.bitbucket.slackifier.util.ResourceConstants;

public class SlackUserPanel implements WebPanel {

  private LinkedUserService linkedUserService;
  private SoyTemplateRenderer soyTemplateRenderer;
  private WebResourceUrlProvider webResourceUrlProvider;

  public SlackUserPanel(LinkedUserService linkedUserService, SoyTemplateRenderer soyTemplateRenderer,
      WebResourceUrlProvider webResourceUrlProvider) {
    this.linkedUserService = linkedUserService;
    this.soyTemplateRenderer = soyTemplateRenderer;
    this.webResourceUrlProvider = webResourceUrlProvider;
  }

  @Override
  public String getHtml(Map<String, Object> context) {
    try (StringWriter writer = new StringWriter()) {
      writeHtml(writer, context);
      return writer.toString();
    } catch (IOException e) {
      // ignore silently
      return "";
    }
  }

  @Override
  public void writeHtml(Writer writer, Map<String, Object> context) throws IOException {
    ApplicationUser user = (ApplicationUser) context.get("profileUser");

    Map<String, Object> soyContext = new HashMap<>();
    soyContext.put("icon", webResourceUrlProvider.getStaticPluginResourceUrl(ResourceConstants.GENERAL,
        "images/slackIconBlack.png", UrlMode.RELATIVE));

    SlackUser slackUser = linkedUserService.getSlackUser(user);
    String slackName = slackUser == null ? "" : slackUser.getUserName();

    soyContext.put("slackName", slackName);
    soyTemplateRenderer.render(writer, ResourceConstants.SOY_TEMPLATES, ResourceConstants.SOY_SLACK_USER_PANEL,
        Collections.unmodifiableMap(soyContext));
  }

}
