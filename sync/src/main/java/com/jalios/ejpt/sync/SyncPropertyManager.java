package com.jalios.ejpt.sync;

import static com.jalios.ejpt.sync.SyncConfigurationConstants.EXCLUDED_DIRS;
import static com.jalios.ejpt.sync.SyncConfigurationConstants.EXCLUDED_FILES;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.jalios.ejpt.sync.utils.Util;

public class SyncPropertyManager {
  private static final SyncPropertyManager SINGLETON = new SyncPropertyManager();
  private static Properties prop;

  private SyncPropertyManager() {

  }

  public static SyncPropertyManager init(File configuration) {    
    prop = Util.loadProperties(configuration);
    return SINGLETON;
  }

  public String getProperty(String key) {
    return prop.getProperty(key);
  }

  public List<String> getExcludedDirs() {
    return Util.splitToList(getProperty(EXCLUDED_DIRS), ",");
  }

  public List<String> getExcludedFiles() {
    return Util.splitToList(getProperty(EXCLUDED_FILES), ",");
  }
}
