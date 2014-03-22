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
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncStrategyReport;
import com.jalios.jcmstools.transversal.EJPTUtil;

/**
 * Handler for action preview and real sync for ALL projects
 * 
 * @author Xuan Tuong LE (lxtuong@gmail.com - @lxtuong)
 */
public class SyncAllHandler extends AbstractHandler {
  public static final String ID = "SyncAllHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.previewall";
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private boolean preview = true;
  private MessageConsoleStream consoleStream;

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
    consoleStream = SyncHandlerUtil.initConsole(CONSOLE_NAME);

    for (IProject jcmsPluginProject : EJPTUtil.getJCMSPluginProjects()) {
      consoleStream.println("\n---------------------------------------------------");
      consoleStream.println("Begin Sync for project '" + jcmsPluginProject + "'");
      consoleStream.println("---------------------------------------------------");

      // determine projects to sync
      IProject jcmsWebappProject = EJPTUtil.getJcmsWebappProject(jcmsPluginProject);

      if (jcmsWebappProject == null) {
        consoleStream.println("Please link the JCMSPlugin project to a webapp project. Right-click on "
            + jcmsPluginProject + " > Properties > Project References > Choose the webapp project");
        consoleStream.println("Operation aborted.");
        break; // next project
      }

      // init configuration and sync context
      File config = EJPTUtil.getSyncConfigurationFilePath(jcmsWebappProject);
      File pluginDirectory = jcmsPluginProject.getLocation().toFile();
      File webappDirectory = new File(EJPTUtil.getWebappDirectoryPath(jcmsWebappProject));
      SyncStrategyConfiguration conf = new SyncStrategyConfiguration.Builder(pluginDirectory, webappDirectory)
          .configuration(config).build();

      run(conf, preview, pluginDirectory, webappDirectory);
      SyncHandlerUtil.refreshProject(jcmsPluginProject);
      SyncHandlerUtil.refreshProject(jcmsWebappProject);

      consoleStream.println("\n---------------------------------------------------");
      consoleStream.println("END Sync for project '" + jcmsPluginProject + "'");
      consoleStream.println("---------------------------------------------------\n");
    }
    return null;
  }

  private void run(SyncStrategyConfiguration configuration, boolean isPreview, File pluginDirectory,
      File webappDirectory) {
    long start = System.currentTimeMillis();
    SyncStrategyReport report = new SyncStrategyReport();
    try {
      if (preview) {
        report = SyncHandlerUtil.previewSync(configuration);
      } else {
        report = SyncHandlerUtil.sync(configuration);
      }
    } catch (SyncStrategyException e) {
      consoleStream.println(e.getMessage());
    }
    SyncHandlerUtil.printReportToConsole(report, consoleStream, preview, webappDirectory.getAbsolutePath(),
        pluginDirectory.getAbsolutePath());
    consoleStream.println("-----------------------------------------------------------");
    consoleStream.println("Sync took : " + (System.currentTimeMillis() - start) + " ms");

  }
}
