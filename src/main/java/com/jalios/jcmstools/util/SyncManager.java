package com.jalios.jcmstools.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;

import com.jalios.io.IOUtil;
import com.jalios.jcmstools.sync.BlackListFilter;
import com.jalios.jcmstools.sync.SyncContext;
import com.jalios.jcmstools.sync.SyncStatus;
import com.jalios.util.Util;

/**
 * 
 * @author xuan-tuong.le@jalios.com
 * 
 */
public class SyncManager {
  private static final SyncManager SINGLETON = new SyncManager();

  private enum CopyDirection {
    PLUGIN_TO_WEBAPP, WEBAPP_TO_PLUGIN
  };

  private MessageConsoleStream stream;

  /**
   * Private constructor
   */
  private SyncManager() {
    // nothing
  }

  /**
   * Get the singleton
   * 
   * @return SyncManager
   */
  public static SyncManager getInstance() {
    return SINGLETON;
  }

  public void setStream(MessageConsoleStream pStream) {
    this.stream = pStream;
  }

  public boolean checkSync(SyncContext context) {

    /*
     * TODO Check arguments differently if (args == null || args.size() < 3) {
     * usage("Bad configuration. Please check out the manual"); return false; }
     */

    stream.println("Sync configuration file : " + context.getConfigFilePath());

    if (context.isPreview()) {
      stream.println("------------------------------------------------------------");
      stream.println("THIS IS A PREVIEW ACTION. NOTHING WILL BE COMMITTED ...");
      stream.println("------------------------------------------------------------");
    }

    File pluginDir = new File(context.getPluginProjectPath());
    if (!pluginDir.exists()) {
      usage("I don't understand this project :-(. Please make sure that you've defined <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project");
      return false;
    }

    File webappDir = new File(context.getWebappProjectPath());
    if (!webappDir.exists()) {
      usage("Your JCMS Webapp project is undefined.");
      return false;
    }

    // Get subDirs
    List<File> subDirList = new ArrayList<File>();
    for (String subFolderPath : context.getPluginProjectSubFoldersPath()) {
      File file = new File(pluginDir, subFolderPath);
      if (!file.exists()) {
        stream.println("Warning! Sub directory '" + file + "' does not exist");
        continue;
      }
      subDirList.add(file);
    }
    if (subDirList.isEmpty()) {
      usage("No valid sub directories were specified.");
      return false;
    }

    stream.println("Plugin Dir: " + pluginDir);
    stream.println("Webapp Dir: " + webappDir);
    stream.println("Sub Dirs: " + subDirList);
    return true;
  }

  public void execute(SyncContext context) throws Exception {
    boolean checkOK = checkSync(context);

    if (!checkOK) {
      return;
    }
    sync(context);
  }

  private void sync(SyncContext context) {
    File pluginDir = new File(context.getPluginProjectPath());
    File webappDir = new File(context.getWebappProjectPath());
    List<File> subDirList = new ArrayList<File>();
    for (String subFolderPath : context.getPluginProjectSubFoldersPath()) {
      File file = new File(pluginDir, subFolderPath);
      if (!file.exists()) {
        stream.println("Warning! Sub directory '" + file + "' does not exist");
        continue;
      }
      subDirList.add(file);
    }

    try {
      stream.println("--------------");
      stream.println("Start sync...");
      syncDir(pluginDir, webappDir, subDirList, context);
      stream.println("--------------");
      syncPublicPluginFile(pluginDir, webappDir, context);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private SyncStatus syncDir(File pluginDir, File webappDir, List<File> subDirList,
      SyncContext context) throws IOException {
    SyncStatus status = new SyncStatus();

    // Get the fileList
    BlackListFilter filter = new BlackListFilter(context);
    List<File> fileList = new ArrayList<File>();
    if (Util.isEmpty(subDirList)) {
      fileList = IOUtil.deepListFiles(pluginDir, filter);
    } else {
      for (Iterator<File> it = subDirList.iterator(); it.hasNext();) {
        File itDir = (File) it.next();
        fileList.addAll(IOUtil.deepListFiles(itDir, filter));
      }
    }

    StringBuilder msgCopyPluginToWebapp = new StringBuilder();
    StringBuilder msgCopyWebappToPlugin = new StringBuilder();
    // Iterates over the src files
    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
      File srcFile = (File) it.next();

      // Get the related tgtFile
      String srcRelativePath = IOUtil.getRelativePath(pluginDir, srcFile);
      File tgtFile = new File(webappDir, srcRelativePath);
      String tgtRelativePath = IOUtil.getRelativePath(webappDir, tgtFile);

      // tgtFile is missing or older, copy srcFile -> tgtFile
      if (Util.isEmpty(tgtFile) || tgtFile.lastModified() < srcFile.lastModified()) {
        msgCopyPluginToWebapp.append("Plugin -> Webapp : copy " + tgtRelativePath + "\n");
        copy(srcFile, tgtFile, context, CopyDirection.PLUGIN_TO_WEBAPP);
        status.incNbCheckedFiles();
        continue;
      }

      if (tgtFile.lastModified() > srcFile.lastModified()) {
        // tgtFile is newser, copy tgtFile -> srcFile
        msgCopyWebappToPlugin.append("Webapp -> Plugin : copy " + tgtRelativePath + "\n");
        copy(tgtFile, srcFile, context, CopyDirection.WEBAPP_TO_PLUGIN);
        status.incNbCheckedFiles();
      }
    }
    stream.println(msgCopyWebappToPlugin.toString());
    stream.println(msgCopyPluginToWebapp.toString());
    stream.println(status.getNbCheckedFiles() + " files copied.");
    return status;
  }

  private void copy(File f1, File f2, SyncContext context, CopyDirection dir) throws IOException {
    if (context.isPreview()) {
      return;
    }
    IOUtil.copyFile(f1, f2);
    f2.setLastModified(f1.lastModified());
  }

  private SyncStatus syncPublicPluginFile(File pluginDir, File webappDir, SyncContext context)
      throws IOException {
    SyncStatus status = new SyncStatus();
    stream.println("Detect new file in " + "/plugins/" + pluginDir.getName());
    List<File> fileList = new ArrayList<File>();

    File pluginPublicDir = new File(webappDir + "/plugins/" + pluginDir.getName());
    BlackListFilter filter = new BlackListFilter(context);
    if (pluginPublicDir.isDirectory()) {
      fileList.addAll(IOUtil.deepListFiles(pluginPublicDir, filter));
    }

    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
      File srcFile = (File) it.next();

      // Get the related tgtFile
      String srcRelativePath = IOUtil.getRelativePath(webappDir, srcFile);
      File tgtFile = new File(pluginDir, srcRelativePath);

      // tgtFile is missing or older, copy srcFile -> tgtFile
      if (Util.isEmpty(tgtFile)) {
        copy(srcFile, tgtFile, context, CopyDirection.WEBAPP_TO_PLUGIN);
        stream.println("Webapp -> Plugin : " + srcRelativePath);
        status.incNbCheckedFiles();
      }
    }
    if (status.getNbCheckedFiles() == 0) {
      stream.println("No new files.");
    } else {
      stream.println(status.getNbCheckedFiles() + " new files.");
    }
    return status;
  }

  private void usage(String errorMsg) {
    if (Util.notEmpty(errorMsg)) {
      stream.println(errorMsg);
    }
  }

}
