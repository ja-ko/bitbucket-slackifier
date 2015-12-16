package eu.kandru.jko.bitbucket.slackifier.util;

public final class ResourceConstants {
  private ResourceConstants() {
    // empty
  }

  private static final String RESOURCE_PREFIX = "eu.kandru.jko.bitbucket.slackifier:";
  public static final String SOY_TEMPLATES = RESOURCE_PREFIX + "soyTemplates";
  public static final String GENERAL = RESOURCE_PREFIX + "slackifier-resources";


  private static final String SOY_NAMESPACE = "eu.kandru.jko.bitbucket.slackifier";

  public static final String SOY_SLACK_USER_PANEL = SOY_NAMESPACE + ".slackNameLabel";
  public static final String SOY_SLACK_USER_SERVLET = SOY_NAMESPACE + ".slackNameServlet";
  public static final String SOY_SLACK_SETTINGS_SERVLET = SOY_NAMESPACE + ".slackSettingsServlet";
}
