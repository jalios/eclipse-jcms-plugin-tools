package com.jalios.ejpt.sync;

import java.io.File;

import com.jalios.ejpt.sync.filesyncstatus.FileAdded;
import com.jalios.ejpt.sync.filesyncstatus.FileModified;
import com.jalios.ejpt.sync.filesyncstatus.FileNotFoundOnDisk;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.utils.IOUtil;

public class FilesFromPluginXmlStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();

    for (File declaredPluginFile : configuration.getFilesDeclaredByPluginXML()) {
      if (!declaredPluginFile.exists()) {
        report.addReport(new FileNotFoundOnDisk(declaredPluginFile), SyncStrategyReport.Direction.UNKNOWN);
        continue;
      }

      File webappFile = IOUtil.getDestinationFile(configuration.getWebappProjectDirectory(),
          configuration.getPluginProjectDirectory(), declaredPluginFile);

      if (!webappFile.exists()) {
        FileSyncStatus fileAdded = new FileAdded(declaredPluginFile, webappFile);
        report.addReport(fileAdded, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() < declaredPluginFile.lastModified()) {
        FileSyncStatus fileModified = new FileModified(declaredPluginFile, webappFile);
        report.addReport(fileModified, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() > declaredPluginFile.lastModified()) {
        FileSyncStatus fileModified = new FileModified(webappFile, declaredPluginFile);
        report.addReport(fileModified, SyncStrategyReport.Direction.TO_PLUGIN);
      }

    }
    return report;
  }

}