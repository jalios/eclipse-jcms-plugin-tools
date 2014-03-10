package com.jalios.jcmsplugin.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImpliciteSyncCompute implements ISync {

  @Override
  public SyncComputeResult computeSync(SyncConfiguration conf, SyncComputeResult result) throws SyncException {
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
        result.addSyncFiles(webappFile, Direction.TO_PLUGIN, pluginFile);
      }
    }

    return result;
  }
}
