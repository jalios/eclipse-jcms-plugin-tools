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
package com.jalios.ejpt.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.jalios.ejpt.sync.SyncReportManager;
import com.jalios.ejpt.sync.SyncStrategyConfiguration;
import com.jalios.ejpt.sync.SyncStrategyException;
import com.jalios.ejpt.sync.executor.CopyExecutor;
import com.jalios.ejpt.sync.strategy.GlobalSyncStrategy;
import com.jalios.ejpt.sync.strategy.SyncStrategy;

/**
 * Util
 * 
 * @author Xuan Tuong LE
 * 
 */
public class Util {
  public static SyncReportManager previewSync(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncStrategy sync = new GlobalSyncStrategy();
    return sync.run(configuration);
  }

  public static SyncReportManager sync(SyncStrategyConfiguration configuration) throws SyncStrategyException {
    SyncReportManager report = new SyncReportManager();
    SyncStrategy sync = new GlobalSyncStrategy();
    report = sync.run(configuration);
    report.run(new CopyExecutor());
    return report;
  }

  public static void refreshProject(IProject project) {
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  public static void refreshProject(IProject project, IProgressMonitor monitor) {
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

}
