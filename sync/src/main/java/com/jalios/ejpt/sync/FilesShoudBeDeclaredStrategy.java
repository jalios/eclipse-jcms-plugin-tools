package com.jalios.ejpt.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jalios.ejpt.sync.filesyncstatus.FileShouldDeclare;
import com.jalios.ejpt.sync.utils.IOUtil;

public class FilesShoudBeDeclaredStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    List<File> filesShouldBeDeclared = new ArrayList<File>();

    List<File> physicalFiles = IOUtil.deepListFiles(configuration.getPluginProjectDirectory(), configuration.getFileFilter());

    for (File physicalFile : physicalFiles) {
      if (!configuration.getFilesDeclaredByPluginXML().contains(physicalFile)) {
        filesShouldBeDeclared.add(physicalFile);
      }
    }
    for (File itFile : filesShouldBeDeclared) {
      report.addReport(new FileShouldDeclare(itFile), SyncStrategyReport.Direction.UNKNOWN);
    }
    return report;
  }

}