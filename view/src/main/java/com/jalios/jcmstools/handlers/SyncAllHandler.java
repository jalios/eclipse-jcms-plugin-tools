package com.jalios.jcmstools.handlers;

import java.io.File;
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

import com.jalios.jcmsplugin.sync.BasicSyncCompute;
import com.jalios.jcmsplugin.sync.Direction;
import com.jalios.jcmsplugin.sync.ISync;
import com.jalios.jcmsplugin.sync.ImpliciteSyncCompute;
import com.jalios.jcmsplugin.sync.SyncComputeResult;
import com.jalios.jcmsplugin.sync.SyncConfiguration;
import com.jalios.jcmsplugin.sync.SyncException;
import com.jalios.jcmsplugin.sync.SyncFile;
import com.jalios.jcmsplugin.sync.SyncUtil;
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
      String cfPath = JPTUtil.getSyncConfPath(jcmsWebappProject);
      File ppPath = jcmsPluginProject.getLocation().toFile();
      File wpPath = new File(JPTUtil.getWebappRootdir(jcmsWebappProject));
      SyncConfiguration conf = new SyncConfiguration.Builder(ppPath, wpPath).conf(cfPath).build();

      if (preview) {
        conf.enablePreview();
      }
      
      SyncComputeResult result = new SyncComputeResult();
      ISync basicComputeSync = new BasicSyncCompute();
      ISync impliciteComputeSync = new ImpliciteSyncCompute();
      try {
        result = basicComputeSync.computeSync(conf, result);
        result = impliciteComputeSync.computeSync(conf, result);
        SyncUtil.runSync(result);
        List<SyncFile> sfToPluginList = result.getSyncFiles(Direction.TO_PLUGIN);
        stream.println("W->P : " + sfToPluginList.size() + " files");
        for (SyncFile sf : sfToPluginList){
          stream.println("W->P : " + sf.getTgt());
        }
        List<SyncFile> sfToWebappList = result.getSyncFiles(Direction.TO_WEBAPP);
        stream.println("P->W : " + sfToWebappList.size() + " files");
        for (SyncFile sf : sfToWebappList){
          stream.println("P->W : " + sf.getTgt());
        }
      } catch (SyncException e) {
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
