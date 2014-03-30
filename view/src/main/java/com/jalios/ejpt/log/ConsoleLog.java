package com.jalios.ejpt.log;

import java.util.Calendar;
import java.util.List;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.context.SyncContext;
import com.jalios.ejpt.sync.filesyncstatus.FileSyncStatus;
import com.jalios.jcmstools.transversal.EJPTUtil;

public class ConsoleLog {
  private static final String CONSOLE_NAME = "Jalios Plugin Tools - Sync Status";
  private static final ConsoleLog SINGLETON = new ConsoleLog();
  private MessageConsoleStream stream;

  private ConsoleLog() {
  }
  
  private void init(){
    MessageConsole console = EJPTUtil.findConsoleByName(CONSOLE_NAME);
    console.activate();
    stream = console.newMessageStream();
  }

  public static ConsoleLog getInstance() {
    return SINGLETON;
  }

  public void info(String msg) {
    init();
    stream.println(msg);
  }

  public void info(SyncReportManager report, SyncContext context) {
    init();
    List<FileSyncStatus> syncFilesToPlugin = report.getSyncFilesToPlugin();
    List<FileSyncStatus> syncFilesToWebapp = report.getSyncFilesToWebapp();
    List<FileSyncStatus> syncFilesUnknown = report.getSyncFilesUnknown();

    if (context.isPreview()) {
      stream.println("-----------------------------------------------------------");
      stream.println("This is only a PREVIEW status. NOTHING HAS BEEN DONE");
    }

    stream.println("************");
    stream.println("Summary : ");
    stream.println("Date : " + Calendar.getInstance().getTime());
    stream.println("Webapp directory : " + context.getConfiguration().getWebappProjectDirectory());
    stream.println("Plugin directory : " + context.getConfiguration().getPluginProjectDirectory());
    stream.println("W->P : " + syncFilesToPlugin.size() + " files ");
    stream.println("P->W : " + syncFilesToWebapp.size() + " files ");
    stream.println("?->? : " + syncFilesUnknown.size() + " files ");
    stream.println("************");
    if (syncFilesUnknown.size() != 0) {
      stream.println("Note on status :");
      stream.println("?->? : Nothing is done on these files");
      stream.println("(FileNotFoundOnDisk) Recheck your plugin.xml because these files aren't on disk anymore");
      stream
          .println("(FileShouldBeDeclared) Theses files won't be embedded in plugin until you declare them in plugin.xml. You could ignore them in sync.conf");
    }
    stream.println("-----------------------------------------------------------");

    for (FileSyncStatus syncStatus : syncFilesToPlugin) {      
      stream.println("(" + syncStatus.getStatusName() + ") " + "W->P : " + syncStatus.getDestination().getPath());
    }
    for (FileSyncStatus sf : syncFilesToWebapp) {
      stream.println("(" + sf.getStatusName() + ") " + "P->W : " + sf.getDestination().getPath());
    }
    for (FileSyncStatus sf : syncFilesUnknown) {
      stream.println("(" + sf.getStatusName() + ") " + "?->? : " + sf.getDestination().getPath());
    }
  }

}
