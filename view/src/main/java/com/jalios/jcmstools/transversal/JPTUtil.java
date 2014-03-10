package com.jalios.jcmstools.transversal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Action utilities
 * 
 * @author Xuan-Tuong LE
 */
public class JPTUtil implements JPTConstants {

  public static MessageConsole findConsole(String name) {
    // init
    MessageConsole myConsole = null;
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager conMan = plugin.getConsoleManager();

    // find if a console exists
    IConsole[] existing = conMan.getConsoles();
    for (int i = 0; i < existing.length; i++) {
      if (name.equals(existing[i].getName())) {
        myConsole = (MessageConsole) existing[i];
        myConsole.clearConsole();
        return myConsole;
      }
    }

    // no console found, create a new one
    myConsole = new MessageConsole(name, null);
    conMan.addConsoles(new IConsole[] { myConsole });
    return myConsole;
  }

  public static String getSyncConfPath(IResource pWebappRsrc) {
    for (File file : pWebappRsrc.getLocation().toFile().listFiles()) {
      if (file.getName().equals(JPTConstants.SYNC_CONF_FILENAME)) {
        return file.getAbsolutePath();
      }
    }
    return "";
  }

  public static String getWebappRootdir(IResource pWebappRsrc) {
    // default root dir
    String result = pWebappRsrc.getLocation().toFile().getPath();
    for (File file : pWebappRsrc.getLocation().toFile().listFiles()) {
      if (file.getName().equals(JPTConstants.SYNC_CONF_FILENAME)) {
        Properties prop = new Properties();
        FileInputStream in;
        try {
          in = new FileInputStream(pWebappRsrc.getLocation().toFile().getPath() + "/"
              + JPTConstants.SYNC_CONF_FILENAME);
          prop.load(in);
          in.close();
          String rootdir = prop.getProperty(JPTConstants.WEBAPP_ROOT_KEY_SC);
          if (rootdir != null && !rootdir.equals("")) {
            result = pWebappRsrc.getLocation().toFile().getPath() + "/" + rootdir;
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }
    }
    return result;
  }

  public static boolean isJCMSPluginProject(IProject project) {
    try {
      return project != null && project.hasNature(JCMS_PLUGIN_NATURE);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean isJCMSWebappProject(IProject project) {
    try {
      return project != null && project.hasNature(JCMS_PROJECT_NATURE);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static IProject getSyncProject(ExecutionEvent event) {
    IProject syncProject = null;

    // case 1 : user is working on an active editor on a file and sync
    // action is
    // called
    IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
    if (activeEditor != null) {
      IFileEditorInput input = (IFileEditorInput)activeEditor.getEditorInput();
      syncProject = input.getFile().getProject();

      if (isJCMSPluginProject(syncProject)) {
        return syncProject;
      }
    }

    // case 2 : user is selecting a tree selection in the packaging/project
    // explorer
    try {
      ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        Object element = structuredSelection.iterator().next();
        if (element instanceof IProject) {
          if (isJCMSPluginProject((IProject) element)) {
            return (IProject) element;
          }
        }
        if (element instanceof IResource) {
          IResource resource = (IResource) element;
          if (isJCMSPluginProject(resource.getProject())) {
            return resource.getProject();
          }
        }
      }
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    return syncProject;
  }

  public static List<IResource> getSyncResources(IProject project) {
    List<IResource> mSyncResources = new ArrayList<IResource>();

    // 1st resource : project plugin
    mSyncResources.add(project);

    // 2nd : webapp referenced project
    try {
      if (project.getReferencedProjects().length == 1) {
        IProject webappProject = project.getReferencedProjects()[0];
        mSyncResources.add(webappProject);
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return mSyncResources;
  }

  public static IProject getJcmsWebappProject(IProject jcmsPluginProject) {
    try {
      if (jcmsPluginProject.getReferencedProjects().length == 1) {
        return jcmsPluginProject.getReferencedProjects()[0];
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Set<IProject> getAllJCMSProject() {
    Set<IProject> results = new HashSet<IProject>();
    IProject[] availableProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

    if (availableProjects != null) {
      for (IProject project : availableProjects) {
        if (isJCMSPluginProject(project)) {
          results.add(project);
        }
      }
    }
    return results;
  }

}
