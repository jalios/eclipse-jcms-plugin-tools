package com.jalios.jcmsplugin.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class JcmsEntityResolver implements EntityResolver {
  private final HashMap<String, String> dtdMap = new HashMap<String, String>();
  private final File dtdDirectory;

  public JcmsEntityResolver(File dtdDirectory) {
    this.dtdDirectory = dtdDirectory;
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.0//EN", "jalios/jcms-plugin-1.0.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.1//EN", "jalios/jcms-plugin-1.1.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.2//EN", "jalios/jcms-plugin-1.2.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.3//EN", "jalios/jcms-plugin-1.3.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.4//EN", "jalios/jcms-plugin-1.4.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.5//EN", "jalios/jcms-plugin-1.5.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.6//EN", "jalios/jcms-plugin-1.6.dtd");
  }

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    String filename = dtdMap.get(publicId);
    if (filename == null) {
      return null;
    }
    File dtdFile = new File(dtdDirectory.getAbsolutePath(), filename);
    return new InputSource(new FileInputStream(dtdFile));
  }
}
