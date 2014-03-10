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
package com.jalios.jcmsplugin.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Sync compute result 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 * 
 */
public final class SyncComputeResult {
  private Map<Direction, List<SyncFile>> map;

  public SyncComputeResult() {
    map = new HashMap<>();
    map.put(Direction.TO_WEBAPP, new ArrayList<SyncFile>());
    map.put(Direction.TO_PLUGIN, new ArrayList<SyncFile>());
  }

  public List<SyncFile> getSyncFiles(Direction dir) {
    return map.get(dir);
  }

  public void addSyncFiles(File filePluginProject, Direction dir, File fileJcmsProject) {
    map.get(dir).add(new SyncFile(filePluginProject, fileJcmsProject));
  }

}
