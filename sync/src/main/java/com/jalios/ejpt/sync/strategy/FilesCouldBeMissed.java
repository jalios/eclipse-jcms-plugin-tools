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
package com.jalios.ejpt.sync.strategy;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.parser.ParseUtil;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.filesyncstatus.FileCouldBeMissed;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.report.SyncReport;
import com.jalios.ejpt.sync.report.SyncUnknownDirectionReport;
import com.jalios.ejpt.sync.utils.IOUtil;
import com.jalios.ejpt.sync.utils.Util;
/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public class FilesCouldBeMissed implements SyncStrategy {

  @Override
  public SyncReportManager run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncReportManager reportManager = new SyncReportManager();
    SyncReport unknownDirectionReport = new SyncUnknownDirectionReport();
    reportManager.addReport(unknownDirectionReport);

    List<File> filesInWebappPublicPluginDirectory = getFilesInWebappPublicPluginDirectory(configuration);
    for (File fileCouldBeMissed : filesInWebappPublicPluginDirectory) {
      if (Util.containsByName(configuration.getFilesExpectedByPluginXML(), fileCouldBeMissed)) {
        continue;
      }
      FileSyncStatus fileSyncStatus = new FileCouldBeMissed(fileCouldBeMissed);
      unknownDirectionReport.add(fileSyncStatus);
    }

    return reportManager;
  }

  private List<File> getFilesInWebappPublicPluginDirectory(SyncStrategyConfiguration configuration) {
    List<File> results = new LinkedList<File>();

    File privatePluginDirectory = ParseUtil.getPrivatePluginDirectory(configuration.getPluginProjectDirectory());
    if (!privatePluginDirectory.exists()) {
      return results;
    }

    File pluginPublicDirectory = new File(configuration.getWebappProjectDirectory(), "/plugins/"
        + privatePluginDirectory.getName());

    if (!pluginPublicDirectory.exists()) {
      return results;
    }
    results.addAll(IOUtil.deepListFiles(pluginPublicDirectory, configuration.getFileFilter()));

    return results;
  }

}
