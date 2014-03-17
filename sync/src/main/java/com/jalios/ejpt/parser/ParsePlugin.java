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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.xml.sax.EntityResolver;

public final class ParsePlugin {

  private static final ParsePlugin SINGLETON = new ParsePlugin();
  private String name = null;
  private String webappDirectory = "";
  // Internal
  protected Document domStructure = null;

  private ParsePlugin() {
  }

  public static ParsePlugin getParser() {
    return SINGLETON;
  }

  public PluginJCMS analyzeWithDefaultResolver(String webappDirectory, String pluginName) {
    this.webappDirectory = webappDirectory;

    File pluginFile = new File(webappDirectory, "WEB-INF/plugins/" + pluginName + "/" + PLUGIN_XML);
    if (!pluginFile.exists()) {
      return null;
    }

    EntityResolver resolver = new JcmsEntityResolver(new File(webappDirectory, "WEB-INF"));
    this.domStructure = ParseUtil.getDomStructure(pluginFile, resolver);
    Element root = this.domStructure.getRootElement();
    this.name = root.getAttributeValue("name");

    PluginJCMS plugin = new PluginJCMS();
    plugin.setFilesPath(getAllFiles(true, false));
    return plugin;
  }

  /**
   * Open for test only
   * @param webappDirectory
   * @param pluginName
   * @param resolver
   * @return
   */
  protected PluginJCMS analyze(String webappDirectory, String pluginName, EntityResolver resolver) {
    this.webappDirectory = webappDirectory;

    File pluginFile = new File(webappDirectory, "WEB-INF/plugins/" + pluginName + "/" + PLUGIN_XML);
    if (!pluginFile.exists()) {
      return null;
    }

    this.domStructure = ParseUtil.getDomStructure(pluginFile, resolver);
    Element root = this.domStructure.getRootElement();
    this.name = root.getAttributeValue("name");

    PluginJCMS plugin = new PluginJCMS();
    plugin.setFilesPath(getAllFiles(true, false));
    return plugin;
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

    // Adding jars
    set.addAll(getJarsPath());

    // Adding java
    set.addAll(getJavaPath(sources));

    // Adding Hibernate mappings
    set.addAll(getHibernateMappingsPath());

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

    // Adding private files
    set.addAll(getPrivatesPath());

    // Adding public files
    set.addAll(getPublicsPath());

    // Adding webapp files
    set.addAll(getWebappsPath());

    return set;
  }

  /**
   * Returns a Set of path to plugin's jars.
   * 
   * @return Set of path
   */
  private Set<String> getJarsPath() {

    Set<String> jarsPath = new TreeSet<String>();

    if (domStructure == null) {
      return jarsPath;
    }

    Element root = domStructure.getRootElement();
    Element jars = root.getChild("jars");
    if (jars == null) {
      return jarsPath;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> jarList = jars.getChildren("jar");
    if (jarList == null || jarList.size() == 0) {
      return jarsPath;
    }

    for (Element itJar : jarList) {
      String itPath = itJar.getAttributeValue("path");
      if (itPath == null) {
        continue;
      }
      jarsPath.add("WEB-INF/lib/" + itPath);
    }
    return jarsPath;
  }

  /**
   * Returns a Set of path to plugin's java files.
   * 
   * @param sources
   *          boolean true to include java files
   * @return Set of path
   */
  private Set<String> getJavaPath(boolean sources) {
    Set<String> javaPaths = new TreeSet<String>();

    if (domStructure == null) {
      return javaPaths;
    }

    Element root = domStructure.getRootElement();
    Element javaClasses = root.getChild("java-classes");
    if (javaClasses == null) {
      return javaPaths;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> javaClassesPath = javaClasses.getChildren("java");
    if (javaClassesPath == null || javaClassesPath.size() == 0) {
      return javaPaths;
    }

    for (Element itJava : javaClassesPath) {
      fillJavaSet(itJava, javaPaths, sources);
    }

    return javaPaths;
  }

  /**
   * Returns a Set of path to plugin's Hibernate mappings (hbm).
   * 
   * @return Set of path
   */
  private Set<String> getHibernateMappingsPath() {

    TreeSet<String> pathSet = new TreeSet<String>();

    if (domStructure == null) {
      return pathSet;
    }

    Element root = domStructure.getRootElement();
    Element hibernate = root.getChild("hibernate");
    if (hibernate == null) {
      return pathSet;
    }

    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> mappingList = hibernate.getChildren("mapping");
    if (mappingList == null || mappingList.size() == 0) {
      return pathSet;
    }

    for (Element mapping : mappingList) {
      String resource = mapping.getAttributeValue("resource");
      if (resource == null) {
        continue;
      }
      pathSet.add("WEB-INF/classes/" + resource);
    }
    return pathSet;
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
    if (ParseUtil.isEmpty(typeList)) {
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
      if (!ParseUtil.isEmpty(javaList)) {
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
      if (ParseUtil.isEmpty(tteSet)) {
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
    if (ParseUtil.isEmpty(elmList)) {
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

  /**
   * Returns a Set of path to plugin's public files.
   * 
   * @return Set of path
   */
  private Set<String> getPublicsPath() {
    Set<String> set = new TreeSet<String>();

    if (domStructure == null) {
      return set;
    }

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

    return set;
  }

  /**
   * Returns a Set of path to plugin's private files.
   * 
   * @return Set of path
   */
  private Set<String> getPrivatesPath() {
    Set<String> set = new TreeSet<String>();

    if (domStructure == null) {
      return set;
    }

    Element root = domStructure.getRootElement();
    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> privateChildren1 = root.getChildren(XML_PRIVATE);
    ParseUtil.fillElementPath(set, privateChildren1, PLUGIN_PRIVATE_PATH + "/" + name + "/", "file", true, null, null);
    @SuppressWarnings("unchecked")
    // JDOM generics
    List<Element> privateChildren2 = root.getChildren(XML_PRIVATE);
    ParseUtil.fillElementPath(set, privateChildren2, PLUGIN_PRIVATE_PATH + "/" + name + "/", "directory", true, null,
        null);
    return set;
  }

  /**
   * Returns a Set of path to plugin's webapps files.
   * 
   * @return Set of path
   */
  private Set<String> getWebappsPath() {
    Set<String> set = new TreeSet<String>();

    if (domStructure == null) {
      return set;
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
    return set;
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
    if (ParseUtil.isEmpty(itemList)) {
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
    if (ParseUtil.isEmpty(restResourceList)) {
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
    if (itClass == null) {
      return;
    }

    pathSet.addAll(getClassFiles(itClass));
  }

  public Set<String> getClassFiles(String fullname) {
    String fullpath = fullname.replace('.', '/');
    Set<String> pathSet = new HashSet<String>();
    pathSet.add("WEB-INF/classes/" + fullpath + ".java");
    return pathSet;
  }

}
