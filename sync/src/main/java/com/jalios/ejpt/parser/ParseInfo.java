/*
 GNU LESSER GENERAL PUBLIC LICENSE
 Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


 This version of the GNU Lesser General Public License incorporates
 the terms and conditions of version 3 of the GNU General Public
 License
 */
package com.jalios.ejpt.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public class ParseInfo {
  private String pluginName;
  private Set<String> filesPath = new HashSet<String>();

  public String getPluginName() {
    return pluginName;
  }

  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }

  public Set<String> getFilesPath() {
    return filesPath;
  }

  public void setFilesPath(Set<String> filesPath) {
    this.filesPath = filesPath;
  }
}
