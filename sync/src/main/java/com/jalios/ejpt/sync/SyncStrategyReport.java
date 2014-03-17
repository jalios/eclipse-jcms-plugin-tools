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

/**
 * Sync compute result
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public final class SyncStrategyReport {
  enum Direction {
    TO_WEBAPP, TO_PLUGIN;
  }

  private final Map<Direction, List<SyncFile>> map;

  public SyncStrategyReport() {
    map = new HashMap<>();
    map.put(Direction.TO_WEBAPP, new ArrayList<SyncFile>());
    map.put(Direction.TO_PLUGIN, new ArrayList<SyncFile>());
  }

  public void addCopyReport(File source, File destination, Direction direction) {
    map.get(direction).add(new SyncFile(source, destination));
  }
  
  public List<SyncFile> getSyncFilesToWebapp() {
    return map.get(Direction.TO_WEBAPP);
  }
  
  public List<SyncFile> getSyncFilesToPlugin() {
    return map.get(Direction.TO_PLUGIN);
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

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for (SyncFile sf : map.get(Direction.TO_WEBAPP)) {
      str.append("P->W : " + sf.getTgt().getAbsolutePath()).append("\n");
    }

    for (SyncFile sf : map.get(Direction.TO_PLUGIN)) {
      str.append("W->P : " + sf.getTgt().getAbsolutePath()).append("\n");
    }
    return str.toString();
  }

}
