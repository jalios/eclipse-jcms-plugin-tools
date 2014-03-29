package com.jalios.ejpt.sync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jalios.ejpt.TestUtil;
import com.jalios.ejpt.sync.filesyncstatus.FileAdded;
import com.jalios.ejpt.sync.filesyncstatus.FileCouldBeMissed;
import com.jalios.ejpt.sync.filesyncstatus.FileModified;
import com.jalios.ejpt.sync.filesyncstatus.FileNotFoundOnDisk;
import com.jalios.ejpt.sync.filesyncstatus.FileShouldBeDeclared;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.ejpt.sync.utils.IOUtil;

public class XmlSyncTest extends TestUtil {
  private File tmpWebappProjectTestDirectory;
  private File tmpPluginProjectTestDirectory;

  private File webappProjectDirectory;
  private File pluginProjectDirectory;

  @Before
  public void setUp() {
    tmpWebappProjectTestDirectory = IOUtil.createTempDir();
    webappProjectDirectory = new File(tmpWebappProjectTestDirectory, "webappproject");
    webappProjectDirectory.mkdirs();
    createLightJcmsProjectStructure();
    tmpPluginProjectTestDirectory = IOUtil.createTempDir();
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

  /**
   * 16 files
   */
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
    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
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
  public void checkFileAddedToWebapp() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 16);

      for (FileSyncStatus syncStatus : report.getSyncFilesToWebapp()) {
        assertTrue(syncStatus instanceof FileAdded);
      }

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  /*
  @Test
  public void checkFileAddedToPlugin() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 16);
      report.run(new CopyExecutor());

      for (FileSyncStatus syncStatus : report.getSyncFilesToWebapp()) {
        assertTrue(syncStatus instanceof FileAdded);
      }

      try {
        new File(webappProjectDirectory, "plugins/TestPlugin/jsp/new-jsp.jsp").createNewFile();
        FileUtils.deleteQuietly(FileUtils.getFile(new File(pluginProjectDirectory,
            "WEB-INF/data/types/MAC/MAC-templates.xml")));
        FileUtils.deleteQuietly(FileUtils.getFile(new File(webappProjectDirectory,
            "WEB-INF/data/types/MAC/MAC-templates.xml")));
      } catch (IOException e) {
        fail(e.getMessage());
      }
      report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToPlugin(), 1);

      FileSyncStatus ss = report.getSyncFilesToPlugin().iterator().next();
      assertTrue(ss instanceof FileAdded);
      assertTrue(ss.getDestination().equals(
          FileUtils.getFile(pluginProjectDirectory, "plugins/TestPlugin/jsp/new-jsp.jsp")));

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }
  */

  @Test
  public void checkFileModified() {
    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 16);

      try {
        FileUtils.touch(FileUtils.getFile(pluginProjectDirectory,
            "WEB-INF/classes/com/jalios/ejpt/test/BasicDataController.java"));
        FileUtils.touch(FileUtils.getFile(webappProjectDirectory, "WEB-INF/classes/com/jalios/ejpt/test/MacUtil.java"));
      } catch (IOException e) {
        fail("Cannot touch files to test this case");
      }

      report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 1);
      assertEquals(report.countSyncFilesToPlugin(), 1);

      for (FileSyncStatus syncStatus : report.getSyncFilesToWebapp()) {
        assertTrue(syncStatus instanceof FileModified);
      }
      for (FileSyncStatus syncStatus : report.getSyncFilesToPlugin()) {
        assertTrue(syncStatus instanceof FileModified);
      }

    } catch (SyncStrategyException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void checkFileNotFoundOnDisk() {
    try {
      FileUtils.copyFile(getFileFromResource("plugin-file-not-found.xml"), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 16);
      assertEquals(report.getSyncFilesUnknown().size(), 1);

      FileSyncStatus syncStatus = report.getSyncFilesUnknown().iterator().next();
      assertTrue(syncStatus instanceof FileNotFoundOnDisk);

    } catch (SyncStrategyException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void checkFileShoudBeDeclareInPluginXML() {
    try {
      new File(pluginProjectDirectory, "plugins/TestPlugin/css/css-not-found-in-plugin-xml.css").createNewFile();
    } catch (IOException e) {
      fail(e.getMessage());
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 16);
      assertEquals(report.getSyncFilesUnknown().size(), 1);

      FileSyncStatus syncStatus = report.getSyncFilesUnknown().iterator().next();
      assertTrue(syncStatus instanceof FileShouldBeDeclared);
    } catch (SyncStrategyException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void checkFileCouldBeMissedWebappProject() {

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      report.run(new CopyExecutor());
      assertEquals(report.countSyncFilesToWebapp(), 16);

      try {
        new File(webappProjectDirectory, "plugins/TestPlugin/css/css-could-missed-in-public-directory.css")
            .createNewFile();
      } catch (IOException e) {
        fail(e.getMessage());
      }
      report = strategy.run(configuration);
      assertEquals(report.getSyncFilesUnknown().size(), 1);

      FileSyncStatus syncStatus = report.getSyncFilesUnknown().iterator().next();
      assertTrue(syncStatus instanceof FileCouldBeMissed);
    } catch (SyncStrategyException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void syncExcludeJsp() {

    try {
      FileUtils.copyFile(getFileFromResource("plugin-nojsp.xml"), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 14);
      assertEquals(report.countSyncFilesToPlugin(), 0);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncJavaPackage() {

    try {
      FileUtils.copyFile(getFileFromResource("plugin-java-package.xml"), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.countSyncFilesToWebapp(), 14);
      assertEquals(report.countSyncFilesToPlugin(), 0);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncWithoutConfiguration() {

    try {
      new File(pluginProjectDirectory, ".settings").mkdirs();
      // 2 new files in configuration
      new File(pluginProjectDirectory, ".jcmsNaturePlugin").createNewFile();
      new File(pluginProjectDirectory, ".settings/foobar.xml").createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        tmpWebappProjectTestDirectory).configuration(null).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.getSyncFilesUnknown().size(), 2);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncWithExcludeOptions() {

    try {
      FileUtils.copyFile(getFileFromResource("sync.conf"), new File(tmpWebappProjectTestDirectory, "sync.conf"));
      new File(pluginProjectDirectory, ".settings").mkdirs();
      new File(pluginProjectDirectory, ".foo").mkdirs();

      // 3 files to exclude from sync
      new File(pluginProjectDirectory, ".jcmsNaturePlugin").createNewFile();
      new File(pluginProjectDirectory, ".anotherFile").createNewFile();
      new File(pluginProjectDirectory, ".settings/foobar.xml").createNewFile();
      new File(pluginProjectDirectory, ".foo/foobar.xml").createNewFile();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    SyncStrategy strategy = (SyncStrategy) context.getBean("sync");
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectDirectory,
        webappProjectDirectory).configuration(getFileFromResource("sync.conf")).build();
    try {
      SyncStrategyReport report = strategy.run(configuration);
      assertEquals(report.getSyncFilesUnknown().size(), 0);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

}
