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
    preview = isPreview(event.getCommand());

    initProjectsFromEvent(event);

    if (!initializeProjectsOK()) {
      return null;
    }

    // init configuration and sync context
    String cfPath = EJPTUtil.getSyncConfigurationFilePath(webappProject);
    File ppPath = pluginProject.getLocation().toFile();
    File wpPath = new File(EJPTUtil.getWebappDirectoryPath(webappProject));
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(ppPath, wpPath).conf(cfPath)
        .build();

    run(configuration);
    SyncHandlerUtil.refreshProject(pluginProject);
    SyncHandlerUtil.refreshProject(webappProject);

    return null;
  }

  private void initProjectsFromEvent(ExecutionEvent event) {
    pluginProject = EJPTUtil.getSyncProject(event);
    webappProject = EJPTUtil.getJcmsWebappProject(pluginProject);
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
    MessageConsole console = EJPTUtil.findConsoleByName(CONSOLE_NAME);
    console.activate();
    consoleStream = console.newMessageStream();

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
      consoleStream.println("This is only a PREVIEW status. I haven't done anything");
    }

    consoleStream.println("************");
    consoleStream.println("Summary : ");
    consoleStream.println("Webapp directory : " + EJPTUtil.getWebappDirectoryPath(webappProject));    
    consoleStream.println("Plugin directory : " + pluginProject.getLocation().toFile());        
    consoleStream.println("W->P : " + syncFilesToPlugin.size() + " files ");
    consoleStream.println("P->W : " + syncFilesToWebapp.size() + " files ");
    consoleStream.println("?->? : " + syncFilesUnknown.size() + " files ");
    consoleStream.println("************");
    if (syncFilesUnknown.size() != 0) {
      consoleStream.println("NOTE :");
      consoleStream.println("(MISSED_DISK) Files declared in plugin.xml but don't exist on disk. Recheck your plugin.xml");
      consoleStream.println("(MISSED_DECLARE) Theses files should (?) be declared in plugin.xml but it's not the case");
    }
    consoleStream.println("-----------------------------------------------------------");

    for (FileSyncStatus sf : syncFilesToPlugin) {
      consoleStream.println("(" + sf.getNatureOpName() + ") " + "W->P : " + sf.getTgt());
    }
    for (FileSyncStatus sf : syncFilesToWebapp) {
      consoleStream.println("(" + sf.getNatureOpName() + ") " + "P->W : " + sf.getTgt());
    }
    for (FileSyncStatus sf : syncFilesUnknown) {
      consoleStream.println("(" + sf.getNatureOpName() + ") " + "?->? : " + sf.getTgt());
    }
  }

}
