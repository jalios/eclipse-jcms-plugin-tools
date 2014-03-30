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
package com.jalios.ejpt.sync;

import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.sync.executor.SyncExecutor;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.report.SyncReport;
import com.jalios.ejpt.sync.report.SyncToPluginReport;
import com.jalios.ejpt.sync.report.SyncToWebappReport;
import com.jalios.ejpt.sync.report.SyncUnknownDirectionReport;

/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public final class SyncReportManager {

  private final List<SyncReport> reports;

  public SyncReportManager() {
    reports = new LinkedList<SyncReport>();
  }

  public void addReport(SyncReport report) {
    reports.add(report);
  }

  public void mergeReport(SyncReportManager reportManager) {
    reports.addAll(reportManager.reports);
  }

  public List<FileSyncStatus> getSyncFilesToWebapp() {
    return getSyncFiles(SyncToWebappReport.class);
  }

  public List<FileSyncStatus> getSyncFilesToPlugin() {
    return getSyncFiles(SyncToPluginReport.class);
  }

  public List<FileSyncStatus> getSyncFilesUnknown() {
    return getSyncFiles(SyncUnknownDirectionReport.class);
  }

  private List<FileSyncStatus> getSyncFiles(Class<?> clazz) {
    List<FileSyncStatus> results = new LinkedList<FileSyncStatus>();
    for (SyncReport report : this.reports) {
      if (clazz.isInstance(report)) {
        results.addAll(report.getFilesSyncStatus());
      }
    }
    return results;
  }

  public void run(SyncExecutor executor) {
    executor.run(this);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for (FileSyncStatus sf : getSyncFilesToWebapp()) {
      str.append(sf.getStatusName() + " P->W : " + sf.getDestination().getAbsolutePath()).append("\n");
    }

    for (FileSyncStatus sf : getSyncFilesToPlugin()) {
      str.append(sf.getStatusName() + " W->P : " + sf.getDestination().getAbsolutePath()).append("\n");
    }

    for (FileSyncStatus sf : getSyncFilesUnknown()) {
      str.append(sf.getStatusName() + " ?->? : " + sf.getDestination().getAbsolutePath()).append("\n");
    }
    return str.toString();
  }

}
