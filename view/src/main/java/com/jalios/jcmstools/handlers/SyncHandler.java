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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsoleStream;

import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.SyncStrategyReport;
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
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private IProject pluginProject;
  private IProject webappProject;
  private MessageConsoleStream consoleStream;
  private boolean preview = false;
  private SyncStrategyConfiguration configuration;

  public Object execute(ExecutionEvent event) {
    consoleStream = SyncHandlerUtil.initConsole(CONSOLE_NAME);
    detectCommandNature(event.getCommand());

    try {
      initSyncContextFromEvent(event);
    } catch (InitSyncContextException exception) {
      consoleStream.println(exception.getMessage());
      return null; // TODO : why this weird return
    }

  

    String jobname = "Check sync";
    Job sync = new SyncJob(jobname);
    sync.schedule();

    return null; // TODO : why this weird return
  }

  public class SyncJob extends Job {
    private SyncJob(String name) {
      super(name);
    }

    protected IStatus run(IProgressMonitor monitor) {
      monitor.beginTask("Sync is running...", 10);
      long start = System.currentTimeMillis();
      SyncStrategyReport report = new SyncStrategyReport();
      try {
        if (preview) {
          report = SyncHandlerUtil.previewSync(configuration);          
        } else {
          report = SyncHandlerUtil.sync(configuration);
          SyncHandlerUtil.refreshProject(pluginProject, monitor);
          SyncHandlerUtil.refreshProject(webappProject, monitor);
        }
      } catch (SyncStrategyException e) {
        consoleStream.println(e.getMessage());
      }
      monitor.worked(5);
      SyncHandlerUtil.printReportToConsole(report, consoleStream, preview,
          EJPTUtil.getWebappDirectoryPath(webappProject), pluginProject.getLocation().toFile().getAbsolutePath());
      consoleStream.println("-----------------------------------------------------------");
      consoleStream.println("Sync took : " + (System.currentTimeMillis() - start) + " ms");
      return Status.OK_STATUS;
    }
  }

  private void detectCommandNature(Command cmd) {
    preview = ID_PREVIEW_CMD.equals(cmd.getId());
  }

  private void initSyncContextFromEvent(ExecutionEvent event) throws InitSyncContextException {
    pluginProject = EJPTUtil.getSyncProject(event);

    if (pluginProject == null) {
      throw new InitSyncContextException(
          "Cannot detect plugin project. "
              + "Make sure that you launch on the plugin project "
              + "or check your plugin project nature by using <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project. "
              + "Operation aborted");
    }

    webappProject = EJPTUtil.getJcmsWebappProject(pluginProject);

    if (webappProject == null) {
      throw new InitSyncContextException("Please link the JCMSPlugin project to a webapp project. Right-click on "
          + pluginProject + " > Properties > Project References > Choose the webapp project" + "Operation aborted");
    }

    // init configuration and sync context
    File config = EJPTUtil.getSyncConfigurationFilePath(webappProject);
    File ppPath = pluginProject.getLocation().toFile();

    try {
      IOUtil.findPluginXMLFile(ppPath);
    } catch (FileNotFoundException e) {
      throw new InitSyncContextException(e.getMessage());
    }

    File wpPath = new File(EJPTUtil.getWebappDirectoryPath(webappProject));
    configuration = new SyncStrategyConfiguration.Builder(ppPath, wpPath).configuration(config).build();
  }

}
