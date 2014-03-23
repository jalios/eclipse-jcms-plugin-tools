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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *  
 * @author Xuan Tuong LE (lxuong@gmail.com)
 */
public class Util {
  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.size() == 0;
  }

  public static boolean notEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }

  public static String[] split(String str, String delim) {
    if (str == null) {
      return null;
    }
    List<String> list = splitToList(str, delim);
    return list.toArray(new String[list.size()]);
  }

  /**
   * Splits a String into a list of String.
   * 
   * @param str
   *          the String to split
   * @param delim
   *          the delimiter (same as StringTokenizer)
   * @return an ArrayList of String.
   * @see StringTokenizer
   */
  public static List<String> splitToList(String str, String delim) {
    if (str == null) {
      return new LinkedList<String>();
    }
    List<String> list = new LinkedList<String>();
    StringTokenizer st = delim != null ? new StringTokenizer(str, delim) : new StringTokenizer(str);
    while (st.hasMoreTokens()) {
      list.add(st.nextToken().trim());
    }
    return list;
  }

  public static File findByFileName(List<File> files, String fileName) throws FileNotFoundException{
    for (File itFile : files) {
      if (itFile.getName().equals(fileName)) {
        return itFile;
      }
    }
    throw new FileNotFoundException("The file name " + fileName + " doesn't exist");
  }

  public static Properties loadProperties(File file) {
    Properties properties = new Properties();
    if (file == null){
      return properties;
    }
    FileInputStream in;
    try {
      in = new FileInputStream(file);
      properties.load(in);
      in.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }

}
