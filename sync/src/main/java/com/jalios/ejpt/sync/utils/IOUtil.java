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
package com.jalios.ejpt.sync.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;

import com.jalios.ejpt.parser.ParseConstants;

/**
 * Util for sync
 * 
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
public class IOUtil {
  private static final Logger logger = Logger.getLogger(IOUtil.class);

  /**
   * List the first level of a directory
   * 
   * @param dir
   * @return
   */
  public static List<File> listDirectoryFirstLevel(File dir) {
    if (dir == null || !dir.isDirectory()) {
      return new ArrayList<File>();
    }

    List<File> results = new ArrayList<File>();
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        results.add(file);
      }
    }

    return results;
  }

  /**
   * Copy f1 into f2 (mkdirs for f2)
   * 
   * @param f1
   *          the source file
   * @param f2
   *          the target file
   * @throws IOException
   */
  public static void copyFile(File f1, File f2) throws IOException {
    if (f1 == null || f2 == null) {
      throw new IllegalArgumentException("f1 and f2 arguments must not be null");
    }

    // Create target directories
    if (f2.getParentFile() != null) {
      f2.getParentFile().mkdirs();
    }

    // Copy
    InputStream input = null;
    OutputStream output = null;
    try {
      input = new FileInputStream(f1);
      output = new FileOutputStream(f2);
      IOUtils.copy(input, output);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ex) {
        }
      }
      if (output != null) {
        try {
          output.close();
        } catch (IOException ex) {
        }
      }
    }
    f2.setLastModified(f1.lastModified());

  }

  public static String getRelativePath(File dir, File file) {

    if (dir == null || file == null) {
      throw new IllegalArgumentException("dir and file arguments must not be null");
    }

    String dirname = dir.toURI().normalize().toString();
    String filename = file.toURI().normalize().toString();

    // file is not under dir
    if (filename.length() < dirname.length() || !filename.startsWith(dirname)) {
      return null;
    }

    // file == dir
    if (filename.length() == dirname.length()) {
      return "";
    }

    // file is under dir
    return filename.substring(dirname.length());
  }

  /**
   * Return a list of files from the given directory and matching the given
   * filter. If a filter is provided, the directories (and their content) that
   * do not match the filter are skipped.
   * 
   * @param dir
   *          the directory to explore
   * @param filter
   *          the FileFilter to use (may be null)
   * @return a List of java.io.File
   * @see #deepListFiles(File, FileFilter, boolean)
   */
  public static List<File> deepListFiles(File dir, FileFilter filter) {
    return deepListFiles(dir, filter, true);
  }

  /**
   * Return a list of files from the given directory and matching the given
   * filter.
   * 
   * @param dir
   *          the directory to explore
   * @param filter
   *          the FileFilter to use (may be null)
   * @param checkDir
   *          if true the directories that do not match the filter are skipped.
   * @return a List of java.io.File
   */
  public static List<File> deepListFiles(File dir, FileFilter filter, boolean checkDir) {
    ArrayList<File> list = new ArrayList<File>();
    deepListFilesVisitor(dir, filter, checkDir, list);
    return list;
  }

  private static void deepListFilesVisitor(File file, FileFilter filter, boolean checkDir, List<File> list) {
    if (file.isDirectory()) {
      if (checkDir && filter != null && !filter.accept(file)) {
        return;
      }
      File[] files = file.listFiles();
      for (int i = 0; i < files.length; i++) {
        deepListFilesVisitor(files[i], filter, checkDir, list);
      }
    } else {
      if (filter == null || filter.accept(file)) {
        list.add(file);
      }
    }
  }

  /**
   * Creates a new empty directory
   * 
   * @return An abstract pathname denoting a newly-created empty directory
   */
  public static File createTempDir() {
    return createTempDir(null, null, null, true);
  }

  /**
   * Creates a new empty directory
   * 
   * @param name
   *          The name of the directory to create. If null, generate a name
   *          based on prefix and current time.
   * @param prefix
   *          The prefix string to be used in generating the file's name
   * @param parentDir
   *          The parent directory in which the directory is to be created, or
   *          null if the default temporary-file directory is to be used
   * @param clear
   *          if true and if this directory already exists the content is deep
   *          deleted
   * @return An abstract pathname denoting a newly-created empty directory
   */
  public static File createTempDir(String name, String prefix, File parentDir, boolean clear) {
    File tmpDir = getTempDir(name, prefix, parentDir);

    if (clear && tmpDir.exists()) {
      deepDelete(tmpDir);
    }

    tmpDir.mkdirs();
    return tmpDir;
  }

  /**
   * Returns An abstract pathname denoting an empty directory (this directory is
   * not created).
   * 
   * @param name
   *          The name of the directory to create. If null, generate a name.
   * @param prefix
   *          The prefix string to be used in generating the file's name
   * @param parentDir
   *          The parent directory in which the directory is to be created, or
   *          null if the default temporary-file directory is to be used
   * @return An abstract pathname denoting an empty directory
   */
  public static File getTempDir(String name, String prefix, File parentDir) {
    prefix = prefix == null ? "ejpt" : prefix;
    parentDir = parentDir != null ? parentDir : new File(System.getProperty("java.io.tmpdir"));

    if (name == null) {
      try {
        File temp = File.createTempFile(prefix, "");
        name = temp.getName();
        temp.delete();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    return new File(parentDir, name);
  }

  public static void deepDelete(File file) {

    // Recursive calls
    if (file.isDirectory()) {
      String[] list = file.list();
      for (int i = 0; i < list.length; i++) {
        deepDelete(new File(file, list[i]));
      }
    }

    // Perform delete
    boolean deleted = file.delete();
    if (!deleted) {
      logger.warn("Cannot delete file " + file);
    }
  }

  public static File getDestinationFile(File destinationDirectory, File sourceDirectory, File file) {
    String fileRelativePath = getRelativePath(sourceDirectory, file);
    return new File(destinationDirectory, fileRelativePath);
  }

  public static File findPluginXMLFile(File directory) throws FileNotFoundException {
    Collection<File> files = FileUtils.listFiles(directory, FileFilterUtils.nameFileFilter(ParseConstants.PLUGIN_XML),
        TrueFileFilter.INSTANCE);

    if (!files.isEmpty() && files.size() == 1) {
      return files.iterator().next();
    }

    throw new FileNotFoundException("'plugin.xml' not found in " + directory);

  }

}
