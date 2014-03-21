package com.jalios.ejpt.sync.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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

  public static boolean isFileNameFoundInFiles(List<File> files, String fileName) {
    for (File itFile : files) {
      if (itFile.getName().equals(fileName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Maths talk : C1 \ (C1 intersection C2)
   * 
   * @param firstCollection
   * @param secondCollection
   * @return
   */
  public static <T> Collection<T> getElementsNotFoundInSecondCollection(Collection<T> firstCollection,
      Collection<T> secondCollection) {
    List<T> collection = new ArrayList<T>();

    for (T element : firstCollection) {
      if (secondCollection.contains(element)) {
        continue;
      }
      collection.add(element);
    }

    return collection;
  }

  public static Properties loadProperties(File file) {
    Properties properties = new Properties();
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
