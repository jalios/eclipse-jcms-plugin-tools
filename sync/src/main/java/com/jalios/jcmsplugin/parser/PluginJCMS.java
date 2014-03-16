package com.jalios.jcmsplugin.parser;

import java.util.HashSet;
import java.util.Set;

public class PluginJCMS {
  private Set<String> filesPath = new HashSet<String>();

  public Set<String> getFilesPath() {
    return filesPath;
  }

  public void setFilesPath(Set<String> filesPath) {
    this.filesPath = filesPath;
  }
}
