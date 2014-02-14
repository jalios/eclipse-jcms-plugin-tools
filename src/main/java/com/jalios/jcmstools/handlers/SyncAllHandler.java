package com.jalios.jcmstools.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jalios.jcmstools.sync.SyncContext;
import com.jalios.jcmstools.transversal.JPTUtil;
import com.jalios.jcmstools.util.SyncManager;

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
  public static final SyncManager syncMgr = SyncManager.getInstance();

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
    boolean preview = false;
    if (isPreview(event.getCommand())) {
      preview = true;
    } else {
      boolean go = MessageDialog.openConfirm(window.getShell(), "Sync All",
          "Are you sure to sync all JCMS Plugin Project ?");
      if (!go) {
        return null;
      }
    }
    MessageConsole console = JPTUtil.findConsole(CONSOLE_NAME);
    console.activate();
    MessageConsoleStream stream = console.newMessageStream();
    syncMgr.setStream(stream);

    for (IProject jcmsPluginProject : JPTUtil.getAllJCMSProject()) {
      stream.println("\n---------------------------------------------------");
      stream.println("Begin Sync for project '" + jcmsPluginProject + "'");
      stream.println("---------------------------------------------------");

      // determine projects to sync
      IProject jcmsWebappProject = JPTUtil.getJcmsWebappProject(jcmsPluginProject);

      if (jcmsWebappProject == null) {
        stream.println("Please link the JCMSPlugin project to a webapp project. Right-click on "
            + jcmsPluginProject + " > Properties > Project References > Choose the webapp project");
        stream.println("Operation aborted.");
        break; // next project
      }

      // init configuration and sync context
      String syncConfPath = JPTUtil.getSyncConfPath(jcmsWebappProject);
      SyncContext syncContext = new SyncContext(syncConfPath);

      syncContext.setWebappProjectPath(JPTUtil.getWebappRootdir(jcmsWebappProject));
      File pluginFile = jcmsPluginProject.getLocation().toFile();
      syncContext.setPluginProjectPath(pluginFile.getPath());

      // take first level plugin directory (plugins, WEB-INF,...)
      List<String> pluginSubFolders = new ArrayList<String>();
      for (File file : pluginFile.listFiles()) {
        if (file.isDirectory()) {
          pluginSubFolders.add(file.getName());
        }
      }
      syncContext.setPluginProjectSubFoldersPath(pluginSubFolders);

      if (!preview) {
        syncContext.disablePreview();
      }

      try {
        syncMgr.execute(syncContext);
      } catch (Exception e) {
        e.printStackTrace();
      }

      // refresh
      try {
        jcmsPluginProject.refreshLocal(IResource.DEPTH_INFINITE, null);
        jcmsWebappProject.refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }

      stream.println("\n---------------------------------------------------");
      stream.println("END Sync for project '" + jcmsPluginProject + "'");
      stream.println("---------------------------------------------------\n");
    }
    return null;
  }
}
