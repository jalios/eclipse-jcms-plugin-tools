package com.jalios.jcmstools.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class SyncHandlerUtil {

  public static void refreshProject(IProject project){
    try {
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
}
