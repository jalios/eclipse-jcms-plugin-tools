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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 *
 */
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
