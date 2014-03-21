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

import java.io.File;
import java.io.FileFilter;

/**
 * @author Xuan Tuong LE - lxtuong@gmail.com
 *
 */
public final class SyncStrategyConfiguration {
  private File pluginDirectory;
  private File webappDirectory;
  private File config;
  private FileFilter fileFilter;
  
  private SyncStrategyConfiguration(Builder builder) {
    this.pluginDirectory = builder.pluginDirectory;
    this.webappDirectory = builder.webappDirectory;
    this.config = builder.config;
    this.fileFilter = builder.fileFilter;
  }

  public File getPluginProjectDirectory() {
    return pluginDirectory;
  }

  public File getWebappProjectDirectory() {
    return webappDirectory;
  }
  
  public FileFilter getFileFilter(){
    return fileFilter;
  }
    
  public static class Builder {
    private File pluginDirectory;
    private File webappDirectory;
    private File config;
    private FileFilter fileFilter = new BlackListFilter.Builder().build();

    public Builder(File ppRootDir, File wpRootDir) {
      this.pluginDirectory = ppRootDir;
      this.webappDirectory = wpRootDir;
    }

    public Builder configuration(File config) {
      SyncPropertyManager propertyManager = SyncPropertyManager.init(config);
      fileFilter = new BlackListFilter.Builder().excludedDirs(propertyManager.getExcludedDirs())
          .excludedFiles(propertyManager.getExcludedFiles()).build();
      this.config = config;
      return this;
    }

    public SyncStrategyConfiguration build() {
      return new SyncStrategyConfiguration(this);
    }
  }
}
