package com.jalios.jcmstools.transversal;

public interface JPTConstants {
  public static final String PLUGIN_PROJECT_FILE = ".jcmsPluginNature";
  public static final String OLD_NATURE_PLUGIN_PROJECT = ".*Plugin.*";
  public static final String SYNC_CONF_FILENAME = "sync.conf";

  public static final String JTOOLS_NAME = "Jalios Plugin Tools for Eclipse";

  /* ------------ KEY PROPERTIES IN SYNC.CONF-------- */
  public static final String WEBAPP_ROOT_KEY_SC = "root.dir";
  public static final String EXCLUDED_DIR_KEY_SC = "excluded.dirs";
  public static final String EXCLUDED_FILES_KEY_SC = "excluded.files";

  public static final String JCMS_PLUGIN_NATURE = "com.jalios.jpt.natures.jcmspluginnature";
  public static final String JCMS_PROJECT_NATURE = "com.jalios.jpt.natures.jcmsprojectnature";  

}
