package com.jalios.ejpt.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jalios.ejpt.parser.ParsePlugin;
import com.jalios.ejpt.parser.PluginJCMS;

public class XmlSyncStrategy implements SyncStrategy {

  @Override
  public SyncStrategyReport run(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    File webappProjectDirectory = configuration.getWebappProjectRootDir();
    File pluginProjectDirectory = configuration.getPluginProjectRootDir();

    for (File declaredPluginFile : getPluginXmlDeclaredFiles(pluginProjectDirectory)) {
      
      if (!declaredPluginFile.exists()){
        SyncFile syncFile = new SyncFile(declaredPluginFile, declaredPluginFile, SyncFile.Nature.MISSED);
        //System.out.println("(Missed) ? -> ? : " + declaredPluginFile.getAbsolutePath());        
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.UNKNOWN);
        continue;
      }
      
      File webappFile = SyncUtil.getDestinationFile(webappProjectDirectory, pluginProjectDirectory, declaredPluginFile);

      if (!webappFile.exists()) {
        SyncFile syncFile = new SyncFile(declaredPluginFile, webappFile, SyncFile.Nature.ADDED);
        // System.out.println("(Added) P -> W : " + pluginFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() < declaredPluginFile.lastModified()) {
        SyncFile syncFile = new SyncFile(declaredPluginFile, webappFile, SyncFile.Nature.MODIFIED);
        // System.out.println("(Modified) P -> W : " + pluginFile.getPath());
        report.addCopyReport(syncFile, SyncStrategyReport.Direction.TO_WEBAPP);
        continue;
      }

      if (webappFile.lastModified() > declaredPluginFile.lastModified()) {
        SyncFile syncFile = new SyncFile(webappFile, declaredPluginFile, SyncFile.Nature.MODIFIED);
        // System.out.println("(Modified) W -> P : " + webappFile.getPath());
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

}
