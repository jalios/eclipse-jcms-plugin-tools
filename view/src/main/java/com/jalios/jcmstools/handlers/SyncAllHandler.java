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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.jalios.ejpt.jobs.SyncJob;
import com.jalios.ejpt.log.ConsoleLog;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.context.InitSyncContextException;
import com.jalios.ejpt.sync.context.SyncContext;
import com.jalios.jcmstools.transversal.EJPTUtil;

/**
 * Handler for action preview and real sync for ALL projects
 * 
 * @author Xuan Tuong LE (lxtuong@gmail.com - @lxtuong)
 */
public class SyncAllHandler extends AbstractHandler {
  public static final String ID = "SyncAllHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.previewall";
  private boolean preview = true;
  private static final ConsoleLog logger = ConsoleLog.getInstance();

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
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    preview = isPreview(event.getCommand());

    if (!preview) {
      boolean isSyncAllConfirmed = MessageDialog.openConfirm(window.getShell(), "Sync All",
          "Are you sure to sync all JCMS Plugin Project ?");
      if (!isSyncAllConfirmed) {
        return null;
      }
    }

    for (IProject pluginProject : EJPTUtil.getPluginProjects()) {
      SyncContext context;
      try {
        context = initSyncContext(pluginProject);
        context.setPreview(preview);
      } catch (InitSyncContextException e) {
        logger.info(e.getMessage());
        break;
      }

      Job sync = new SyncJob("Check sync for : " + pluginProject, context);
      sync.schedule();

    }
    return null;
  }

  private SyncContext initSyncContext(IProject pluginProject) throws InitSyncContextException {
    if (pluginProject == null) {
      throw new InitSyncContextException(
          "Cannot detect plugin project. "
              + "Make sure that you launch on the plugin project "
              + "or check your plugin project nature by using <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project. "
              + "Operation aborted");
    }
    SyncContext context = new SyncContext();
    // determine projects to sync
    IProject webappProject = EJPTUtil.getJcmsWebappProject(pluginProject);

    if (webappProject == null) {
      throw new InitSyncContextException("Please link the JCMSPlugin project to a webapp project. Right-click on "
          + pluginProject + " > Properties > Project References > Choose the webapp project");
    }

    // init configuration and sync context
    File configurationFile = EJPTUtil.getSyncConfigurationFilePath(webappProject);
    File pluginDirectory = pluginProject.getLocation().toFile();
    File webappDirectory = new File(EJPTUtil.getWebappDirectoryPath(webappProject));
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginDirectory, webappDirectory)
        .configuration(configurationFile).build();

    context.setPluginProject(pluginProject);
    context.setWebappProject(webappProject);
    context.setConfiguration(configuration);

    return context;
  }
}
