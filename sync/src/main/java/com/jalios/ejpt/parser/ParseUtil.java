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
package com.jalios.ejpt.parser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.jalios.ejpt.sync.utils.BlackListFilter;
import com.jalios.ejpt.sync.utils.IOUtil;
import com.jalios.ejpt.sync.utils.Util;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public class ParseUtil {
  private static final Logger logger = Logger.getLogger(ParseJcmsPluginXml.class);

  public static Document getDomStructure(File file) {
    try {
      InputStream inputStream = new FileInputStream(file);
      SAXBuilder builder = getSAXBuilderWithInternalResolver();
      Document document = builder.build(inputStream);
      return document;
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    } catch (JDOMException ex) {
      logger.error(ex.getMessage());
    } catch (Exception ex) {
      logger.error(ex.getMessage());
    }
    return null;
  }

  public static void fillElementPath(Collection<String> paths, List<Element> elmList, String prefix, String tagName,
      boolean deep, String matchAttribute, String matchValue) {
    if (Util.isEmpty(elmList)) {
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
        String[] itAttrValue = Util.split(itFile.getAttributeValue(matchAttribute), "|");
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

  private static SAXBuilder getSAXBuilderWithInternalResolver() {
    SAXBuilder builder = new SAXBuilder(SAXParser.class.getName(), false);
    builder.setValidation(true);
    builder.setEntityResolver(new InternalEntityResolver());
    return builder;
  }

  public static File getPrivatePluginDirectory(File pluginDirectory) {
    File rootDirectoryPlugins = new File(pluginDirectory, "WEB-INF/plugins");
    File pluginPrivateDirectory = null;
    File[] rootDirectories = rootDirectoryPlugins.listFiles(new BlackListFilter.Builder().build());
    // only one
    if (rootDirectoryPlugins.isDirectory() && (rootDirectories.length == 1)) {
      pluginPrivateDirectory = rootDirectories[0];
    }
    return pluginPrivateDirectory;
  }

  public static List<File> getPluginXmlDeclaredFiles(File projectRootDirectory, FileFilter filter) {
    // check status from plugin.xml
    ParseService parser = new ParseJcmsPluginXml();
    ParseInfo info = new ParseInfo();

    try {
      info = parser.parse(projectRootDirectory);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    List<File> files = new LinkedList<File>();

    for (String declaredFilePath : info.getFilesPath()) {
      File declaredFile = new File(projectRootDirectory, declaredFilePath);
      if (declaredFile.isDirectory()) {
        files.addAll(IOUtil.deepListFiles(declaredFile, filter));
        continue;
      }

      files.add(new File(projectRootDirectory, declaredFilePath));
    }

    return files;
  }

}
