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

import static com.jalios.ejpt.parser.ParseConstants.FILE_INDEX_LISTENER;
import static com.jalios.ejpt.parser.ParseConstants.LINK_INDEX_LISTENER;
import static com.jalios.ejpt.parser.ParseConstants.PLUGIN_PRIVATE_PATH;
import static com.jalios.ejpt.parser.ParseConstants.PLUGIN_PUBLIC_PATH;
import static com.jalios.ejpt.parser.ParseConstants.PLUGIN_XML;
import static com.jalios.ejpt.parser.ParseConstants.TEMPLATES_TAG;
import static com.jalios.ejpt.parser.ParseConstants.TEMPLATE_TAG;
import static com.jalios.ejpt.parser.ParseConstants.XML_COMPONENTS;
import static com.jalios.ejpt.parser.ParseConstants.XML_OPENAPI;
import static com.jalios.ejpt.parser.ParseConstants.XML_OPENAPI_RESOURCE;
import static com.jalios.ejpt.parser.ParseConstants.XML_PRIVATE;
import static com.jalios.ejpt.parser.ParseConstants.XML_PUBLIC;
import static com.jalios.ejpt.parser.ParseConstants.XML_TYPE;
import static com.jalios.ejpt.parser.ParseConstants.XML_TYPES;
import static com.jalios.ejpt.parser.ParseConstants.XML_WEBAPP;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.jalios.ejpt.sync.utils.IOUtil;
import com.jalios.ejpt.sync.utils.Util;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public final class ParseJcmsPluginXml implements ParseService {
  private static final Logger logger = Logger.getLogger(ParseJcmsPluginXml.class);

  private String name = null;
  private File rootDirectory;
  // Internal
  private Document domStructure = null;
  private Set<String> fileSet = Collections.synchronizedSet(new HashSet<String>());

  public ParseInfo parse(File directory) throws ParseException {
    rootDirectory = directory;
    File pluginFile;
    try {
      pluginFile = IOUtil.findPluginXMLFile(new File(directory, "WEB-INF/plugins"));
    } catch (FileNotFoundException exception) {
      logger.error(exception.getMessage());
      throw new ParseException(exception.getMessage());
    }
    return internalParse(pluginFile);

  }

  private ParseInfo internalParse(File pluginFile){
    ParseInfo parseInfo = new ParseInfo();

    domStructure = ParseUtil.getDomStructure(pluginFile);
    Element root = domStructure.getRootElement();
    name = root.getAttributeValue("name");
    parseInfo.setPluginName(name);

    if (domStructure == null) {
      return parseInfo;
    }

    parseInfo.setFilesPath(getAllFiles(true, false));
    return parseInfo;
  }

  /**
   * Returns a Set of relative path to all Plugins files
   * 
   * @param sources
   *          include java sources in the path
   * @param generated
   *          include generated files
   * @return Set a set of relatives path
   */
  private Set<String> getAllFiles(boolean sources, boolean generated) {

    Set<String> set = new TreeSet<String>();

    // Adding plugin XML
    set.add(PLUGIN_PRIVATE_PATH + "/" + name + "/" + PLUGIN_XML);

    // Adding types path
    Map<String, Set<String>> typeMap = getTypesPath(sources);
    if (typeMap != null) {
      for (Map.Entry<String, Set<String>> itEntry : typeMap.entrySet()) {
        Set<String> itSet = itEntry.getValue();
        set.addAll(itSet);
      }
      if (generated) {
        // See TypeProcessor#removeType
        set.add("WEB-INF/classes/generated/" + LINK_INDEX_LISTENER + ".java");
        set.add("WEB-INF/classes/generated/" + LINK_INDEX_LISTENER + ".class");
        set.add("WEB-INF/classes/generated/" + FILE_INDEX_LISTENER + ".java");
        set.add("WEB-INF/classes/generated/" + FILE_INDEX_LISTENER + ".class");
      }
    }

    // Adding templates
    set.addAll(getTemplatesPath());

    // Adding workflow
    set.addAll(getWorkflowsPath());

    
    // Adding component
    Map<String, String> itemMap = getPluginComponentPath(sources);
    if (itemMap != null) {
      set.addAll(itemMap.keySet());
    }

    // Adding OpenAPI resource class
    itemMap = getOpenAPIResourcePath(sources);
    if (itemMap != null) {
      set.addAll(itemMap.keySet());
    }

    List<Runnable> threads = new LinkedList<Runnable>();
    threads.add(new CheckJavaPathThread());
    threads.add(new CheckJarsPathThread());
    threads.add(new CheckPrivatesPathThread());
    threads.add(new CheckPublicsPathThread());
    threads.add(new CheckWebappsPathThread());
    threads.add(new CheckHibernateMappingsPathThread());

    for (Runnable thread : threads){
      thread.run();
    }
    
    synchronized (fileSet) {
      set.addAll(fileSet);
    }

    return set;
  }

  private class CheckJarsPathThread implements Runnable {

    @Override
    public void run() {
      Set<String> jarsPath = new TreeSet<String>();

      Element root = domStructure.getRootElement();
      Element jars = root.getChild("jars");
      if (jars == null) {
        return;
      }

      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> jarList = jars.getChildren("jar");
      if (jarList == null || jarList.size() == 0) {
        return;
      }

      for (Element itJar : jarList) {
        String itPath = itJar.getAttributeValue("path");
        if (itPath == null) {
          continue;
        }
        jarsPath.add("WEB-INF/lib/" + itPath);
      }
      fileSet.addAll(jarsPath);
    }

  }
  
  private class CheckJavaPathThread implements Runnable {

    @Override
    public void run() {
      Set<String> javaPaths = new TreeSet<String>();

      Element root = domStructure.getRootElement();
      Element javaClasses = root.getChild("java-classes");
      if (javaClasses == null) {
        return;
      }

      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> javaClassesPath = javaClasses.getChildren("java");
      if (javaClassesPath == null || javaClassesPath.size() == 0) {
        return;
      }

      for (Element itJava : javaClassesPath) {
        fillJavaSet(itJava, javaPaths, true);
      }
      fileSet.addAll(javaPaths);
    }

  }
  
  private class CheckHibernateMappingsPathThread implements Runnable {

    @Override
    public void run() {
      Element root = domStructure.getRootElement();
      Element hibernate = root.getChild("hibernate");
      if (hibernate == null) {
        return;
      }

      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> mappingList = hibernate.getChildren("mapping");
      if (mappingList == null || mappingList.size() == 0) {
        return;
      }

      for (Element mapping : mappingList) {
        String resource = mapping.getAttributeValue("resource");
        if (resource == null) {
          continue;
        }
        fileSet.add("WEB-INF/classes/" + resource);
      }      
    }

  }  

  /**
   * Returns a Set of path to plugin's workflow.
   * 
   * @return Set of path
   */
  private Set<String> getWorkflowsPath() {
    Set<String> wfSet = new TreeSet<String>();

    if (domStructure == null) {
      return wfSet;
    }

    Element root = domStructure.getRootElement();
    Element wfs = root.getChild("workflows");
    if (wfs == null) {
      return wfSet;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> wfList = wfs.getChildren("workflow");
    if (wfList == null || wfList.size() == 0) {
      return wfSet;
    }

    for (Element itWf : wfList) {
      String itId = itWf.getAttributeValue("id");
      if (itId == null) {
        continue;
      }
      wfSet.add("WEB-INF/data/workflows/" + itId + ".xml");
    }

    return wfSet;
  }

  /**
   * Returns a Map of Type Mame / Relative path to declared plugin's type files
   * <ul>
   * <li>Type XML</li>
   * <li>Type Template XML</li>
   * <li>Declared custom java Files</li>
   * <li>Declared JSP</li>
   * <li>Declared Resources (preview, images, ...)</li>
   * </ul>
   * 
   * @param sources
   *          include java sources in the path
   * @param generated
   *          include generated content
   * @return Set a set of relatives path
   */
  private Map<String, Set<String>> getTypesPath(boolean sources) {
    Map<String, Set<String>> typeMap = new HashMap<String, Set<String>>();

    if (domStructure == null) {
      return typeMap;
    }

    Element root = domStructure.getRootElement();
    Element typesElm = root.getChild(XML_TYPES);

    if (typesElm == null) {
      return typeMap;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> typeList = typesElm.getChildren(XML_TYPE);
    if (Util.isEmpty(typeList)) {
      return typeMap;
    }

    for (Element itType : typeList) {
      String itName = itType.getAttributeValue("name");

      if (itName == null) {
        continue;
      }

      Set<String> pathSet = new TreeSet<String>();

      // Retrieve structure xml file
      pathSet.add("WEB-INF/data/types/" + itName + "/" + itName + ".xml");
      pathSet.add("WEB-INF/data/types/" + itName + "/" + itName + "-templates.xml");

      // Retrieve JSP
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> fileChildrenList = itType.getChildren("file");
      ParseUtil.fillElementPath(pathSet, fileChildrenList, "types/" + itName + "/", "file", true, null, null);
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> directoryChildrenList = itType.getChildren("directory");
      ParseUtil.fillElementPath(pathSet, directoryChildrenList, "types/" + itName + "/", "directory", true, null, null);

      // Retrieve Java
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> javaList = itType.getChildren("java");
      if (Util.notEmpty(javaList)) {
        for (Element itJava : javaList) {
          fillJavaSet(itJava, pathSet, sources);
        }
      }

      typeMap.put(itName, pathSet);
    }

    return typeMap;
  }

  /**
   * Returns a Set of path to plugin's type's templates.
   * 
   * @return Set of path
   */
  private Set<String> getTemplatesPath() {
    Set<String> templatePathSet = new HashSet<String>();

    List<String> tteList = retrieveTypeTemplateEntries();
    for (String ttePath : tteList) {
      templatePathSet.add(ttePath);
    }
    return templatePathSet;
  }

  private List<String> retrieveTypeTemplateEntries() {
    Element root = domStructure.getRootElement();
    Element typesEml = root.getChild(XML_TYPES);
    List<String> tteList = new ArrayList<String>(); // List

    if (typesEml == null) {
      return tteList;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> templates = typesEml.getChildren(TEMPLATES_TAG);
    if (templates == null) {
      return tteList;
    }

    // Iterate on <templates />
    for (Element itElm : templates) {
      String te = getTypeEntryFromElement(itElm, "type");
      if (te == null) {
        continue;
      }
      String folderPath = "types/" + te + "/";
      Set<String> tteSet = getTypeTemplateEntriesFromElement(itElm, folderPath);
      if (Util.isEmpty(tteSet)) {
      }
      tteList.addAll(tteSet);
    }

    return tteList;
  }

  private String getTypeEntryFromElement(Element itElm, String attr) {
    String itType = itElm.getAttributeValue(attr);
    return itType;

  }

  private Set<String> getTypeTemplateEntriesFromElement(Element itElm, String folderPath) {

    Set<String> tteSet = new HashSet<String>();
    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> elmList = itElm.getChildren(TEMPLATE_TAG);
    if (Util.isEmpty(elmList)) {
      return tteSet;
    }

    for (Element itTemplate : elmList) {
      String itDir = itTemplate.getAttributeValue("dir");
      String itPath = folderPath;
      if (itDir == null || !itDir.equals("type")) {
        itPath = "plugins/" + name + "/" + itPath;
      }

      tteSet.add(itPath + itTemplate.getAttributeValue("file"));
    }

    return tteSet;
  }

  private class CheckPublicsPathThread implements Runnable {

    @Override
    public void run() {
      Set<String> set = new TreeSet<String>();

      Element root = domStructure.getRootElement();
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> publicChildren1 = root.getChildren(XML_PUBLIC);
      ParseUtil.fillElementPath(set, publicChildren1, PLUGIN_PUBLIC_PATH + "/" + name + "/", "file", true, null, null);
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> publicChildren2 = root.getChildren(XML_PUBLIC);
      ParseUtil.fillElementPath(set, publicChildren2, PLUGIN_PUBLIC_PATH + "/" + name + "/", "directory", true, null,
          null);

      fileSet.addAll(set);
    }

  }

  private class CheckWebappsPathThread implements Runnable {

    @Override
    public void run() {
      Set<String> set = new TreeSet<String>();

      if (domStructure == null) {
        return;
      }

      Element root = domStructure.getRootElement();
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> webappChildren1 = root.getChildren(XML_WEBAPP);
      ParseUtil.fillElementPath(set, webappChildren1, "", "file", true, null, null);
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> webappChildren2 = root.getChildren(XML_WEBAPP);
      ParseUtil.fillElementPath(set, webappChildren2, "", "directory", true, null, null);
      fileSet.addAll(set);
    }

  }

  private class CheckPrivatesPathThread implements Runnable {

    @Override
    public void run() {
      Set<String> set = new TreeSet<String>();

      Element root = domStructure.getRootElement();
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> privateChildren1 = root.getChildren(XML_PRIVATE);
      ParseUtil
          .fillElementPath(set, privateChildren1, PLUGIN_PRIVATE_PATH + "/" + name + "/", "file", true, null, null);
      @SuppressWarnings("unchecked")
      // JDOM generics
      List<Element> privateChildren2 = root.getChildren(XML_PRIVATE);
      ParseUtil.fillElementPath(set, privateChildren2, PLUGIN_PRIVATE_PATH + "/" + name + "/", "directory", true, null,
          null);

      fileSet.addAll(set);
    }

  }

  private void fillPluginComponentPath(Map<String, String> itemMap, String tagName, boolean sources) {

    if (domStructure == null) {
      return;
    }

    Element root = domStructure.getRootElement();
    Element plugincpnt = root.getChild(XML_COMPONENTS);
    if (plugincpnt == null) {
      return;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> itemList = plugincpnt.getChildren(tagName);
    if (Util.isEmpty(itemList)) {
      return;
    }

    Set<String> itSet = new TreeSet<String>();

    // Extract Java/Class path
    for (Element itElm : itemList) {
      fillJavaSet(itElm, itSet, sources);
    }

    // Fill Map
    for (String itPath : itSet) {
      itemMap.put(itPath, tagName);
    }
  }

  /**
   * Returns a Map from path of plugin's components files to their nature
   * ("storelistener", "dblistener", ...).
   * 
   * @param sources
   *          boolean true to include java files
   * @return Map of path ==> nature of plugin component
   */
  private Map<String, String> getPluginComponentPath(boolean sources) {

    Map<String, String> itemMap = new TreeMap<String, String>();

    if (domStructure == null) {
      return itemMap;
    }

    fillPluginComponentPath(itemMap, "storelistener", sources);
    fillPluginComponentPath(itemMap, "dblistener", sources);
    fillPluginComponentPath(itemMap, "datacontroller", sources);
    fillPluginComponentPath(itemMap, "channellistener", sources);
    fillPluginComponentPath(itemMap, "queryfilter", sources);
    fillPluginComponentPath(itemMap, "cleanfilter", sources);
    fillPluginComponentPath(itemMap, "policyfilter", sources);
    fillPluginComponentPath(itemMap, "authenticationhandler", sources);
    fillPluginComponentPath(itemMap, "alarmlistener", sources);

    return itemMap;
  }

  /**
   * Returns a Map from path of plugin's OpenAPI resources files to their
   * uritemplate.
   * 
   * @param sources
   *          boolean true to include java files
   * @return Map of path of resource class ==> uri template
   */
  private Map<String, String> getOpenAPIResourcePath(boolean sources) {

    Map<String, String> pathMap = new TreeMap<String, String>();

    if (domStructure == null) {
      return pathMap;
    }

    Element root = domStructure.getRootElement();
    Element openapicpnt = root.getChild(XML_OPENAPI);
    if (openapicpnt == null) {
      return pathMap;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> restResourceList = openapicpnt.getChildren(XML_OPENAPI_RESOURCE);
    if (Util.isEmpty(restResourceList)) {
      return pathMap;
    }

    // Extract Java/Class path
    for (Element itElm : restResourceList) {

      String uriTemplate = itElm.getAttributeValue("uriTemplate");
      Set<String> itSet = new TreeSet<String>();

      fillJavaSet(itElm, itSet, sources);
      for (String itPath : itSet) {
        pathMap.put(itPath, uriTemplate);
      }
    }
    return pathMap;
  }

  private void fillJavaSet(Element itJava, Set<String> pathSet, boolean sources) {
    String itClass = itJava.getAttributeValue("class");
    // Check attribute class
    if (itClass != null) {
      pathSet.addAll(getClassFiles(itClass));
      return;
    }

    String itPackage = itJava.getAttributeValue("package");
    // Check attribute package
    if (itPackage != null) {
      // String excludesPattern = itJava.getAttributeValue("excludes");
      pathSet.addAll(getPackageClassFiles(rootDirectory, itPackage, sources));
      return;
    }
  }

  public Set<String> getClassFiles(String fullname) {
    String fullpath = fullname.replace('.', '/');
    Set<String> pathSet = new HashSet<String>();
    pathSet.add("WEB-INF/classes/" + fullpath + ".java");
    return pathSet;
  }

  private Set<String> getPackageClassFiles(File realPath, String fullPackage, boolean sources) {
    String packagePath = fullPackage.replace('.', '/');
    File packageFolder = new File(realPath, "/WEB-INF/classes/" + packagePath);

    Set<String> pathSet = new HashSet<String>(5);

    // Check folder exists
    if (!packageFolder.exists()) {
      return pathSet;
    }

    File[] files = packageFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory() || file.getName().endsWith(".java");
      }
    });

    if (files == null || files.length == 0) {
      return pathSet;
    }

    for (File file : files) {
      String name = file.getName();
      if (file.isDirectory()) {
        pathSet.addAll(getPackageClassFiles(realPath, packagePath + "/" + name, sources));
      } else {
        // Add .class File
        pathSet.add("WEB-INF/classes/" + packagePath + "/" + name);

        // Add .java
        if (sources) {
          String shortname = name.substring(0, name.lastIndexOf('.'));
          if (shortname.indexOf("$") == -1) { // Skip inner class
            pathSet.add("WEB-INF/classes/" + packagePath + "/" + shortname + ".java");
          }
        }
      }
    }

    return pathSet;
  }

}
