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
import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.parser.ParseUtil;
import com.jalios.ejpt.sync.utils.BlackListFilter;

/**
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public final class SyncStrategyConfiguration {
  private File pluginDirectory;
  private File webappDirectory;
  private FileFilter fileFilter;
  private List<File> filesDeclaredByPluginXML = new LinkedList<File>();

  private SyncStrategyConfiguration(Builder builder) {
    this.pluginDirectory = builder.pluginDirectory;
    this.webappDirectory = builder.webappDirectory;
    this.fileFilter = builder.fileFilter;
    this.filesDeclaredByPluginXML = builder.filesDeclaredByPluginXML;
  }

  public File getPluginProjectDirectory() {
    return pluginDirectory;
  }

  public File getWebappProjectDirectory() {
    return webappDirectory;
  }

  public FileFilter getFileFilter() {
    return fileFilter;
  }

  public List<File> getFilesDeclaredByPluginXML() {
    return filesDeclaredByPluginXML;
  }

  public static class Builder {
    private File pluginDirectory;
    private File webappDirectory;
    private FileFilter fileFilter = new BlackListFilter.Builder().build();
    private List<File> filesDeclaredByPluginXML = new LinkedList<File>();

    public Builder(File ppRootDir, File wpRootDir) {
      this.pluginDirectory = ppRootDir;
      this.webappDirectory = wpRootDir;
    }

    public Builder configuration(File config) {
      SyncPropertyManager propertyManager = SyncPropertyManager.init(config);
      fileFilter = new BlackListFilter.Builder().excludedDirs(propertyManager.getExcludedDirs())
          .excludedFiles(propertyManager.getExcludedFiles()).build();
      return this;
    }

    public SyncStrategyConfiguration build() {
      filesDeclaredByPluginXML = ParseUtil.getPluginXmlDeclaredFiles(this.pluginDirectory, this.fileFilter);
      return new SyncStrategyConfiguration(this);
    }

  }
}
