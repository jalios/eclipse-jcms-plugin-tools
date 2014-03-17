package com.jalios.ejpt;

import java.io.File;

public class TestUtil {

  public File getFileFromResource(String filename){
    return new File(this.getClass().getResource("/"+filename).getFile());
  }
}
