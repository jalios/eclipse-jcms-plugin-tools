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

import java.util.Calendar;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.jalios.ejpt.sync.CopyExecutor;
import com.jalios.ejpt.sync.GlobalSyncStrategy;
import com.jalios.ejpt.sync.SyncStrategy;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncStrategyReport;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.jcmstools.transversal.EJPTUtil;

/**
 * Util
 * @author Xuan Tuong LE 
 *
 */
public class SyncHandlerUtil {
  public static SyncStrategyReport previewSync(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategy sync = new GlobalSyncStrategy(); 
    return sync.run(configuration);
  }

  public static SyncStrategyReport sync(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategyReport report = new SyncStrategyReport();
    SyncStrategy sync = new GlobalSyncStrategy();
    report = sync.run(configuration);
    report.run(new CopyExecutor());
    return report;
  }

  public static void refreshProject(IProject project) {
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
  

  public static void refreshProject(IProject project, IProgressMonitor monitor) {
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  public static MessageConsoleStream initConsole(String consoleName) {
    MessageConsole console = EJPTUtil.findConsoleByName(consoleName);
    console.activate();
    return console.newMessageStream();
  }

  public static void printReportToConsole(SyncStrategyReport report, MessageConsoleStream consoleStream,
      boolean preview, String webappDirectory, String pluginDirectory) {

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
    consoleStream.println("Webapp directory : " + webappDirectory);
    consoleStream.println("Plugin directory : " + pluginDirectory);
    consoleStream.println("W->P : " + syncFilesToPlugin.size() + " files ");
    consoleStream.println("P->W : " + syncFilesToWebapp.size() + " files ");
    consoleStream.println("?->? : " + syncFilesUnknown.size() + " files ");
    consoleStream.println("************");
    if (syncFilesUnknown.size() != 0) {
      consoleStream.println("Note on status :");
      consoleStream.println("?->? : Nothing is done on these files");      
      consoleStream
          .println("(FileNotFoundOnDisk) Recheck your plugin.xml because these files aren't on disk anymore");
      consoleStream.println("(FileShouldBeDeclared) Theses files won't be embedded in plugin until you declare them in plugin.xml. You could ignore them in sync.conf");
    }
    consoleStream.println("-----------------------------------------------------------");

    for (FileSyncStatus syncStatus : syncFilesToPlugin) {
      consoleStream.println("(" + syncStatus.getStatusName() + ") " + "W->P : " + syncStatus.getDestination().getPath());
    }
    for (FileSyncStatus sf : syncFilesToWebapp) {
      consoleStream.println("(" + sf.getStatusName() + ") " + "P->W : " + sf.getDestination().getPath());
    }
    for (FileSyncStatus sf : syncFilesUnknown) {
      consoleStream.println("(" + sf.getStatusName() + ") " + "?->? : " + sf.getDestination().getPath());
    }
  }

}
