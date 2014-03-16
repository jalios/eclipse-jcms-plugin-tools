package com.jalios.jcmsplugin.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.xerces.parsers.SAXParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;

public class ParseUtil {

  public static Document getDomStructure(File file, EntityResolver entityResolver) {
    try {
      InputStream inputStream = new FileInputStream(file);
      SAXBuilder builder = getSAXBuilder(entityResolver);
      Document document = builder.build(inputStream);
      return document;
    } catch (IOException ex) {
      ex.printStackTrace();
    } catch (JDOMException ex) {
      ex.printStackTrace();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  public static void fillElementPath(Collection<String> paths, List<Element> elmList, String prefix, String tagName,
      boolean deep, String matchAttribute, String matchValue) {
    if (isEmpty(elmList)) {
      return;
    }

    for (Iterator<Element> it = elmList.iterator(); it.hasNext();) {
      Element itFile = (Element) it.next();

      // Deep recursion
      if (!itFile.getQualifiedName().equals(tagName)) {
        if (deep) {
          @SuppressWarnings("unchecked")
          // JDOM generics
          List<Element> childrenList = itFile.getChildren();
          fillElementPath(paths, childrenList, prefix, tagName, deep, matchAttribute, matchValue);
        }
        continue;
      }

      // Check path attributes
      String itPath = (String) itFile.getAttributeValue("path");
      if (itPath == null) {
        continue;
      }

      // Check match value (attribute may have | separator)
      if (matchAttribute != null && matchValue != null) {
        String[] itAttrValue = split(itFile.getAttributeValue(matchAttribute), "|");
        for (int i = 0; i < itAttrValue.length; i++) {
          if (matchValue.equals(itAttrValue[i])) {
            paths.add(prefix + itPath);
            continue;
          }
        }
      }
      // Otherwise add it directly
      else {
        paths.add(prefix + itPath);
      }
    }
  }
  
  
  private static SAXBuilder getSAXBuilder(EntityResolver entityResolver) {
    SAXBuilder builder = new SAXBuilder(SAXParser.class.getName(), false);
    builder.setExpandEntities(true);
    builder.setValidation(true);
    builder.setEntityResolver(entityResolver);
    return builder;
  }

  public static boolean isEmpty(Collection<?> c) {
    return c == null || c.size() == 0;
  }

  public static String[] split(String str, String delim) {
    if (str == null) {
      return null;
    }
    ArrayList<String> list = splitToList(str, delim);
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
  public static ArrayList<String> splitToList(String str, String delim) {
    if (str == null) {
      return null;
    }
    ArrayList<String> list = new ArrayList<String>();
    StringTokenizer st = delim != null ? new StringTokenizer(str, delim) : new StringTokenizer(str);
    while (st.hasMoreTokens()) {
      list.add(st.nextToken().trim());
    }
    return list;
  }

}
