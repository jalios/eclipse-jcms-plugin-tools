package com.jalios.ejpt.sync.report;

import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;

public abstract class SyncReport {
  private List<FileSyncStatus> filesSyncStatus = new LinkedList<FileSyncStatus>();
  
  public void add(FileSyncStatus fileSyncStatus){
    filesSyncStatus.add(fileSyncStatus);
  }
  
  public List<FileSyncStatus> getFilesSyncStatus(){
    return this.filesSyncStatus;
  }
}
