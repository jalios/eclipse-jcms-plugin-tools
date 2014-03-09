package com.jalios.jcmsplugin.sync;

import java.io.File;

public class SyncFile {
  private File src;
  private File tgt;

  public SyncFile(File src, File tgt) {
    this.src = src;
    this.tgt = tgt;
  }

  public File getSrc() {
    return src;
  }

  public void setSrc(File src) {
    this.src = src;
  }

  public File getTgt() {
    return tgt;
  }

  public void setTgt(File tgt) {
    this.tgt = tgt;
  }
}
