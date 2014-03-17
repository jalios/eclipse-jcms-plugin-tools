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
package com.jalios.ejpt.sync;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jalios.ejpt.sync.CopyExecutor;
import com.jalios.ejpt.sync.NewWebappFileStrategy;
import com.jalios.ejpt.sync.SyncStrategy;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncStrategyReport;
import com.jalios.ejpt.sync.SyncUtil;

/**
 * Test different sync situation
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
public class SyncTest {
  private static Logger logger = Logger.getLogger(SyncTest.class);
  private ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
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
    logger.debug("Create webapp project at " + webappProjectDirectory.getAbsolutePath());

    tmpPluginProjectTestDirectory = SyncUtil.createTempDir();
    pluginProjectDirectory = new File(tmpPluginProjectTestDirectory, "TestPlugin");
    pluginProjectDirectory.mkdirs();
    createLightPluginProjectStructure();
    logger.debug("Create plugin project at " + pluginProjectDirectory.getAbsolutePath());
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
    new File(pluginProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail").mkdirs();
    new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages").mkdirs();
    new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/test/plugin").mkdirs();

    try {
      // 9 files
      new File(pluginProjectDirectory, "plugins/TestPlugin/css/plugin.css").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/css/test.css").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/docs/changelog.txt").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/js/plugin.js").createNewFile();
      new File(pluginProjectDirectory, "plugins/TestPlugin/types/PortletQueryForeachDetail/template.jsp")
          .createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/en.prop").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/languages/fr.prop").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/plugins/TestPlugin/properties/plugin.xml").createNewFile();
      new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/test/plugin/BasicDataController.java")
          .createNewFile();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void syncNewPluginProject() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("strategy");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 9);
      assertEquals(report.countSyncFilesToPlugin(), 0);
    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  
  @Test
  public void syncNoChange() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("strategy");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 9);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      report.run(new CopyExecutor());

      report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 0);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      report.run(new CopyExecutor());

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncModifiedFileJcmsProject() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("strategy");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();

    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 9);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      report.run(new CopyExecutor());

      new File(pluginProjectDirectory, "plugins/TestPlugin/css/newStyle.css").createNewFile();
      report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 1);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Only true if the plugin project name is the same as plugin name 
   */
  @Test
  public void syncNewPublicPluginFileFromWebappProject() {
    SyncStrategy fileSyncStrategy = (SyncStrategy) context.getBean("strategy");
    SyncStrategy newWebappFileStrategy = new NewWebappFileStrategy();    
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();

    try {
      SyncStrategyReport report = fileSyncStrategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 9);
      assertEquals(report.countSyncFilesToPlugin(), 0);
      report.run(new CopyExecutor());

      new File(webappProjectDirectory, "plugins/TestPlugin/css/newStyle.css").createNewFile();
      report = newWebappFileStrategy.run(configuration);
      assertEquals(report.countSyncFilesToPlugin(), 1);      
      assertEquals(report.countSyncFilesToWebapp(), 0);
      report.run(new CopyExecutor());          
      
    } catch (SyncStrategyException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
