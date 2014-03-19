package com.jalios.ejpt.sync;

import java.io.File;

public class SyncFile {
  private File source;
  private File destination;
  private Nature nature;
 

  enum Nature{
    ADDED, MODIFIED, MISSED_DISK, MISSED_DECLARE
  }
  
  public SyncFile(File src, File tgt) {
    this.source = src;
    this.destination = tgt;
  }
  
  public SyncFile(File src, File tgt, Nature nature) {
    this.source = src;
    this.destination = tgt;
    this.nature = nature;
  }

  public File getSrc() {
    return source;
  }

  public void setSrc(File src) {
    this.source = src;
  }

  public File getTgt() {
    return destination;
  }

  public void setTgt(File tgt) {
    this.destination = tgt;
  }
  
  public String getNatureOpName(){
    return this.nature.name();
  }
}
