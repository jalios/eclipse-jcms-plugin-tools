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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jalios.jcmsplugin.sync.CopyExecutor;
import com.jalios.jcmsplugin.sync.FileSyncStrategy;
import com.jalios.jcmsplugin.sync.NewWebappFileStrategy;
import com.jalios.jcmsplugin.sync.SyncStrategy;
import com.jalios.jcmsplugin.sync.SyncStrategyConfiguration;
import com.jalios.jcmsplugin.sync.SyncStrategyException;
import com.jalios.jcmsplugin.sync.SyncStrategyReport;
import com.jalios.jcmstools.transversal.JPTUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SyncAllHandler extends AbstractHandler {
  public static final String ID = "SyncAllHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.previewall";
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private boolean preview = true;

  /**
   * The constructor.
   */
  public SyncAllHandler() {
  }

  public boolean isPreview(Command cmd) {
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
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    preview = isPreview(event.getCommand());

    if (!preview) {
      boolean isSyncAllConfirmed = MessageDialog.openConfirm(window.getShell(), "Sync All",
          "Are you sure to sync all JCMS Plugin Project ?");
      if (!isSyncAllConfirmed) {
        return null;
      }
    }
    MessageConsole console = JPTUtil.findConsole(CONSOLE_NAME);
    console.activate();
    MessageConsoleStream stream = console.newMessageStream();

    for (IProject jcmsPluginProject : JPTUtil.getAllJCMSProject()) {
      stream.println("\n---------------------------------------------------");
      stream.println("Begin Sync for project '" + jcmsPluginProject + "'");
      stream.println("---------------------------------------------------");

      // determine projects to sync
      IProject jcmsWebappProject = JPTUtil.getJcmsWebappProject(jcmsPluginProject);

      if (jcmsWebappProject == null) {
        stream.println("Please link the JCMSPlugin project to a webapp project. Right-click on " + jcmsPluginProject
            + " > Properties > Project References > Choose the webapp project");
        stream.println("Operation aborted.");
        break; // next project
      }

      // init configuration and sync context
      String cfPath = JPTUtil.getSyncConfPath(jcmsWebappProject);
      File ppPath = jcmsPluginProject.getLocation().toFile();
      File wpPath = new File(JPTUtil.getWebappRootdir(jcmsWebappProject));
      SyncStrategyConfiguration conf = new SyncStrategyConfiguration.Builder(ppPath, wpPath).conf(cfPath).build();

      run(conf, preview);
      SyncHandlerUtil.refreshProject(jcmsPluginProject);
      SyncHandlerUtil.refreshProject(jcmsWebappProject);

      stream.println("\n---------------------------------------------------");
      stream.println("END Sync for project '" + jcmsPluginProject + "'");
      stream.println("---------------------------------------------------\n");
    }
    return null;
  }

  private void run(SyncStrategyConfiguration configuration, boolean isPreview) {
    SyncStrategyReport report1 = new SyncStrategyReport();
    SyncStrategyReport report2 = new SyncStrategyReport();

    SyncStrategy fileSync = new FileSyncStrategy();
    SyncStrategy newWebappFileStrategy = new NewWebappFileStrategy();
    try {
      report1 = fileSync.run(configuration);
      if (!isPreview) {
        report1.run(new CopyExecutor());
      }
      report2 = newWebappFileStrategy.run(configuration);
      if (!isPreview) {
        report2.run(new CopyExecutor());
      }

    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }
}
