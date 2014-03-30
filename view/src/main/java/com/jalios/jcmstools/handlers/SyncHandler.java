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
import java.io.FileNotFoundException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;

import com.jalios.ejpt.jobs.SyncJob;
import com.jalios.ejpt.log.ConsoleLog;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.context.InitSyncContextException;
import com.jalios.ejpt.sync.context.SyncContext;
import com.jalios.ejpt.sync.utils.IOUtil;
import com.jalios.jcmstools.transversal.EJPTUtil;

/**
 * Handler for action preview and real sync
 * 
 * @author Xuan Tuong LE (lxtuong@gmail.com - @lxtuong)
 */
public class SyncHandler extends AbstractHandler {
  public static final String ID = "SyncHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.preview";
  private static final ConsoleLog logger = ConsoleLog.getInstance();
  private SyncContext context;

  public Object execute(ExecutionEvent event) {
    try {
      context = initSyncContextFromEvent(event);      
    } catch (InitSyncContextException exception) {
      logger.info(exception.getMessage());
      return null;
    }

    boolean preview = isPreviewCommand(event.getCommand());
    context.setPreview(preview);

    String jobname = preview ? "Check preview sync" : "Check sync";
    Job sync = new SyncJob(jobname, context);
    sync.schedule();

    return null;
  }

  private boolean isPreviewCommand(Command cmd) {
    return ID_PREVIEW_CMD.equals(cmd.getId());
  }

  private SyncContext initSyncContextFromEvent(ExecutionEvent event) throws InitSyncContextException {
    SyncContext context = new SyncContext();
    IProject pluginProject = EJPTUtil.getSelectedProject(event);

    if (pluginProject == null) {
      throw new InitSyncContextException(
          "Cannot detect plugin project. "
              + "Make sure that you launch on the plugin project "
              + "or check your plugin project nature by using <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project. "
              + "Operation aborted");
    }

    IProject webappProject = EJPTUtil.getJcmsWebappProject(pluginProject);

    if (webappProject == null) {
      throw new InitSyncContextException("Please link the JCMSPlugin project to a webapp project. Right-click on "
          + pluginProject + " > Properties > Project References > Choose the webapp project" + "Operation aborted");
    }

    // init configuration and sync context
    File configurationFile = EJPTUtil.getSyncConfigurationFilePath(webappProject);
    File pluginProjectPath = pluginProject.getLocation().toFile();

    try {
      IOUtil.findPluginXMLFile(pluginProjectPath);
    } catch (FileNotFoundException e) {
      throw new InitSyncContextException("Cannot find the configuration file : " + e.getMessage());
    }

    File webappProjectPath = new File(EJPTUtil.getWebappDirectoryPath(webappProject));
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(pluginProjectPath,
        webappProjectPath).configuration(configurationFile).build();

    context.setPluginProject(pluginProject);
    context.setWebappProject(webappProject);
    context.setConfiguration(configuration);

    return context;
  }

}
