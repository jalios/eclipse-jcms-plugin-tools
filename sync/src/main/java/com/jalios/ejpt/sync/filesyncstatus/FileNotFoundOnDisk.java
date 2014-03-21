package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileNotFoundOnDisk extends FileSyncStatus {

  public FileNotFoundOnDisk(File fileNotFound) {
    super(fileNotFound, fileNotFound);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}
