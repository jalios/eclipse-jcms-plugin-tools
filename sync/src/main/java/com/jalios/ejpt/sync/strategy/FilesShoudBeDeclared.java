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
import java.util.List;

import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.filesyncstatus.FileShouldBeDeclared;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.report.SyncReport;
import com.jalios.ejpt.sync.report.SyncUnknownDirectionReport;
import com.jalios.ejpt.sync.utils.IOUtil;

/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public class FilesShoudBeDeclared implements SyncStrategy {

  @Override
  public SyncReportManager run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncReportManager reportManager = new SyncReportManager();
    SyncReport unknownDirectionReport = new SyncUnknownDirectionReport();
    reportManager.addReport(unknownDirectionReport);

    List<File> filesExpectedByPluginXML = configuration.getFilesExpectedByPluginXML();
    List<File> physicalFilesInPluginProject = IOUtil.deepListFiles(configuration.getPluginProjectDirectory(),
        configuration.getFileFilter());

    for (File physicalFile : physicalFilesInPluginProject) {
      if (filesExpectedByPluginXML.contains(physicalFile)) {
        continue;
      }
      FileSyncStatus fileSyncStatus = new FileShouldBeDeclared(physicalFile);
      unknownDirectionReport.add(fileSyncStatus);

    }
    return reportManager;
  }

}