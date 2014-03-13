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
import java.util.List;

/**
 * Basic sync from legacy implementation at Jalios Delete is not taken in
 * account. This class is only for package visibility
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
final class BasicSyncCompute implements ISync {

  @Override
  public void computeSync(SyncConfiguration conf, SyncComputeResult result) throws SyncException {
    File wpRootDir = conf.getWebappProjectRootDir();
    File ppRootDir = conf.getPluginProjectRootDir();
    List<File> ppSubDir = SyncUtil.listDirectoryFirstLevel(ppRootDir);

    List<File> ppFileList = new ArrayList<File>();
    if (ppSubDir == null || ppSubDir.size() == 0) {
      ppFileList = SyncUtil.deepListFiles(ppRootDir, new BlackListFilter());
    } else {
      for (File itDir : ppSubDir) {
        ppFileList.addAll(SyncUtil.deepListFiles(itDir, new BlackListFilter()));
      }
    }

    for (File src : ppFileList) {
      // Get the related tgtFile
      String targetRelPath = SyncUtil.getRelativePath(ppRootDir, src);
      File target = new File(wpRootDir, targetRelPath);

      if (target == null || target.lastModified() < src.lastModified()) {
        result.addSyncFilesToWebapp(src, target);
        continue;
      }

      if (target.lastModified() > src.lastModified()) {
        result.addSyncFilesToPlugin(target, src);
      }
    }
  }
}
