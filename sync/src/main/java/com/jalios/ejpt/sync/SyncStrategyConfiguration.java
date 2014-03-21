package com.jalios.ejpt.sync;

import java.io.File;

public final class SyncStrategyConfiguration {
  private File pluginDirectory;
  private File webappDirectory;
  private File config;
  
  private SyncStrategyConfiguration(Builder builder) {
    this.pluginDirectory = builder.pluginDirectory;
    this.webappDirectory = builder.webappDirectory;
    this.config = builder.config;
  }

  public File getPluginProjectRootDir() {
    return pluginDirectory;
  }

  public File getWebappProjectRootDir() {
    return webappDirectory;
  }
  
  public File getConfiguration() {
    return config;
  }
  
  public static class Builder {
    private File pluginDirectory;
    private File webappDirectory;
    private File config;

    public Builder(File ppRootDir, File wpRootDir) {
      this.pluginDirectory = ppRootDir;
      this.webappDirectory = wpRootDir;
    }

    public Builder configuration(File config) {
      this.config = config;
      return this;
    }

    public SyncStrategyConfiguration build() {
      return new SyncStrategyConfiguration(this);
    }
  }
}
