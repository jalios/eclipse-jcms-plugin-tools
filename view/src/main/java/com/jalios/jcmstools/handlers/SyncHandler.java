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
package com.jalios.jcmstools.handlers;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.jalios.ejpt.sync.CopyExecutor;
import com.jalios.ejpt.sync.SyncStrategy;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncStrategyReport;
import com.jalios.ejpt.sync.XmlSyncStrategy;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.jcmstools.transversal.EJPTUtil;

/**
 * 
 * @author Xuan Tuong LE (lxtuong@gmail.com - @lxtuong)
 */
public class SyncHandler extends AbstractHandler {
  public static final String ID = "SyncHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.preview";
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private IProject pluginProject;
  private IProject webappProject;
  private MessageConsoleStream consoleStream;
  private boolean preview = false;

  /**
   * The constructor.
   */
  public SyncHandler() {
    
  }

  private boolean isPreview(Command cmd) {
    if (cmd == null) {
      return false;
    }

    return ID_PREVIEW_CMD.equals(cmd.getId());
  }

  /**
   * the command has been executed, so extract extract the needed information
   * from the application context.
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    MessageConsole console = EJPTUtil.findConsoleByName(CONSOLE_NAME);
    console.activate();
    consoleStream = console.newMessageStream();
    
    preview = isPreview(event.getCommand());

    pluginProject = EJPTUtil.getSyncProject(event);

    if (pluginProject == null) {
      consoleStream.println("Cannot detect plugin project. Make sure that you launch on the plugin project.");
      consoleStream.println("Operation aborted.");
      return null;
    }

    webappProject = EJPTUtil.getJcmsWebappProject(pluginProject);

    if (!initializeProjectsOK()) {
      return null;
    }

    // init configuration and sync context
    File config = EJPTUtil.getSyncConfigurationFilePath(webappProject);
    File ppPath = pluginProject.getLocation().toFile();
    File wpPath = new File(EJPTUtil.getWebappDirectoryPath(webappProject));
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(ppPath, wpPath).configuration(
        config).build();

    run(configuration);
    SyncHandlerUtil.refreshProject(pluginProject);
    SyncHandlerUtil.refreshProject(webappProject);

    return null;
  }

  private boolean initializeProjectsOK() {
    if (pluginProject == null) {
      consoleStream
          .println("Please define your project as a JCMSPlugin project by using <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project");
      consoleStream.println("Operation aborted.");
      return false;
    }

    if (webappProject == null) {
      consoleStream.println("Please link the JCMSPlugin project to a webapp project. Right-click on " + pluginProject
          + " > Properties > Project References > Choose the webapp project");
      consoleStream.println("Operation aborted.");
      return false;
    }

    return true;
  }

  private void run(SyncStrategyConfiguration configuration) {
    long start = System.currentTimeMillis();


    SyncStrategyReport report = new SyncStrategyReport();

    SyncStrategy sync = new XmlSyncStrategy();
    try {
      report = sync.run(configuration);
      if (!preview) {
        report.run(new CopyExecutor());
      }
      printReport(report);

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
    consoleStream.println("-----------------------------------------------------------");
    consoleStream.println("Sync took : " + (System.currentTimeMillis() - start) + " ms");

  }

  private void printReport(SyncStrategyReport report) {

    List<FileSyncStatus> syncFilesToPlugin = report.getSyncFilesToPlugin();
    List<FileSyncStatus> syncFilesToWebapp = report.getSyncFilesToWebapp();
    List<FileSyncStatus> syncFilesUnknown = report.getSyncFilesUnknown();

    if (preview) {
      consoleStream.println("-----------------------------------------------------------");
      consoleStream.println("This is only a PREVIEW status. NOTHING HAS BEEN DONE");
    }

    consoleStream.println("************");
    consoleStream.println("Summary : ");
    consoleStream.println("Date : " + Calendar.getInstance().getTime());
    consoleStream.println("Webapp directory : " + EJPTUtil.getWebappDirectoryPath(webappProject));
    consoleStream.println("Plugin directory : " + pluginProject.getLocation().toFile());
    consoleStream.println("W->P : " + syncFilesToPlugin.size() + " files ");
    consoleStream.println("P->W : " + syncFilesToWebapp.size() + " files ");
    consoleStream.println("?->? : " + syncFilesUnknown.size() + " files ");
    consoleStream.println("************");
    if (syncFilesUnknown.size() != 0) {
      consoleStream.println("Note on status :");
      consoleStream
          .println("(MISSED_DISK) Files declared in plugin.xml but don't exist on disk. Recheck your plugin.xml");
      consoleStream.println("(MISSED_DECLARE) Theses files should (?) be declared in plugin.xml but it's not the case");
    }
    consoleStream.println("-----------------------------------------------------------");
        
    for (FileSyncStatus syncStatus : syncFilesToPlugin) {
      consoleStream.println("(" + syncStatus.getStatusName() + ") " + "W->P : " + syncStatus.getDestination());
    }
    for (FileSyncStatus sf : syncFilesToWebapp) {
      consoleStream.println("(" + sf.getStatusName() + ") " + "P->W : " + sf.getDestination());
    }
    for (FileSyncStatus sf : syncFilesUnknown) {
      consoleStream.println("(" + sf.getStatusName() + ") " + "?->? : " + sf.getDestination());
    }
  }

}
