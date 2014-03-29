package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public final class FileCouldBeMissed extends FileSyncStatus {

  public FileCouldBeMissed(File fileCouldMissed) {
    super(fileCouldMissed, fileCouldMissed);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}
