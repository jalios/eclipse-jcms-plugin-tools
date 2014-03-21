package com.jalios.ejpt.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;

public class CopyExecutor implements SyncExecutor {

  @Override
  public void run(SyncStrategyReport report) {
    List<FileSyncStatus> syncFiles = new ArrayList<FileSyncStatus>();
    syncFiles.addAll(report.getSyncFilesToPlugin());
    syncFiles.addAll(report.getSyncFilesToWebapp());
    for (FileSyncStatus syncFile : syncFiles) {
      try {
        SyncUtil.copyFile(syncFile.getSource(), syncFile.getDestination());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
