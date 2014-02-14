package com.jalios.jcmstools.sync;

import java.util.ArrayList;
import java.util.List;

public class SyncContext {
  // by default, args empty, preview, standard/auto direction 
  private List<String> args = new ArrayList<String>();
  private String configFilePath;  
  private boolean preview = true;
  private boolean w2pDirection = false;
  private boolean p2wDirection = false;
  private String pluginProjectPath;
  private String webappProjectPath;
  private List<String> pluginProjectSubFoldersPath = new ArrayList<String>();
  
  public String getPluginProjectPath() {
    return pluginProjectPath;
  }

  public void setPluginProjectPath(String pluginProjectPath) {
    this.pluginProjectPath = pluginProjectPath;
  }

  public String getWebappProjectPath() {
    return webappProjectPath;
  }

  public void setWebappProjectPath(String webappProjectPath) {
    this.webappProjectPath = webappProjectPath;
  }

  public List<String> getPluginProjectSubFoldersPath() {
    return pluginProjectSubFoldersPath;
  }

  public void setPluginProjectSubFoldersPath(List<String> pluginProjectSubFoldersPath) {
    this.pluginProjectSubFoldersPath.clear();
    this.pluginProjectSubFoldersPath.addAll(pluginProjectSubFoldersPath);
  }

  public SyncContext(String configFilePath){
    this.configFilePath = configFilePath;
  }
  
  public void enableW2PDirection() {
    w2pDirection = true;
    p2wDirection = false;
  }

  public void enableP2WDirection() {
    w2pDirection = false;
    p2wDirection = true;
  }
  
  public void enableDefaultDirection(){
    w2pDirection = false;
    p2wDirection = false;
  }
  
  public boolean isW2PDirection(){
    return w2pDirection && !p2wDirection;
  }
  
  public boolean isP2WDirection(){
    return p2wDirection && !w2pDirection;
  }
  
  public boolean isDefaultDirection(){
    return !w2pDirection && !p2wDirection;
  }
  
  public void enablePreview(){
    preview = true;
  }
  
  public void disablePreview(){
    preview = false;
  }
  
  public boolean isPreview(){
    return preview;
  }  
  
  public String getConfigFilePath(){
    return configFilePath;
  }
  
  public void setArgs(List<String> pArgs){
    args.clear();
    args.addAll(pArgs);
  }
  
  public List<String> getArgs(){
    return args;
  }

}
