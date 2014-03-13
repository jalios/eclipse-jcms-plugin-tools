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
import java.util.Iterator;
import java.util.List;

/**
 * This class is only for package visibility
 * @author Xuan Tuong LE - lxtuong@gmail.com
 *
 */
final class ImpliciteSyncCompute implements ISync {

  @Override
  public void computeSync(SyncConfiguration conf, SyncComputeResult result) throws SyncException {
    File wpRootDir = conf.getWebappProjectRootDir();
    File ppRootDir = conf.getPluginProjectRootDir();
    List<File> fileList = new ArrayList<File>();
    File pluginPublicDir = new File(wpRootDir + "/plugins/" + ppRootDir.getName());
    if (pluginPublicDir.isDirectory()) {
      fileList.addAll(SyncUtil.deepListFiles(pluginPublicDir, new BlackListFilter()));
    }

    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
      File webappFile = (File) it.next();

      // Get the related tgtFile
      String srcRelativePath = SyncUtil.getRelativePath(wpRootDir, webappFile);
      File pluginFile = new File(ppRootDir, srcRelativePath);

      // tgtFile is missing or older, copy srcFile -> tgtFile
      if (pluginFile == null || pluginFile.lastModified() < webappFile.lastModified() ) {
        result.addSyncFilesToPlugin(webappFile, pluginFile);
      }
    }
  }
}
