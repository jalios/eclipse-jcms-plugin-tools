package com.jalios.ejpt.sync;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jalios.ejpt.TestUtil;

public class XmlSyncTest extends TestUtil {
  private File tmpWebappProjectTestDirectory;
  private File tmpPluginProjectTestDirectory;

  private File webappProjectDirectory;
  private File pluginProjectDirectory;

  @Before
  public void setUp() {
    tmpWebappProjectTestDirectory = SyncUtil.createTempDir();
    webappProjectDirectory = new File(tmpWebappProjectTestDirectory, "webappproject");
    webappProjectDirectory.mkdirs();
    createLightJcmsProjectStructure();

    tmpPluginProjectTestDirectory = SyncUtil.createTempDir();
    pluginProjectDirectory = new File(tmpPluginProjectTestDirectory, "TestPluginRoot");
    pluginProjectDirectory.mkdirs();
    createLightPluginProjectStructure();
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(tmpWebappProjectTestDirectory);
      FileUtils.deleteDirectory(tmpPluginProjectTestDirectory);
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
    new File(webappProjectDirectory, "work").mkdirs();
    try {
      new File(webappProjectDirectory, "display.jsp").createNewFile();
      new File(webappProjectDirectory, "edit.jsp").createNewFile();
      new File(webappProjectDirectory, "index.jsp").createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createLightPluginProjectStructure() {
    new File(pluginProjectDirectory, "plugins/TestPlugin/css").mkdirs();
    new File(pluginProjectDirectory, "plugins/TestPlugin/docs").mkdirs();
    new File(pluginProjectDirectory, "plugins/TestPlugin/js").mkdirs();
    new File(pluginProjectDirectory, "plugins/TestPlugin/jsp").mkdirs();
    new File(pluginProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail").mkdirs();
    new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages").mkdirs();
    new File(pluginProjectDirectory, "WEB-INF/data/types/MAC").mkdirs();
    new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test").mkdirs();
    new File(pluginProjectDirectory, "types/MAC").mkdirs();

    try {
      // public files

      // css & js : 4
      new File(pluginProjectDirectory, "plugins/TestPlugin/css/plugin1.css").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/css/plugin2.less").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/docs/changelog.txt").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/js/plugin.js").createNewFile();

      // types : 4
      new File(pluginProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail/template.jsp")
          .createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/data/types/MAC/MAC.xml").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/data/types/MAC/MAC-templates.xml").createNewFile();
      new File(pluginProjectDirectory, "types/MAC/doMACFullDisplay.jsp").createNewFile();

      // jsp : 2
      new File(pluginProjectDirectory, "plugins/TestPlugin/jsp/home.jsp").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/jsp/content.jsp").createNewFile();

      // end - public files

      // private files : 4
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/en.prop").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/fr.prop").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/plugin.prop").createNewFile();
      FileUtils.copyFile(getFileFromResource("plugin.xml"), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));

      // java files : 2
      new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test/BasicDataController.java").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test/MacUtil.java").createNewFile();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void syncNewPluginProject() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("xmlStrategy");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 16);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      
      report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 0);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      
    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }
  
  @Test
  public void syncNoJsp() {
    
    try {
      FileUtils.copyFile(getFileFromResource("plugin-nojsp.xml"), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    SyncStrategy strategy = (SyncStrategy) context.getBean("xmlStrategy");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 14);
      assertEquals(report.countSyncFilesToPlugin(), 0);      
      
    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

}
