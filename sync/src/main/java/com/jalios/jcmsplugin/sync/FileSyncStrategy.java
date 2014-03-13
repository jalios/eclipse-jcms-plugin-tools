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
package com.jalios.jcmsplugin.sync;

import java.io.File;
import java.util.List;

/**
 * This sync strategy bases on file present between a webapp project and a
 * plugin project. If file doesn't exist on webapp project, it'll be reported to
 * be copied Otherwise, it'll base on last modified date to decide the copy
 * direction
 * 
 * @author Xuan Tuong LE (@lxtuong, lxtuong@gmail.com)
 */
public final class FileSyncStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    File webappProjectDirectory = configuration.getWebappProjectRootDir();
    File pluginProjectDirectory = configuration.getPluginProjectRootDir();

    for (File pluginFile : getPluginProjectFiles(pluginProjectDirectory)) {
      File webappFile = getWebappFile(webappProjectDirectory, pluginProjectDirectory, pluginFile);

      if (webappFile == null || webappFile.lastModified() < pluginFile.lastModified()) {
        report.addCopyReport(pluginFile, webappFile, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() > pluginFile.lastModified()) {
        report.addCopyReport(webappFile, pluginFile, SyncStrategyReport.Direction.TO_PLUGIN);
      }
    }
    return report;
  }

  private File getWebappFile(File webappProjectDirectory, File pluginProjectDirectory, File pluginFile) {
    String pluginFileRelativePath = SyncUtil.getRelativePath(pluginProjectDirectory, pluginFile);
    return new File(webappProjectDirectory, pluginFileRelativePath);
  }

  private List<File> getPluginProjectFiles(File pluginProjectRootDirectory) {
    return SyncUtil.deepListFiles(pluginProjectRootDirectory, new BlackListFilter());
  }
}
