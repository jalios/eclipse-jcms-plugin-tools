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
package com.jalios.jcmsplugin.sync;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jalios.jcmsplugin.sync.Direction;
import com.jalios.jcmsplugin.sync.ISync;
import com.jalios.jcmsplugin.sync.SyncComputeResult;
import com.jalios.jcmsplugin.sync.SyncConfiguration;
import com.jalios.jcmsplugin.sync.SyncException;
import com.jalios.jcmsplugin.sync.SyncUtil;

/**
 * Test different sync situation
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
public class SyncTest {
  private ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
  private File tmpJcmsProject;
  private File tmpPluginProject;

  private File jcmsProject;
  private File pluginProject;

  @Before
  public void setUp() {
    tmpJcmsProject = SyncUtil.createTempDir();
    jcmsProject = new File(tmpJcmsProject, "jcmsproject");
    jcmsProject.mkdirs();
    createLightJcmsProject(jcmsProject);

    tmpPluginProject = SyncUtil.createTempDir();
    pluginProject = new File(tmpPluginProject, "pluginproject");
    pluginProject.mkdirs();
    createLightPluginProject(pluginProject);
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDirectory(tmpJcmsProject);
      FileUtils.deleteDirectory(tmpPluginProject);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void syncNewPluginProject() {
    // run a sync
    ISync basicComputeSync = (ISync) context.getBean("basicComputeSync");
    SyncConfiguration config = new SyncConfiguration();
    SyncComputeResult result = new SyncComputeResult();
    try {
      result = basicComputeSync.computeSync(jcmsProject, pluginProject, config, result);
      SyncUtil.runSync(result);

      // all files in new plugin project must go to the jcms webapp project
      assertEquals(result.getSyncFiles(Direction.TO_WEBAPP).size(), 9);

      // no files go in plugin project
      assertEquals(result.getSyncFiles(Direction.TO_PLUGIN).size(), 0);
    } catch (SyncException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncNoChange() {
    ISync basicComputeSync = (ISync) context.getBean("basicComputeSync");
    SyncConfiguration config = new SyncConfiguration();
    SyncComputeResult result1 = new SyncComputeResult();
    SyncComputeResult result2 = new SyncComputeResult();

    try {
      result1 = basicComputeSync.computeSync(jcmsProject, pluginProject, config, result1);
      SyncUtil.runSync(result1);

      // all files in new plugin project must go to the jcms webapp project
      assertEquals(result1.getSyncFiles(Direction.TO_WEBAPP).size(), 9);

      // no files go in plugin project
      assertEquals(result1.getSyncFiles(Direction.TO_PLUGIN).size(), 0);

      // sync again
      result2 = basicComputeSync.computeSync(jcmsProject, pluginProject, config, result2);
      SyncUtil.runSync(result2);
      assertEquals(result2.getSyncFiles(Direction.TO_WEBAPP).size(), 0);
      assertEquals(result2.getSyncFiles(Direction.TO_PLUGIN).size(), 0);

    } catch (SyncException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncException() {
    // TODO
  }

  @Test
  public void syncPreview() {
    // TODO
  }

  @Test
  public void syncNewFileInJcmsProject() {
    ISync basicComputeSync = (ISync) context.getBean("basicComputeSync");
    SyncConfiguration config = new SyncConfiguration();
    SyncComputeResult result1 = new SyncComputeResult();

    try {
      result1 = basicComputeSync.computeSync(jcmsProject, pluginProject, config, result1);
      SyncUtil.runSync(result1);

      // all files in new plugin project must go to the jcms webapp project
      assertEquals(result1.getSyncFiles(Direction.TO_WEBAPP).size(), 9);

      // no files go in plugin project
      assertEquals(result1.getSyncFiles(Direction.TO_PLUGIN).size(), 0);

      // file created in jcms project is not synced in basic compute sync
      new File(jcmsProject, "plugins/TestPlugin/css/newStyle.css").createNewFile();
      SyncComputeResult result2 = new SyncComputeResult();
      basicComputeSync.computeSync(jcmsProject, pluginProject, config, result2);
      SyncUtil.runSync(result2);
      assertEquals(result2.getSyncFiles(Direction.TO_PLUGIN).size(), 0);

      // file created in plugin project is still synced in basic compute sync
      new File(pluginProject, "plugins/TestPlugin/css/newStyle.css").createNewFile();
      SyncComputeResult result3 = new SyncComputeResult();
      result3 = basicComputeSync.computeSync(jcmsProject, pluginProject, config, result3);
      SyncUtil.runSync(result3);
      assertEquals(result3.getSyncFiles(Direction.TO_WEBAPP).size(), 1);

    } catch (SyncException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void createLightJcmsProject(File jcmsProjectRootDir) {
    new File(jcmsProjectRootDir, "admin").mkdirs();
    new File(jcmsProjectRootDir, "css").mkdirs();
    new File(jcmsProjectRootDir, "custom").mkdirs();
    new File(jcmsProjectRootDir, "feed").mkdirs();
    new File(jcmsProjectRootDir, "front").mkdirs();
    new File(jcmsProjectRootDir, "flash").mkdirs();
    new File(jcmsProjectRootDir, "images").mkdirs();
    new File(jcmsProjectRootDir, "jcore").mkdirs();
    new File(jcmsProjectRootDir, "js").mkdirs();
    new File(jcmsProjectRootDir, "types").mkdirs();
    new File(jcmsProjectRootDir, "WEB-INF/classes").mkdirs();
    new File(jcmsProjectRootDir, "work").mkdirs();
    try {
      new File(jcmsProjectRootDir, "display.jsp").createNewFile();
      new File(jcmsProjectRootDir, "edit.jsp").createNewFile();
      new File(jcmsProjectRootDir, "index.jsp").createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void createLightPluginProject(File pluginProjectRootDir) {

    new File(pluginProjectRootDir, "plugins/TestPlugin/css").mkdirs();
    new File(pluginProjectRootDir, "plugins/TestPlugin/docs").mkdirs();
    new File(pluginProjectRootDir, "plugins/TestPlugin/js").mkdirs();
    new File(pluginProjectRootDir, "plugins/TestPlugin/types/PortletQueryForeachDetail").mkdirs();
    new File(pluginProjectRootDir, "WEB-INF/plugins/TestPlugin/properties/languages").mkdirs();
    new File(pluginProjectRootDir, "WEB-INF/classes/com/jalios/test/plugin").mkdirs();

    try {
      // 9 files
      new File(pluginProjectRootDir, "plugins/TestPlugin/css/plugin.css").createNewFile();
      new File(pluginProjectRootDir, "plugins/TestPlugin/css/test.css").createNewFile();
      new File(pluginProjectRootDir, "plugins/TestPlugin/docs/changelog.txt").createNewFile();
      new File(pluginProjectRootDir, "plugins/TestPlugin/js/plugin.js").createNewFile();
      new File(pluginProjectRootDir, "plugins/TestPlugin/types/PortletQueryForeachDetail/template.jsp").createNewFile();
      new File(pluginProjectRootDir, "WEB-INF/plugins/TestPlugin/properties/languages/en.prop").createNewFile();
      new File(pluginProjectRootDir, "WEB-INF/plugins/TestPlugin/properties/languages/fr.prop").createNewFile();
      new File(pluginProjectRootDir, "WEB-INF/plugins/TestPlugin/properties/plugin.xml").createNewFile();
      new File(pluginProjectRootDir, "WEB-INF/classes/com/jalios/test/plugin/BasicDataController.java").createNewFile();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
