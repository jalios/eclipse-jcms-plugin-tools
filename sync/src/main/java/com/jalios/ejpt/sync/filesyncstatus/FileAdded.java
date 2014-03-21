package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileAdded extends FileSyncStatus {

  public FileAdded(File source, File destination) {
    super(source, destination);
  }

  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}
