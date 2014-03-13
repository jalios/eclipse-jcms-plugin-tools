package com.jalios.jcmsplugin.sync;

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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This strategy only works when plugin project name is the same as plugin name
 * It's a convention but it is not always the case
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public final class NewWebappFileStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    File webappProjectDirectory = configuration.getWebappProjectRootDir();
    File pluginProjectDirectory = configuration.getPluginProjectRootDir();

    List<File> webappFiles = getFilesInPluginPublicDirectory(webappProjectDirectory, pluginProjectDirectory.getName());

    for (File webappFile : webappFiles) {
      File pluginFile = getPluginFile(pluginProjectDirectory, webappProjectDirectory, webappFile);

      if (pluginFile == null || pluginFile.lastModified() < webappFile.lastModified()) {
        report.addCopyReport(webappFile, pluginFile, SyncStrategyReport.Direction.TO_PLUGIN);
      }
    }
    return report;
  }

  private List<File> getFilesInPluginPublicDirectory(File webappProjectDirectory, String pluginName) {
    List<File> files = new ArrayList<File>();
    File pluginPublicDirectory = new File(webappProjectDirectory + "/plugins/" + pluginName);
    if (pluginPublicDirectory.isDirectory()) {
      files.addAll(SyncUtil.deepListFiles(pluginPublicDirectory, new BlackListFilter()));
    }
    return files;

  }

  private File getPluginFile(File pluginProjectDirectory, File webappProjectDirectory, File webappFile) {
    String pluginFileRelativePath = SyncUtil.getRelativePath(webappProjectDirectory, webappFile);
    return new File(pluginProjectDirectory, pluginFileRelativePath);
  }
}
