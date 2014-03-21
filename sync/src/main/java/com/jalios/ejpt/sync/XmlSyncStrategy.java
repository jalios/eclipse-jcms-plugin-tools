package com.jalios.ejpt.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
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
  private File webappProject;
  private File pluginProject;
  private FileFilter fileFilter;

  private void init(SyncStrategyConfiguration configuration) {
    webappProject = configuration.getWebappProjectRootDir();
    pluginProject = configuration.getPluginProjectRootDir();
    if (configuration.getConfiguration() == null) {
      fileFilter = new BlackListFilter.Builder().build();
    } else {
      SyncPropertyManager propertyManager = SyncPropertyManager.init(configuration.getConfiguration());
      fileFilter = new BlackListFilter.Builder().excludedDirs(propertyManager.getExcludedDirs())
          .excludedFiles(propertyManager.getExcludedFiles()).build();
    }
  }

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    init(configuration);
    SyncStrategyReport report = new SyncStrategyReport();
    List<File> declaredFiles = getPluginXmlDeclaredFiles(pluginProject);

    report.mergeReport(checkPhysicalFilesInPluginProject(declaredFiles));

    report.mergeReport(checkPluginPublicDirectoryOnWebapp(pluginProject, webappProject, declaredFiles));

    report.mergeReport(checkDeclaredFilesFromPluginXml(pluginProject, webappProject, declaredFiles));

    return report;
  }

  private SyncStrategyReport checkPluginPublicDirectoryOnWebapp(File pluginProjectDirectory,
      File webappProjectDirectory, List<File> declaredFiles) {
    SyncStrategyReport report = new SyncStrategyReport();
    File privatePluginDirectory = ParseUtil.getPrivatePluginDirectory(pluginProjectDirectory);
    File pluginPublicDirectory = new File(webappProjectDirectory + "/plugins/" + privatePluginDirectory.getName());

    if (!pluginPublicDirectory.exists()) {
      return report;
    }

    List<File> filesInPluginPublicDirectory = SyncUtil.deepListFiles(pluginPublicDirectory, fileFilter);
    for (File itFile : filesInPluginPublicDirectory) {
      if (!containsByName(declaredFiles, itFile)) {
        report.addReport(new FileCouldMissed(itFile), SyncStrategyReport.Direction.UNKNOWN);
      }
    }
    return report;
  }

  private SyncStrategyReport checkPhysicalFilesInPluginProject(List<File> declaredFiles) {
    SyncStrategyReport report = new SyncStrategyReport();
    List<File> filesShouldBeDeclared = new ArrayList<File>();

    List<File> physicalFiles = SyncUtil.deepListFiles(pluginProject, fileFilter);

    for (File physicalFile : physicalFiles) {
      if (!declaredFiles.contains(physicalFile)) {
        filesShouldBeDeclared.add(physicalFile);
      }
    }
    for (File itFile : filesShouldBeDeclared) {
      report.addReport(new FileShouldDeclare(itFile), SyncStrategyReport.Direction.UNKNOWN);
    }
    return report;
  }

  private SyncStrategyReport checkDeclaredFilesFromPluginXml(File pluginProjectDirectory, File webappProjectDirectory,
      List<File> declaredFiles) {
    SyncStrategyReport report = new SyncStrategyReport();

    for (File declaredPluginFile : declaredFiles) {
      if (!declaredPluginFile.exists()) {
        report.addReport(new FileNotFoundOnDisk(declaredPluginFile), SyncStrategyReport.Direction.UNKNOWN);
        continue;
      }

      File webappFile = SyncUtil.getDestinationFile(webappProjectDirectory, pluginProjectDirectory, declaredPluginFile);

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
          files.addAll(SyncUtil.deepListFiles(declaredFile, null));
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
