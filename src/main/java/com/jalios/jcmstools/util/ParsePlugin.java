package com.jalios.jcmstools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jalios.jcms.TypeProcessor;
import com.jalios.jcms.TypeTemplateEntry;
import com.jalios.jcms.plugin.PluginManager;
import com.jalios.util.Util;
import com.jalios.util.XmlUtil;

public class ParsePlugin {
	public static final String PLUGIN_XML = "plugin.xml";
	public static final String XML_PRIVATE = "private-files";
	public static final String XML_PUBLIC = "public-files";
	public static final String XML_WEBAPP = "webapp-files";
	public static final String XML_TYPES = "types";
	public static final String XML_TYPE = "type";
	public static final String XML_DEPENDENCIES = "dependencies";
	public static final String XML_DEPENDENCY = "dependency";
	public static final String XML_COMPONENTS = "plugincomponents";
	public static final String XML_HIBERNATE = "hibernate";
	public static final String XML_OPENAPI = "openapi";
	public static final String XML_OPENAPI_RESOURCE = "resource";
	private String name = null;
	private static EntityResolver entityResolver = null;
	private String webappDir = "";
	// Internal
	protected Document config = null;

	public ParsePlugin(String pWebappDir){
		this.webappDir = pWebappDir;		
	}
	/**
	 * Load a given Plugin Archive if it has not been already loadded
	 * 
	 * @param file
	 *            the folder that contains PLUGIN_XML
	 * @return Plugin or null if there is an error
	 */
	public Set<String> loadPluginXML(File file) {
		if (!file.isDirectory()) {
			return null;
		}

		File config = new File(file.getAbsolutePath() + "/" + PLUGIN_XML);
		if (!config.exists()) {
			return null;
		}

		try {
			InputStream is = new FileInputStream(config);
			return loadPluginQuietly(file.getName(), is, false);
		} catch (IOException ex) {

		}
		return null;
	}

	public EntityResolver getPluginEntityResolver() {
		if (entityResolver == null) {
			entityResolver = new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					/*
					 * String filename = dtdMap.get(publicId); if (filename ==
					 * null) { return null; }
					 */
					String path = webappDir + "/WEB-INF/jalios/" + "jcms-plugin-1.4.dtd";
					return new InputSource(new FileInputStream(path));
				}
			};
		}
		return entityResolver;
	}

	public SAXBuilder getSAXBuilder() {
		SAXBuilder builder = XmlUtil.getBuilder();
		builder.setValidation(true);
		builder.setEntityResolver(getPluginEntityResolver());
		return builder;
	}

	/**
	 * Parse the plugins.xml and build a new Plugin from it's root attribute
	 * "class"
	 * 
	 * @param config
	 *            the JDOM Document config
	 * @return Plugin the loaded Plugin from config
	 */
	public Set<String> loadConfig(Document config, boolean isDeployed) {
		if (config == null)
			return null;

		try {
			Element root = config.getRootElement();
			this.name = root.getAttributeValue("name");

			// Instanciate plugin
			return load(config, isDeployed);

		} catch (ClassCastException ex) {
		}

		return null;
	}

	/**
	 * Called by PluginManager during initialization to setup main variable from
	 * config file.
	 * 
	 * Do not access other plugins because they might not been already loadded
	 * wait init() method
	 * 
	 * @param config
	 *            JDOM Document of config plugin file
	 * @param isDeployed
	 *            true if the plugin has been deployed
	 */
	public Set<String> load(Document config, boolean isDeployed) {
		this.config = config;

		// Backup data

		// Retrieve xml main data
		Element root = config.getRootElement();
		Util.toInt(root.getAttributeValue("order"), 0);
		Util.toBoolean(root.getAttributeValue("jsync"), true);
		Util.split(Util.getString(root.getAttributeValue("appserver"), ""), "|");
		root.getAttributeValue("jcms");

		// Retrieve xml additional data
		Util.getString(root.getAttributeValue("url"), "");
		Util.getString(root.getAttributeValue("license"), "");

		XmlUtil.getLangChildrenMap(root, "label");
		XmlUtil.getLangChildrenMap(root, "description");

		return getAllPathSet(true, false);
	}

	/**
	 * Returns a Set of relative path to all Plugins files
	 * 
	 * @param sources
	 *            include java sources in the path
	 * @param generated
	 *            include generated files
	 * @return Set a set of relatives path
	 */
	public Set<String> getAllPathSet(boolean sources, boolean generated) {

		Set<String> set = new TreeSet<String>();

		// Adding plugin XML
		set.add(PluginManager.PLUGIN_PRIVATE_PATH + "/" + name + "/"
				+ PluginManager.PLUGIN_XML);

		// Adding types path
		Map<String, Set<String>> typeMap = getTypesPath(sources);
		if (Util.notEmpty(typeMap)) {
			for (Map.Entry<String, Set<String>> itEntry : typeMap.entrySet()) {
				Set<String> itSet = itEntry.getValue();
				set.addAll(itSet);
			}
			if (generated) {
				// See TypeProcessor#removeType
				set.add("WEB-INF/classes/generated/"
						+ TypeProcessor.LINK_INDEX_LISTENER + ".java");
				set.add("WEB-INF/classes/generated/"
						+ TypeProcessor.LINK_INDEX_LISTENER + ".class");
				set.add("WEB-INF/classes/generated/"
						+ TypeProcessor.FILE_INDEX_LISTENER + ".java");
				set.add("WEB-INF/classes/generated/"
						+ TypeProcessor.FILE_INDEX_LISTENER + ".class");
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
		if (Util.notEmpty(itemMap)) {
			set.addAll(itemMap.keySet());
		}

		// Adding OpenAPI resource class
		itemMap = getOpenAPIResourcePath(sources);
		if (Util.notEmpty(itemMap)) {
			set.addAll(itemMap.keySet());
		}

		// Adding private files
		set.addAll(getPrivatesPath());

		// Adding public files
		set.addAll(getPublicsPath());

		// Adding webapp files
		set.addAll(getWebappsPath());

		for (String path : set) {
			System.out.println(path);
		}
		return set;
	}

	/**
	 * Returns a Set of path to plugin's jars.
	 * 
	 * @return Set of path
	 */
	public Set<String> getJarsPath() {

		TreeSet<String> jarsSet = new TreeSet<String>();

		if (config == null) {
			return jarsSet;
		}

		Element root = config.getRootElement();
		Element jars = root.getChild("jars");
		if (jars == null) {
			return jarsSet;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> jarList = jars.getChildren("jar");
		if (Util.isEmpty(jarList)) {
			return jarsSet;
		}

		for (Element itJar : jarList) {
			String itPath = itJar.getAttributeValue("path");
			if (itPath == null) {
				continue;
			}
			jarsSet.add("WEB-INF/lib/" + itPath);
		}
		return jarsSet;
	}

	/**
	 * Returns a Set of path to plugin's java files.
	 * 
	 * @param sources
	 *            boolean true to include java files
	 * @return Set of path
	 */
	public Set<String> getJavaPath(boolean sources) {
		Set<String> javaSet = new TreeSet<String>();

		if (config == null) {
			return javaSet;
		}

		Element root = config.getRootElement();
		Element javaClasses = root.getChild("java-classes");
		if (javaClasses == null) {
			return javaSet;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> javaList = javaClasses.getChildren("java");
		if (Util.isEmpty(javaList)) {
			return javaSet;
		}

		for (Element itJava : javaList) {
			fillJavaSet(itJava, javaSet, sources);
		}

		return javaSet;
	}

	/**
	 * Returns a Set of path to plugin's Hibernate mappings (hbm).
	 * 
	 * @return Set of path
	 * @since jcms-6.0.0
	 */
	public Set<String> getHibernateMappingsPath() {

		TreeSet<String> pathSet = new TreeSet<String>();

		if (config == null) {
			return pathSet;
		}

		Element root = config.getRootElement();
		Element hibernate = root.getChild("hibernate");
		if (hibernate == null) {
			return pathSet;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> mappingList = hibernate.getChildren("mapping");
		if (Util.isEmpty(mappingList)) {
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
	public Set<String> getWorkflowsPath() {
		Set<String> wfSet = new TreeSet<String>();

		if (config == null) {
			return wfSet;
		}

		Element root = config.getRootElement();
		Element wfs = root.getChild("workflows");
		if (wfs == null) {
			return wfSet;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> wfList = wfs.getChildren("workflow");
		if (Util.isEmpty(wfList)) {
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
	 * Quietly load a given Plugin from it stream. Then close the stream.
	 * 
	 * @param name
	 * @param is
	 *            The Plugin InputStream
	 * @param archive
	 *            Is it an archive or a deployed folder
	 * @return Plugin or null if there is an error
	 */
	public Set<String> loadPluginQuietly(String name, InputStream is,
			boolean archive) {
		try {
			SAXBuilder builder = getSAXBuilder();
			Document d = builder.build(is);
			return loadConfig(d, !archive);
		} catch (JDOMException ex) {

		} catch (Exception ex) {
			System.out.println("error ?");
		}
		return null;
	}

	/**
	 * Returns a Map of Type Mame / Relative path to declared plugin's type
	 * files
	 * <ul>
	 * <li>Type XML</li>
	 * <li>Type Template XML</li>
	 * <li>Declared custom java Files</li>
	 * <li>Declared JSP</li>
	 * <li>Declared Resources (preview, images, ...)</li>
	 * </ul>
	 * 
	 * @param sources
	 *            include java sources in the path
	 * @param generated
	 *            include generated content
	 * @return Set a set of relatives path
	 */
	public Map<String, Set<String>> getTypesPath(boolean sources) {
		Map<String, Set<String>> typeMap = new HashMap<String, Set<String>>();

		if (config == null) {
			return typeMap;
		}

		Element root = config.getRootElement();
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
			pathSet.add("WEB-INF/data/types/" + itName + "/" + itName
					+ "-templates.xml");

			// Retrieve JSP
			@SuppressWarnings("unchecked")
			// JDOM generics
			List<Element> fileChildrenList = itType.getChildren("file");
			fillElementPath(pathSet, fileChildrenList, "types/" + itName + "/",
					"file", true, null, null);
			@SuppressWarnings("unchecked")
			// JDOM generics
			List<Element> directoryChildrenList = itType
					.getChildren("directory");
			fillElementPath(pathSet, directoryChildrenList, "types/" + itName
					+ "/", "directory", true, null, null);

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
	public Set<String> getTemplatesPath() {
		Set<String> templatePathSet = new HashSet<String>();

		List<TypeTemplateEntry> tteList = getTypeTemplateEntries();
		for (TypeTemplateEntry tte : tteList) {
			templatePathSet.add(tte.getPath());
		}
		return templatePathSet;
	}

	public List<TypeTemplateEntry> getTypeTemplateEntries() {
		return retrieveTypeTemplateEntries(false);
	}

	private List<TypeTemplateEntry> retrieveTypeTemplateEntries(boolean register) {
		Element root = config.getRootElement();
		Element typesEml = root.getChild(XML_TYPES);
		List<TypeTemplateEntry> tteList = new ArrayList<TypeTemplateEntry>(); // List

		if (typesEml == null) {
			return tteList;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> templates = typesEml
				.getChildren(TypeProcessor.TEMPLATES_TAG);
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
			Set<TypeTemplateEntry> tteSet = getTypeTemplateEntriesFromElement(
					itElm, folderPath);
			if (Util.isEmpty(tteSet)) {
			}
			tteList.addAll(tteSet);
		}

		return tteList;
	}

	private String getTypeEntryFromElement(Element itElm, String attr) {
		String itType = itElm.getAttributeValue(attr);
		return itType;
		/*
		 * if (itType == null){ return null; }
		 * 
		 * // Skip unresolable types Class<?> clazz = resolveClass(itType); if
		 * (clazz == null){ return null; }
		 * 
		 * TypeEntry te = channel.getTypeEntry(clazz); return te;
		 */
	}

	private Set<TypeTemplateEntry> getTypeTemplateEntriesFromElement(
			Element itElm, String folderPath) {

		Set<TypeTemplateEntry> tteSet = new HashSet<TypeTemplateEntry>();
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> elmList = itElm.getChildren(TypeProcessor.TEMPLATE_TAG);
		if (Util.isEmpty(elmList)) {
			return tteSet;
		}

		for (Element itTemplate : elmList) {
			String itDir = itTemplate.getAttributeValue("dir");
			String itPath = folderPath;
			if (itDir == null || !itDir.equals("type")) {
				itPath = "plugins/" + name + "/" + itPath;
			}

			TypeTemplateEntry tte = TypeTemplateEntry.decodeTemplate(
					itTemplate, itPath);
			tte.setPlugged(true);
			tteSet.add(tte);
		}

		return tteSet;
	}

	/**
	 * Returns a Set of path to plugin's public files.
	 * 
	 * @return Set of path
	 */
	public Set<String> getPublicsPath() {
		Set<String> set = new TreeSet<String>();

		if (config == null) {
			return set;
		}

		Element root = config.getRootElement();
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> publicChildren1 = root.getChildren(XML_PUBLIC);
		fillElementPath(set, publicChildren1, PluginManager.PLUGIN_PUBLIC_PATH
				+ "/" + name + "/", "file", true, null, null);
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> publicChildren2 = root.getChildren(XML_PUBLIC);
		fillElementPath(set, publicChildren2, PluginManager.PLUGIN_PUBLIC_PATH
				+ "/" + name + "/", "directory", true, null, null);

		return set;
	}

	/**
	 * Returns a Set of path to plugin's private files.
	 * 
	 * @return Set of path
	 */
	public Set<String> getPrivatesPath() {
		Set<String> set = new TreeSet<String>();

		if (config == null) {
			return set;
		}

		Element root = config.getRootElement();
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> privateChildren1 = root.getChildren(XML_PRIVATE);
		fillElementPath(set, privateChildren1,
				PluginManager.PLUGIN_PRIVATE_PATH + "/" + name + "/", "file",
				true, null, null);
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> privateChildren2 = root.getChildren(XML_PRIVATE);
		fillElementPath(set, privateChildren2,
				PluginManager.PLUGIN_PRIVATE_PATH + "/" + name + "/",
				"directory", true, null, null);
		return set;
	}

	/**
	 * Returns a Set of path to plugin's webapps files.
	 * 
	 * @return Set of path
	 */
	public Set<String> getWebappsPath() {
		Set<String> set = new TreeSet<String>();

		if (config == null) {
			return set;
		}

		Element root = config.getRootElement();
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> webappChildren1 = root.getChildren(XML_WEBAPP);
		fillElementPath(set, webappChildren1, "", "file", true, null, null);
		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> webappChildren2 = root.getChildren(XML_WEBAPP);
		fillElementPath(set, webappChildren2, "", "directory", true, null, null);
		return set;
	}

	protected void fillPluginComponentPath(Map<String, String> itemMap,
			String tagName, boolean sources) {

		if (config == null) {
			return;
		}

		Element root = config.getRootElement();
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
	 *            boolean true to include java files
	 * @return Map of path ==> nature of plugin component
	 */
	public Map<String, String> getPluginComponentPath(boolean sources) {

		Map<String, String> itemMap = new TreeMap<String, String>();

		if (config == null) {
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
	 *            boolean true to include java files
	 * @return Map of path of resource class ==> uri template
	 */
	public Map<String, String> getOpenAPIResourcePath(boolean sources) {

		Map<String, String> pathMap = new TreeMap<String, String>();

		if (config == null) {
			return pathMap;
		}

		Element root = config.getRootElement();
		Element openapicpnt = root.getChild(XML_OPENAPI);
		if (openapicpnt == null) {
			return pathMap;
		}

		@SuppressWarnings("unchecked")
		// JDOM generics
		List<Element> restResourceList = openapicpnt
				.getChildren(XML_OPENAPI_RESOURCE);
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

	// ------------------------------------------------------------------------
	// UTILITY READ XML DOCUMENT
	// ------------------------------------------------------------------------

	protected void fillElementPath(Collection<String> paths,
			List<Element> elmList, String prefix, String tagName, boolean deep,
			String matchAttribute, String matchValue) {
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
					fillElementPath(paths, childrenList, prefix, tagName, deep,
							matchAttribute, matchValue);
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
				String[] itAttrValue = Util.split(Util.getString(
						itFile.getAttributeValue(matchAttribute), ""), "|");
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

	protected void fillJavaSet(Element itJava, Set<String> pathSet,
			boolean sources) {
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
