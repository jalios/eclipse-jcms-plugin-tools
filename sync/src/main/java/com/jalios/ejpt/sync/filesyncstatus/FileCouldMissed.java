package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public final class FileCouldMissed extends FileSyncStatus {

  public FileCouldMissed(File fileCouldMissed) {
    super(fileCouldMissed, fileCouldMissed);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}
