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

import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.filesyncstatus.FileAdded;
import com.jalios.ejpt.sync.filesyncstatus.FileModified;
import com.jalios.ejpt.sync.filesyncstatus.FileNotFoundOnDisk;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.report.SyncReport;
import com.jalios.ejpt.sync.report.SyncToPluginReport;
import com.jalios.ejpt.sync.report.SyncToWebappReport;
import com.jalios.ejpt.sync.report.SyncUnknownDirectionReport;
import com.jalios.ejpt.sync.utils.IOUtil;
/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public class FilesExpectedByPluginXml implements SyncStrategy {

  @Override
  public SyncReportManager run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncReportManager reportManager = new SyncReportManager();    
    SyncReport unknownDirectionReport = new SyncUnknownDirectionReport();
    SyncReport toWebappReport = new SyncToWebappReport();
    SyncReport toPluginReport = new SyncToPluginReport();
    reportManager.addReport(unknownDirectionReport);
    reportManager.addReport(toWebappReport);
    reportManager.addReport(toPluginReport);

    for (File expectedFile : configuration.getFilesExpectedByPluginXML()) {
      if (!expectedFile.exists()) {
        FileSyncStatus fileNotFound = new FileNotFoundOnDisk(expectedFile);
        unknownDirectionReport.add(fileNotFound);
        continue;
      }

      File webappFile = IOUtil.getDestinationFile(configuration.getWebappProjectDirectory(),
          configuration.getPluginProjectDirectory(), expectedFile);

      if (!webappFile.exists()) {
        FileSyncStatus fileAdded = new FileAdded(expectedFile, webappFile);
        toWebappReport.add(fileAdded);
        continue;
      }

      if (webappFile.lastModified() < expectedFile.lastModified()) {
        FileSyncStatus fileModified = new FileModified(expectedFile, webappFile);
        toWebappReport.add(fileModified);
        continue;
      }

      if (webappFile.lastModified() > expectedFile.lastModified()) {
        FileSyncStatus fileModified = new FileModified(webappFile, expectedFile);
        toPluginReport.add(fileModified);
      }

    }
    return reportManager;
  }

}