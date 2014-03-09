package com.jalios.jcmsplugin.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BlackListFilter implements FileFilter {
  private Set<String> excludedDirs = new TreeSet<String>();
  private Set<String> excludedFiles = new TreeSet<String>();
  private List<String> excludedExtensions = Arrays.asList(".class");

  public BlackListFilter() {
    excludedDirs.add(".svn");    
  }

  public boolean accept(File file) {
    // ignore directories
    if (excludedDirs.contains(file.getName())) {
      return false;
    }

    // ignore files
    if (excludedFiles.contains(file.getName())) {
      return false;
    }

    // ignore extensions
    for (String excludedExt : excludedExtensions) {
      if (file.getName().endsWith(excludedExt)) {
        return false;
      }
    }

    // ignore regex patterns
    if (file.getName().matches(".*th-.*x.*")) {
      return false;
    }
    return true;
  }
}
