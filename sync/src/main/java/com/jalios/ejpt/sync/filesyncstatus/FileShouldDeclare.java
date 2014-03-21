package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileShouldDeclare extends FileSyncStatus {

  public FileShouldDeclare(File fileShoudBeDeclared) {
    super(fileShoudBeDeclared, fileShoudBeDeclared);
  }
  
  @Override
  public String getStatusName() {    
    return this.getClass().getSimpleName();
  }
}

