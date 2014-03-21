package com.jalios.ejpt.parser;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jalios.ejpt.TestUtil;
import com.jalios.ejpt.sync.SyncUtil;

public class ParserTest extends TestUtil{

  private File tmpWebappProjectTestDirectory;

  private File webappProjectDirectory;

  @Before
  public void setUp() {
    tmpWebappProjectTestDirectory = SyncUtil.createTempDir();
    webappProjectDirectory = new File(tmpWebappProjectTestDirectory, "webappproject");
    webappProjectDirectory.mkdirs();
    createLightJcmsProjectStructure();
    System.out.println(webappProjectDirectory.getAbsolutePath());

  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(tmpWebappProjectTestDirectory);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }

  private void createLightJcmsProjectStructure() {
    new File(webappProjectDirectory, "admin").mkdirs();
    new File(webappProjectDirectory, "css").mkdirs();
    new File(webappProjectDirectory, "custom").mkdirs();
    new File(webappProjectDirectory, "feed").mkdirs();
    new File(webappProjectDirectory, "front").mkdirs();
    new File(webappProjectDirectory, "flash").mkdirs();
    new File(webappProjectDirectory, "images").mkdirs();
    new File(webappProjectDirectory, "jcore").mkdirs();
    new File(webappProjectDirectory, "js").mkdirs();
    new File(webappProjectDirectory, "types").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/classes").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/jalios").mkdirs();    
    new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/css").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/docs").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/js").mkdirs();
    new File(webappProjectDirectory, "plugins/TestPlugin/jsp").mkdirs();    
    new File(webappProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages").mkdirs();
    new File(webappProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test").mkdirs();
    new File(webappProjectDirectory, "work").mkdirs();
    try {
      new File(webappProjectDirectory, "plugins/TestPlugin/css/plugin.css").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/css/test.css").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/docs/changelog.txt").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/js/plugin.js").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail/template.jsp")
          .createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/jsp/home.jsp").createNewFile();
      new File(webappProjectDirectory, "plugins/TestPlugin/jsp/target.jsp").createNewFile();
      new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/en.prop").createNewFile();
      new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/fr.prop").createNewFile();      
      new File(webappProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test/BasicDataController.java")
          .createNewFile();
      new File(webappProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test/MacUtil.java")
      .createNewFile();   
      new File(webappProjectDirectory, "display.jsp").createNewFile();
      new File(webappProjectDirectory, "edit.jsp").createNewFile();
      new File(webappProjectDirectory, "index.jsp").createNewFile();
      
      FileUtils.copyFile(getFileFromResource("plugin-jx.xml"), new File(webappProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));      
      FileUtils.copyFile(getFileFromResource("jcms-plugin-1.6.dtd"), new File(webappProjectDirectory,
          "WEB-INF/jalios/jcms-plugin-1.6.dtd"));            
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void loadPluginXML() {
    // init a jcms structure with a plugin
    ParsePlugin parser = ParsePlugin.getParser();    
    PluginJCMS testPlugin = parser.analyze(webappProjectDirectory.getAbsolutePath(), "TestPlugin");
    assertNotNull(testPlugin);
    /*
    for (String filePath : testPlugin.getFilesPath()){
      System.out.println(filePath);
    }
    */
  }

}
