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
public final class BasicSyncCompute implements ISync {

  @Override
  public SyncComputeResult computeSync(SyncConfiguration conf, SyncComputeResult result) throws SyncException {   
    File wpRootDir = conf.getWebappProjectRootDir();
    File ppRootDir = conf.getPluginProjectRootDir();
    List<File> subDirPluginProject = SyncUtil.listDirectoryFirstLevel(ppRootDir);

    List<File> filePluginProjectList = new ArrayList<File>();
    if (subDirPluginProject == null || subDirPluginProject.size() == 0) {
      filePluginProjectList = SyncUtil.deepListFiles(ppRootDir, new BlackListFilter());
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
      String filePluginProjectRelativePath = SyncUtil.getRelativePath(ppRootDir, filePluginProject);
      File fileJcmsProject = new File(wpRootDir, filePluginProjectRelativePath);

      if (fileJcmsProject == null || fileJcmsProject.lastModified() < filePluginProject.lastModified()) {
        result.addSyncFiles(filePluginProject, Direction.TO_WEBAPP, fileJcmsProject);
        continue;
      }

      if (fileJcmsProject.lastModified() > filePluginProject.lastModified()) {
        result.addSyncFiles(fileJcmsProject, Direction.TO_PLUGIN, filePluginProject);
      }
    }

    return result;
  }
}
