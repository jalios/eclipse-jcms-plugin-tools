package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileShouldBeDeclared extends FileSyncStatus {

  public FileShouldBeDeclared(File fileShoudBeDeclared) {
    super(fileShoudBeDeclared, fileShoudBeDeclared);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}

