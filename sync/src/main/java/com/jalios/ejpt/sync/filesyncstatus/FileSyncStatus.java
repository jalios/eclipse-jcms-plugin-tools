package com.jalios.ejpt.sync.filesyncstatus;

import java.io.File;

public class FileSyncStatus {
  private File source;
  private File destination;
  
  public FileSyncStatus(File source, File destination) {
    this.source = source;
    this.destination = destination;
  }

  public File getSource() {
    return source;
  }

  public void setSource(File source) {
    this.source = source;
  }

  public File getDestination() {
    return destination;
  }

  public void setDestination(File destination) {
    this.destination = destination;
  }
}
