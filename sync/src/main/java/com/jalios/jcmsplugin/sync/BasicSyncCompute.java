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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
public class BasicSyncCompute implements ISync {

  @Override
  public SyncComputeResult computeSync(File jcmsProjectDirPath, File pluginProjectDirPath,
      SyncConfiguration configuration, SyncComputeResult previousSyncCompute) throws SyncException {
    List<File> subDirPluginProject = SyncUtil.listDirectoryFirstLevel(pluginProjectDirPath);

    List<File> filePluginProjectList = new ArrayList<File>();
    if (subDirPluginProject == null || subDirPluginProject.size() == 0) {
      filePluginProjectList = SyncUtil.deepListFiles(pluginProjectDirPath, new BlackListFilter());
    } else {
      for (Iterator<File> it = subDirPluginProject.iterator(); it.hasNext();) {
        File itDir = (File) it.next();
        filePluginProjectList.addAll(SyncUtil.deepListFiles(itDir, new BlackListFilter()));
      }
    }

    // Iterates over the src files
    for (Iterator<File> it = filePluginProjectList.iterator(); it.hasNext();) {
      File filePluginProject = (File) it.next();
      // Get the related tgtFile
      String filePluginProjectRelativePath = SyncUtil.getRelativePath(pluginProjectDirPath, filePluginProject);
      File fileJcmsProject = new File(jcmsProjectDirPath, filePluginProjectRelativePath);

      if (fileJcmsProject == null || fileJcmsProject.lastModified() < filePluginProject.lastModified()) {
        previousSyncCompute.addSyncFiles(filePluginProject, Direction.TO_WEBAPP, fileJcmsProject);
        continue;
      }

      if (fileJcmsProject.lastModified() > filePluginProject.lastModified()) {
        previousSyncCompute.addSyncFiles(fileJcmsProject, Direction.TO_PLUGIN, filePluginProject);
      }
    }

    return previousSyncCompute;
  }

}
