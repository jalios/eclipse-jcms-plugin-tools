/*
 GNU LESSER GENERAL PUBLIC LICENSE
 Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


 This version of the GNU Lesser General Public License incorporates
 the terms and conditions of version 3 of the GNU General Public
 License
 */
package com.jalios.ejpt.sync;

import static com.jalios.ejpt.sync.SyncConfigurationConstants.EXCLUDED_DIRS;
import static com.jalios.ejpt.sync.SyncConfigurationConstants.EXCLUDED_FILES;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.jalios.ejpt.sync.utils.Util;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
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
