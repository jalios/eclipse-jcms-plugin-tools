package com.jalios.jcmstools.sync;

public class SyncStatus {

  private int nbCheckedFiles = 0;

  public int getNbCheckedFiles() {
    return nbCheckedFiles;
  }

  public void incNbCheckedFiles() {
    nbCheckedFiles++;
  }
  
}
