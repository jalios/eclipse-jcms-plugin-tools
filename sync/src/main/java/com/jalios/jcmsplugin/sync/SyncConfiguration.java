package com.jalios.jcmsplugin.sync;

import java.io.File;

public final class SyncConfiguration {
  private File ppRootDir;
  private File wpRootDir;
  private String confPath;
  private boolean preview = false;
  
  private SyncConfiguration(Builder b) {
    this.ppRootDir = b.ppRootDir;
    this.wpRootDir = b.wpRootDir;
    this.confPath = b.confPath;
  }

  public File getPluginProjectRootDir() {
    return ppRootDir;
  }

  public File getWebappProjectRootDir() {
    return wpRootDir;
  }
  
  public void enablePreview(){
    this.preview = true;
  }
 
  public boolean isPreviewEnabled(){
    return this.preview;
  }

  public static class Builder {
    private File ppRootDir;
    private File wpRootDir;
    private String confPath;

    public Builder(File ppRootDir, File wpRootDir) {
      this.ppRootDir = ppRootDir;
      this.wpRootDir = wpRootDir;
    }

    public Builder conf(String confPath) {
      this.confPath = confPath;
      return this;
    }

    public SyncConfiguration build() {
      return new SyncConfiguration(this);
    }
  }
}
