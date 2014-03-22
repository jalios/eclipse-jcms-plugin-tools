package com.jalios.ejpt.sync;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.jalios.ejpt.TestUtil;

public class BlackListFilterTest extends TestUtil{
  private File tmpWebappProjectTestDirectory;

  private File webappProjectDirectory;

  @Before
  public void setUp() {
    tmpWebappProjectTestDirectory = SyncUtil.createTempDir();
    webappProjectDirectory = new File(tmpWebappProjectTestDirectory, "webappproject");
    webappProjectDirectory.mkdirs();
    createDummyDirectory();
    

  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(tmpWebappProjectTestDirectory);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void createDummyDirectory() {
    new File(webappProjectDirectory, ".git").mkdirs();        
    new File(webappProjectDirectory, ".svn").mkdirs();    
    new File(webappProjectDirectory, "admin").mkdirs();
    new File(webappProjectDirectory, "types").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/.svn").mkdirs();    
    new File(webappProjectDirectory, "WEB-INF/classes").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/jalios").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/css").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/css/.svn").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/css/.svn/prop-base").mkdirs(); 
    try {
      new File(webappProjectDirectory, "plugins/TestPlugin/css/.svn/all-wcprops").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/css/.svn/entries").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/css/plugin.css").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/css/test.css").createNewFile();
      
      FileUtils.copyFile(getFileFromResource("jcms-plugin-1.6.dtd"), new File(webappProjectDirectory,
          "WEB-INF/jalios/jcms-plugin-1.6.dtd"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  

  @Test
  public void ignoreSVN() {
    // init a jcms structure with a plugin
    BlackListFilter filter = new BlackListFilter.Builder().build();
    List<File> files = new LinkedList<>();
    for (File itFile : SyncUtil.deepListFiles(webappProjectDirectory, filter)){     
      files.add(itFile);
    }
    
    for (File itFile : files){
      assertFalse(itFile.getPath().matches(".*.svn.*"));
    }
  }
}
