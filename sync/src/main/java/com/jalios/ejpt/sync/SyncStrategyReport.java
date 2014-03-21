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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;

/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public final class SyncStrategyReport {
  enum Direction {
    TO_WEBAPP, TO_PLUGIN, UNKNOWN;
  }

  private final Map<Direction, List<FileSyncStatus>> map;

  public SyncStrategyReport() {
    map = new HashMap<>();
    map.put(Direction.TO_WEBAPP, new ArrayList<FileSyncStatus>());
    map.put(Direction.TO_PLUGIN, new ArrayList<FileSyncStatus>());
    map.put(Direction.UNKNOWN, new ArrayList<FileSyncStatus>());
    
  }
  
  public void addReport(FileSyncStatus syncFile, Direction direction) {
    map.get(direction).add(syncFile);
  }
  
  public List<FileSyncStatus> getSyncFilesToWebapp() {
    return map.get(Direction.TO_WEBAPP);
  }
  
  public List<FileSyncStatus> getSyncFilesToPlugin() {
    return map.get(Direction.TO_PLUGIN);
  }
  
  public List<FileSyncStatus> getSyncFilesUnknown() {
    return map.get(Direction.UNKNOWN);
  }

  public int countSyncFilesToWebapp() {
    return map.get(Direction.TO_WEBAPP).size();
  }

  public int countSyncFilesToPlugin() {
    return map.get(Direction.TO_PLUGIN).size();
  }

  public void run(SyncExecutor executor) {
    executor.run(this);
  }
  
  public void mergeReport(SyncStrategyReport report) {
    map.get(Direction.TO_PLUGIN).addAll(report.getSyncFilesToPlugin());
    map.get(Direction.TO_WEBAPP).addAll(report.getSyncFilesToWebapp());
    map.get(Direction.UNKNOWN).addAll(report.getSyncFilesUnknown());
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for (FileSyncStatus sf : map.get(Direction.TO_WEBAPP)) {
      str.append("P->W : " + sf.getDestination().getAbsolutePath()).append("\n");
    }

    for (FileSyncStatus sf : map.get(Direction.TO_PLUGIN)) {
      str.append("W->P : " + sf.getDestination().getAbsolutePath()).append("\n");
    }
    

    for (FileSyncStatus sf : map.get(Direction.UNKNOWN)) {
      str.append("?->? : " + sf.getDestination().getAbsolutePath()).append("\n");
    }
    return str.toString();
  }

}
