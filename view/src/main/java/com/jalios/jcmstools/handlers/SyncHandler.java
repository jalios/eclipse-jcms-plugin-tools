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

import com.jalios.jcmsplugin.sync.CopyExecutor;
import com.jalios.jcmsplugin.sync.FileSyncStrategy;
import com.jalios.jcmsplugin.sync.NewWebappFileStrategy;
import com.jalios.jcmsplugin.sync.SyncFile;
import com.jalios.jcmsplugin.sync.SyncStrategy;
import com.jalios.jcmsplugin.sync.SyncStrategyConfiguration;
import com.jalios.jcmsplugin.sync.SyncStrategyException;
import com.jalios.jcmsplugin.sync.SyncStrategyReport;
import com.jalios.jcmstools.transversal.JPTUtil;

/**
 * Sync handler for sync and sync preview command
 * 
 * @author xuan-tuong.le (lxtuong@gmail.com - @lxtuong)
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SyncHandler extends AbstractHandler {
  public static final String ID = "SyncHandler";
  public static final String ID_PREVIEW_CMD = "com.jalios.commands.preview";
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private IProject pluginProject;
  private IProject webappProject;
  private MessageConsoleStream message;
  private boolean preview = false;

  /**
   * The constructor.
   */
  public SyncHandler() {
    MessageConsole console = JPTUtil.findConsole(CONSOLE_NAME);
    console.activate();
    message = console.newMessageStream();

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
      
    initProject(event);
    
    if (isInitOK()){
      return null;
    }   

    // init configuration and sync context
    String cfPath = JPTUtil.getSyncConfPath(webappProject);
    File ppPath = pluginProject.getLocation().toFile();
    File wpPath = new File(JPTUtil.getWebappRootdir(webappProject));
    SyncStrategyConfiguration configuration = new SyncStrategyConfiguration.Builder(ppPath, wpPath).conf(cfPath)
        .build();

    run(configuration);
    SyncHandlerUtil.refreshProject(pluginProject);
    SyncHandlerUtil.refreshProject(webappProject);

    return null;
  }
  
  private void initProject(ExecutionEvent event){
    pluginProject = JPTUtil.getSyncProject(event);
    webappProject = JPTUtil.getJcmsWebappProject(pluginProject);
  }

  private boolean isInitOK(){
    if (pluginProject == null) {
      message
          .println("Please define your project as a JCMSPlugin project by using <nature>com.jalios.jpt.natures.jcmspluginnature</nature> in .project");
      message.println("Operation aborted.");
      return false;
    }

    if (webappProject == null) {
      message.println("Please link the JCMSPlugin project to a webapp project. Right-click on " + pluginProject
          + " > Properties > Project References > Choose the webapp project");
      message.println("Operation aborted.");
      return false;
    }
    
    return true;
  }
  
  private void run(SyncStrategyConfiguration configuration){
    SyncStrategyReport report1 = new SyncStrategyReport();
    SyncStrategyReport report2 = new SyncStrategyReport();

    SyncStrategy fileSync = new FileSyncStrategy();
    SyncStrategy newWebappFileStrategy = new NewWebappFileStrategy();
    try {
      report1 = fileSync.run(configuration);
      if (!preview){
        report1.run(new CopyExecutor());
      }
      printReport(report1);

      report2 = newWebappFileStrategy.run(configuration);
      if (!preview){
        report2.run(new CopyExecutor());
      }     
      printReport(report2);
    } catch (SyncStrategyException e) {
      e.printStackTrace();
    }
  }

  private void printReport(SyncStrategyReport report) {
    if (preview){
      message.println("THIS IS ONLY A PREVIEW. NOTHING HAS BEEN EXECUTED");
    }
    List<SyncFile> syncFilesToPlugin = report.getSyncFilesToPlugin();
    message.println("W->P : " + syncFilesToPlugin.size() + " files");
    for (SyncFile sf : syncFilesToPlugin) {
      message.println("W->P : " + sf.getTgt());
    }
    List<SyncFile> syncFilesToWebapp = report.getSyncFilesToWebapp();
    message.println("P->W : " + syncFilesToWebapp.size() + " files");
    for (SyncFile sf : syncFilesToWebapp) {
      message.println("P->W : " + sf.getTgt());
    }
  }
  
}
