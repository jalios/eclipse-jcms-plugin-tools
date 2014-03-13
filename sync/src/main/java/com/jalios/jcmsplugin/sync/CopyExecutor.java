package com.jalios.jcmsplugin.sync;

import java.io.IOException;

public class CopyExecutor implements SyncExecutor {

  @Override
  public void run(SyncStrategyReport report) {
    for (SyncFile syncFile : report.getSyncFiles(SyncStrategyReport.Direction.TO_WEBAPP)) {
      try {
        SyncUtil.copyFile(syncFile.getSrc(), syncFile.getTgt());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    for (SyncFile sf : report.getSyncFiles(SyncStrategyReport.Direction.TO_PLUGIN)) {
      try {
        SyncUtil.copyFile(sf.getSrc(), sf.getTgt());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
