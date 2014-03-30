package com.jalios.ejpt.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.jalios.ejpt.log.ConsoleLog;
import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.context.SyncContext;

public class SyncJob extends Job {
  private static final int WORK_UNIT = 10;
  private boolean preview;
  private SyncContext context;
  private static final ConsoleLog logger = ConsoleLog.getInstance();

  public SyncJob(String name, SyncContext context) {
    super(name);
    this.context = context;
    this.preview = context.isPreview();
  }

  protected IStatus run(IProgressMonitor monitor) {
    monitor.beginTask("Sync is running...", WORK_UNIT);
    long start = System.currentTimeMillis();
    SyncReportManager report = new SyncReportManager();
    try {
      if (preview) {
        report = Util.previewSync(context.getConfiguration());
      } else {
        report = Util.sync(context.getConfiguration());
        Util.refreshProject(context.getPluginProject(), monitor);
        Util.refreshProject(context.getWebappProject(), monitor);
      }
    } catch (SyncStrategyException e) {
      logger.info(e.getMessage());
      return Status.CANCEL_STATUS;
    }
    monitor.worked(WORK_UNIT / 2);
    
    logger.info(report, context);
    logger.info("-----------------------------------------------------------");
    logger.info("Sync took : " + (System.currentTimeMillis() - start) + " ms");
    monitor.done();
    return Status.OK_STATUS;
  }
}
