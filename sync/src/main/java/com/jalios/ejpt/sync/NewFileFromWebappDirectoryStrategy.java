package com.jalios.ejpt.sync;

import java.io.File;
import java.util.List;

import com.jalios.ejpt.parser.ParseUtil;
import com.jalios.ejpt.sync.filesyncstatus.FileAdded;
import com.jalios.ejpt.sync.filesyncstatus.FileCouldBeMissed;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.utils.IOUtil;
import com.jalios.ejpt.sync.utils.Util;

public class NewFileFromWebappDirectoryStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    File privatePluginDirectory = ParseUtil.getPrivatePluginDirectory(configuration.getPluginProjectDirectory());
    if (privatePluginDirectory == null) {
      return report;
    }
    File pluginPublicDirectory = new File(configuration.getWebappProjectDirectory(), "/plugins/"
        + privatePluginDirectory.getName());

    if (!pluginPublicDirectory.exists()) {
      return report;
    }

    
//    List<File> webappFilesByPluginXml = ParseUtil.getPluginXmlDeclaredFiles(configuration.getWebappProjectDirectory(), configuration.getFileFilter());
//
//    for (File declareWebappFile : webappFilesByPluginXml) {
//      if (!declareWebappFile.exists()) {
//        continue;
//      }
//
//      File destinationFile = IOUtil.getDestinationFile(configuration.getPluginProjectDirectory(),
//          configuration.getWebappProjectDirectory(), declareWebappFile);
//
//      if (!destinationFile.exists()) {
//        FileSyncStatus fileAdded = new FileAdded(declareWebappFile, destinationFile);
//        report.addReport(fileAdded, SyncStrategyReport.Direction.TO_PLUGIN);
//      }
//    }
    

    for (File itFile : IOUtil.deepListFiles(pluginPublicDirectory, configuration.getFileFilter())) {
      if (!Util.containsByName(configuration.getFilesDeclaredByPluginXML(), itFile)) {
        report.addReport(new FileCouldBeMissed(itFile), SyncStrategyReport.Direction.UNKNOWN);
      }
    }

    return report;
  }

}
