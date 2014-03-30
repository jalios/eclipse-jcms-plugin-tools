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
package com.jalios.jcmstools.transversal;

import static com.jalios.jcmstools.transversal.EJPTConstants.JCMS_PLUGIN_NATURE;
import static com.jalios.jcmstools.transversal.EJPTConstants.JCMS_PROJECT_NATURE;

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
import org.eclipse.core.runtime.IPath;
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
 * Utilities
 * 
 * @author Xuan-Tuong LE
 */
public class EJPTUtil {

  /**
   * Find a console by name Reuse existing console if found and clear it
   * Otherwise, create a new one
   * 
   * @param consoleName
   *          console to find
   * @return MessageConsole console
   */
  public static MessageConsole findConsoleByName(String consoleName) {
    ConsolePlugin plugin = ConsolePlugin.getDefault();
    IConsoleManager consoleManager = plugin.getConsoleManager();

    IConsole[] actualConsoles = consoleManager.getConsoles();
    for (IConsole itConsole : actualConsoles) {
      if (consoleName.equals(itConsole.getName())) {
        MessageConsole consoleResult = (MessageConsole) itConsole;
        consoleResult.clearConsole();
        return consoleResult;
      }
    }

    MessageConsole consoleResult = new MessageConsole(consoleName, null);
    consoleManager.addConsoles(new IConsole[] { consoleResult });
    return consoleResult;
  }

  public static File getSyncConfigurationFilePath(IResource webappResource) {
    IPath webappLocation = webappResource.getLocation();
    if (webappLocation == null) {
      return null;
    }

    for (File file : webappLocation.toFile().listFiles()) {
      if (EJPTConstants.SYNC_CONF_FILENAME.equals(file.getName())) {
        return file;
      }
    }
    return null;
  }

  private static boolean isPropertyExist(IPath webappLocation, String propertyKeyName) {
    if (webappLocation == null || propertyKeyName == null) {
      return false;
    }

    Properties prop = new Properties();
    try {
      File syncFile = new File(webappLocation.toFile().getPath(), EJPTConstants.SYNC_CONF_FILENAME);

      if (!syncFile.exists()) {
        return false;
      }

      FileInputStream in = new FileInputStream(syncFile);
      prop.load(in);
      in.close();
      String propertyValue = prop.getProperty(propertyKeyName);
      return propertyValue != null && !propertyValue.equals("");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static String getWebappDirectoryPath(IResource webappResource) {
    if (webappResource.getLocation() == null) {
      return "";
    }

    IPath webappLocation = webappResource.getLocation();
    if (!isPropertyExist(webappLocation, EJPTConstants.WEBAPP_ROOT_DIRECTORY_KEY)) {
      return webappLocation.toFile().getPath();
    }

    for (File file : webappLocation.toFile().listFiles()) {
      if (file.getName().equals(EJPTConstants.SYNC_CONF_FILENAME)) {
        Properties prop = new Properties();
        FileInputStream in;
        try {
          in = new FileInputStream(webappResource.getLocation().toFile().getPath() + "/"
              + EJPTConstants.SYNC_CONF_FILENAME);
          prop.load(in);
          in.close();
          String rootdir = prop.getProperty(EJPTConstants.WEBAPP_ROOT_DIRECTORY_KEY);
          if (rootdir != null && !rootdir.equals("")) {
            return webappResource.getLocation().toFile().getPath() + "/" + rootdir;
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }
    }
    return "";
  }

  public static boolean isJCMSPluginProject(IProject project) {
    if (project == null) {
      return false;
    }

    try {
      return project.hasNature(JCMS_PLUGIN_NATURE);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean isJCMSWebappProject(IProject project) {
    if (project == null) {
      return false;
    }

    try {
      return project.hasNature(JCMS_PROJECT_NATURE);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static IProject getSelectedProject(ExecutionEvent event) {
    IProject syncProject = null;

    // case 1 : user is working on an active editor on a file and sync
    // action is
    // called
    IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
    if (activeEditor instanceof IFileEditorInput) {
      IFileEditorInput input = (IFileEditorInput) activeEditor.getEditorInput();
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

  public static Set<IProject> getPluginProjects() {
    IProject[] availableProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

    if (availableProjects == null) {
      return new HashSet<IProject>();
    }

    Set<IProject> jcmsPluginProjects = new HashSet<IProject>();
    for (IProject project : availableProjects) {
      if (isJCMSPluginProject(project)) {
        jcmsPluginProjects.add(project);
      }
    }

    return jcmsPluginProjects;
  }

}
