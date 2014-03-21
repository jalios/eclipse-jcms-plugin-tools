package com.jalios.ejpt.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BlackListFilter implements FileFilter {
  private List<String> excludedDirs = new LinkedList<String>();
  private List<String> excludedFiles = new LinkedList<String>();
  private List<String> excludedExtensions = Arrays.asList(".class");

  private BlackListFilter(Builder builder) {
    // default
    excludedDirs.add(".svn");
    excludedDirs.add(".git");
    excludedFiles.add(".project");
    excludedFiles.add(".externalToolsBuilders");

    // options
    excludedDirs.addAll(builder.excludedDirs);
    excludedFiles.addAll(builder.excludedFiles);

  }

  public static class Builder {
    private List<String> excludedDirs = new LinkedList<String>();
    private List<String> excludedFiles = new LinkedList<String>();

    public Builder excludedDirs(List<String> directories) {
      excludedDirs.addAll(directories);
      return this;
    }

    public Builder excludedFiles(List<String> files) {
      excludedFiles.addAll(files);
      return this;
    }

    public BlackListFilter build() {
      return new BlackListFilter(this);
    }
  }

  public boolean accept(File file) {
    if (excludedDirs.contains(file.getName())) {
      return false;
    }

    if (excludedFiles.contains(file.getName())) {
      return false;
    }

    for (String excludedExt : excludedExtensions) {
      if (file.getName().endsWith(excludedExt)) {
        return false;
      }
    }

    if (file.getName().matches(".*th-.*x.*")) {
      return false;
    }
    return true;
  }
}
