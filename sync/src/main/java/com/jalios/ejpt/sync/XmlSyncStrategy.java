package com.jalios.ejpt.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.jalios.ejpt.parser.ParsePlugin;
import com.jalios.ejpt.parser.ParseUtil;
import com.jalios.ejpt.parser.PluginJCMS;
import com.jalios.ejpt.sync.filesyncstatus.FileAdded;
import com.jalios.ejpt.sync.filesyncstatus.FileCouldMissed;
import com.jalios.ejpt.sync.filesyncstatus.FileModified;
import com.jalios.ejpt.sync.filesyncstatus.FileNotFoundOnDisk;
import com.jalios.ejpt.sync.filesyncstatus.FileShouldDeclare;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;

public class XmlSyncStrategy implements SyncStrategy {
  // global info for different internal strategies
  private FileFilter fileFilter;
  private List<File> pluginXmlDeclaredFiles;

  private void cacheInfo(SyncStrategyConfiguration configuration) {
    pluginXmlDeclaredFiles = getPluginXmlDeclaredFiles(configuration.getPluginProjectDirectory());
    fileFilter = configuration.getFileFilter();
  }

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    cacheInfo(configuration);
    SyncStrategyReport finalReport = new SyncStrategyReport();

    List<SyncStrategy> internalStrategies = new LinkedList<SyncStrategy>();
    internalStrategies.add(new FilesShoudBeDeclaredStrategy());
    internalStrategies.add(new FilesFromPluginXmlStrategy());
    internalStrategies.add(new NewFileFromWebappDirectoryStrategy());

    for (SyncStrategy syncStrategy : internalStrategies) {
      finalReport.mergeReport(syncStrategy.run(configuration));
    }

    return finalReport;
  }

  private class FilesShoudBeDeclaredStrategy implements SyncStrategy {

    @Override
    public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
      SyncStrategyReport report = new SyncStrategyReport();
      List<File> filesShouldBeDeclared = new ArrayList<File>();

      List<File> physicalFiles = SyncUtil.deepListFiles(configuration.getPluginProjectDirectory(), fileFilter);

      for (File physicalFile : physicalFiles) {
        if (!pluginXmlDeclaredFiles.contains(physicalFile)) {
          filesShouldBeDeclared.add(physicalFile);
        }
      }
      for (File itFile : filesShouldBeDeclared) {
        report.addReport(new FileShouldDeclare(itFile), SyncStrategyReport.Direction.UNKNOWN);
      }
      return report;
    }

  }

  private class NewFileFromWebappDirectoryStrategy implements SyncStrategy {

    @Override
    public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
      SyncStrategyReport report = new SyncStrategyReport();
      File privatePluginDirectory = ParseUtil.getPrivatePluginDirectory(configuration.getPluginProjectDirectory());
      File pluginPublicDirectory = new File(configuration.getWebappProjectDirectory(), "/plugins/"
          + privatePluginDirectory.getName());

      if (!pluginPublicDirectory.exists()) {
        return report;
      }

      List<File> webappFilesByPluginXml = getPluginXmlDeclaredFiles(configuration.getWebappProjectDirectory());

      for (File declareWebappFile : webappFilesByPluginXml) {
        File pluginFile = SyncUtil.getDestinationFile(configuration.getPluginProjectDirectory(),
            configuration.getWebappProjectDirectory(), declareWebappFile);

        if (!pluginFile.exists()) {
          FileSyncStatus fileAdded = new FileAdded(declareWebappFile, pluginFile);
          report.addReport(fileAdded, SyncStrategyReport.Direction.TO_PLUGIN);
        }
      }

      for (File itFile : SyncUtil.deepListFiles(pluginPublicDirectory, fileFilter)) {
        if (!containsByName(pluginXmlDeclaredFiles, itFile)) {
          report.addReport(new FileCouldMissed(itFile), SyncStrategyReport.Direction.UNKNOWN);
        }
      }

      return report;
    }

  }

  private class FilesFromPluginXmlStrategy implements SyncStrategy {

    @Override
    public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
      SyncStrategyReport report = new SyncStrategyReport();

      for (File declaredPluginFile : pluginXmlDeclaredFiles) {
        if (!declaredPluginFile.exists()) {
          report.addReport(new FileNotFoundOnDisk(declaredPluginFile), SyncStrategyReport.Direction.UNKNOWN);
          continue;
        }

        File webappFile = SyncUtil.getDestinationFile(configuration.getWebappProjectDirectory(),
            configuration.getPluginProjectDirectory(), declaredPluginFile);

        if (!webappFile.exists()) {
          FileSyncStatus fileAdded = new FileAdded(declaredPluginFile, webappFile);
          report.addReport(fileAdded, SyncStrategyReport.Direction.TO_WEBAPP);
          continue;
        }

        if (webappFile.lastModified() < declaredPluginFile.lastModified()) {
          FileSyncStatus fileModified = new FileModified(declaredPluginFile, webappFile);
          report.addReport(fileModified, SyncStrategyReport.Direction.TO_WEBAPP);
          continue;
        }

        if (webappFile.lastModified() > declaredPluginFile.lastModified()) {
          FileSyncStatus fileModified = new FileModified(webappFile, declaredPluginFile);
          report.addReport(fileModified, SyncStrategyReport.Direction.TO_PLUGIN);
        }

      }
      return report;
    }

  }

  /**
   * Get files declared in plugin.xml
   * 
   * @param pluginProjectDirectory
   *          plugin projet directory
   * @return List&ltFile&gt
   */
  private List<File> getPluginXmlDeclaredFiles(File pluginProjectDirectory) {
    // check status from plugin.xml
    ParsePlugin parser = ParsePlugin.getParser();
    PluginJCMS info = parser.analyze(pluginProjectDirectory);

    List<File> files = new ArrayList<File>();

    if (info != null) {
      for (String declaredFilePath : info.getFilesPath()) {
        File declaredFile = new File(pluginProjectDirectory, declaredFilePath);
        if (declaredFile.isDirectory()) {
          files.addAll(SyncUtil.deepListFiles(declaredFile, fileFilter));
          continue;
        }

        files.add(new File(pluginProjectDirectory, declaredFilePath));
      }
    }
    return files;
  }

  private boolean containsByName(List<File> files, File anotherFile) {
    for (File file : files) {
      if (file.getName().equals(anotherFile.getName())) {
        return true;
      }
    }
    return false;
  }

}
