package com.jalios.jcmsplugin.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CopyExecutor implements SyncExecutor {

  @Override
  public void run(SyncStrategyReport report) {
    List<SyncFile> syncFiles = new ArrayList<SyncFile>();
    syncFiles.addAll(report.getSyncFilesToPlugin());
    syncFiles.addAll(report.getSyncFilesToWebapp());
    for (SyncFile syncFile : syncFiles) {
      try {
        SyncUtil.copyFile(syncFile.getSrc(), syncFile.getTgt());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
