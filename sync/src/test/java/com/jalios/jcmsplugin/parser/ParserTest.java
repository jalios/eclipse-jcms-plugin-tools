package com.jalios.jcmsplugin.parser;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jalios.jcmsplugin.sync.SyncUtil;

public class ParserTest {

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
    System.out.println(webappProjectDirectory.getAbsolutePath());

    tmpPluginProjectTestDirectory = SyncUtil.createTempDir();
    pluginProjectDirectory = new File(tmpPluginProjectTestDirectory, "TestPlugin");
    pluginProjectDirectory.mkdirs();
    createLightPluginProjectStructure();
    System.out.println(pluginProjectDirectory.getAbsolutePath());

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
    new File(webappProjectDirectory, "WEB-INF/plugins/TestPlugin").mkdirs();
    
    new File(webappProjectDirectory, "work").mkdirs();
    try {
      URL url = this.getClass().getResource("/plugin.xml");
      FileUtils.copyFile(new File(url.getFile()), new File(webappProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
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

      URL url = this.getClass().getResource("/plugin.xml");
      FileUtils.copyFile(new File(url.getFile()), new File(pluginProjectDirectory,
          "WEB-INF/plugins/TestPlugin/plugin.xml"));
      new File(pluginProjectDirectory, "WEB-INF/classes/com/jalios/test/plugin/BasicDataController.java")
          .createNewFile();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void loadPluginXML() {
    // init a jcms structure with a plugin
    ParsePlugin parser = ParsePlugin.getParser();
    File dtdFile = new File(this.getClass().getResource("/jcms-plugin-1.4.dtd").getFile());
    PluginJCMS testPlugin = parser.analyze(webappProjectDirectory.getAbsolutePath(), "TestPlugin",
        new TestEntityResolver(dtdFile));
    assertNotNull(testPlugin);
    for (String file : testPlugin.getFilesPath()){
      System.out.println(file);
    }

    // TODO
    // assertEquals(testPlugin.getPluginFiles().size(), 10);
    // assertEquals(testPlugin.getPrivateFiles().size(), 10);
    // assertEquals(testPlugin.getWebappFiles().size(), 10);

  }

}
