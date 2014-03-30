package com.jalios.ejpt.sync.context;

import org.eclipse.core.resources.IProject;

import com.jalios.ejpt.sync.SyncStrategyConfiguration;

public class SyncContext {
  private IProject pluginProject;
  private IProject webappProject;
  private SyncStrategyConfiguration configuration;
  private boolean preview;
  
  public boolean isPreview() {
    return preview;
  }
  public void setPreview(boolean preview) {
    this.preview = preview;
  }
  public SyncStrategyConfiguration getConfiguration() {
    return configuration;
  }
  public void setConfiguration(SyncStrategyConfiguration configuration) {
    this.configuration = configuration;
  }
  public IProject getPluginProject() {
    return pluginProject;
  }
  public void setPluginProject(IProject pluginProject) {
    this.pluginProject = pluginProject;
  }
  public IProject getWebappProject() {
    return webappProject;
  }
  public void setWebappProject(IProject webappProject) {
    this.webappProject = webappProject;
  }
}
