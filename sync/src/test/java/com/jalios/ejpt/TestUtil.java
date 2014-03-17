package com.jalios.ejpt;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestUtil {
  protected ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

  public File getFileFromResource(String filename){
    return new File(this.getClass().getResource("/"+filename).getFile());
  }
}
