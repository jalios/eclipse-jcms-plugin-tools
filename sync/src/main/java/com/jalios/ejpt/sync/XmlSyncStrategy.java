package com.jalios.ejpt.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jalios.ejpt.parser.ParsePlugin;
import com.jalios.ejpt.parser.ParseUtil;
import com.jalios.ejpt.parser.PluginJCMS;

public class XmlSyncStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    File webappProjectDirectory = configuration.getWebappProjectRootDir();
    File pluginProjectDirectory = configuration.getPluginProjectRootDir();

    List<File> declaredPluginFiles = getPluginXmlDeclaredFiles(pluginProjectDirectory);

    for (File fileCouldBeDeclaredFromPluginProject : getFilesCouldBeDeclared(pluginProjectDirectory,
        declaredPluginFiles)) {
      SyncFile syncFile = new SyncFile(fileCouldBeDeclaredFromPluginProject, fileCouldBeDeclaredFromPluginProject,
          SyncFile.Nature.MISSED_DECLARE);
      report.addCopyReport(syncFile, SyncStrategyReport.Direction.UNKNOWN);
    }

    File privatePluginDirectory = ParseUtil.getPrivatePluginDirectory(pluginProjectDirectory);
    File pluginPublicDirectory = new File(webappProjectDirectory + "/plugins/" + privatePluginDirectory.getName());
    List<File> filesInPluginPublicDirectory = SyncUtil.deepListFiles(pluginPublicDirectory, new BlackListFilter());

    for (File fileInPPD : filesInPluginPublicDirectory) {
      if (!containsByName(declaredPluginFiles, fileInPPD)) {
        SyncFile syncFile = new SyncFile(fileInPPD, fileInPPD, SyncFile.Nature.MISSED_DECLARE);
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.UNKNOWN);
      }
    }

    for (File declaredPluginFile : declaredPluginFiles) {
      if (!declaredPluginFile.exists()) {
        SyncFile syncFile = new SyncFile(declaredPluginFile, declaredPluginFile, SyncFile.Nature.MISSED_DISK);
        // System.out.println(syncFile.getNatureOpName() + " ?->? : " +
        // declaredPluginFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.UNKNOWN);
        continue;
      }

      File webappFile = SyncUtil.getDestinationFile(webappProjectDirectory, pluginProjectDirectory, declaredPluginFile);

      if (!webappFile.exists()) {
        SyncFile syncFile = new SyncFile(declaredPluginFile, webappFile, SyncFile.Nature.ADDED);
        // System.out.println(syncFile.getNatureOpName() + " P->W : " +
        // declaredPluginFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() < declaredPluginFile.lastModified()) {
        SyncFile syncFile = new SyncFile(declaredPluginFile, webappFile, SyncFile.Nature.MODIFIED);
        // System.out.println(syncFile.getNatureOpName() + " P->W : " +
        // declaredPluginFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() > declaredPluginFile.lastModified()) {
        SyncFile syncFile = new SyncFile(webappFile, declaredPluginFile, SyncFile.Nature.MODIFIED);
        // System.out.println(syncFile.getNatureOpName() + " W->P : " +
        // webappFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.TO_PLUGIN);
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

  private List<File> getFilesCouldBeDeclared(File directory, List<File> declaredFiles) {
    List<File> filesCouldBeDeclared = new ArrayList<File>();

    List<File> physicalFiles = SyncUtil.deepListFiles(directory, new BlackListFilter());

    for (File physicalFile : physicalFiles) {
      if (!declaredFiles.contains(physicalFile)) {
        filesCouldBeDeclared.add(physicalFile);
      }
    }
    return filesCouldBeDeclared;
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
