package com.jalios.jcmstools.transversal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class JToolsPropertiesUtil {

  public static Set<String> getExcludedElements(String syncConfPath, String keyProp) {
    Set<String> excludedElements = new TreeSet<String>();
    Properties properties = new Properties();
    FileInputStream in;
    try {
      in = new FileInputStream(syncConfPath);
      properties.load(in);
      in.close();

      String propValue = properties.getProperty(keyProp);
      if (propValue != null) {
        //excludedElements.addAll(Util.splitToList(propValue, ","));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return excludedElements;
  }

}
