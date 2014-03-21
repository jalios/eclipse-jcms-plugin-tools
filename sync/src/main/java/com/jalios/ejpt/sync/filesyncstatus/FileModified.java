package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileModified extends FileSyncStatus {

  public FileModified(File source, File destination) {
    super(source, destination);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}