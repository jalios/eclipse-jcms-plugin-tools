package com.jalios.ejpt.sync;

import java.io.File;
import java.util.List;

import com.jalios.ejpt.sync.filesyncstatus.FileShouldBeDeclared;
import com.jalios.ejpt.sync.utils.IOUtil;

public class FilesShoudBeDeclaredStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    List<File> filesDeclaredByPluginXML = configuration.getFilesDeclaredByPluginXML();
    List<File> physicalFiles = IOUtil.deepListFiles(configuration.getPluginProjectDirectory(), configuration.getFileFilter());

    for (File physicalFile : physicalFiles) {
      if (!filesDeclaredByPluginXML.contains(physicalFile)) {
        report.addReport(new FileShouldBeDeclared(physicalFile), SyncStrategyReport.Direction.UNKNOWN);        
      }
    }    
    return report;
  }

}