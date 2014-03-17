package com.jalios.ejpt.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class InternalEntityResolver implements EntityResolver {
  private final HashMap<String, String> dtdMap = new HashMap<String, String>();

  public InternalEntityResolver() {
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.0//EN", "jcms-plugin-1.0.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.1//EN", "jcms-plugin-1.1.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.2//EN", "jcms-plugin-1.2.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.3//EN", "jcms-plugin-1.3.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.4//EN", "jcms-plugin-1.4.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.5//EN", "jcms-plugin-1.5.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.6//EN", "jcms-plugin-1.6.dtd");
  }

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    String filename = dtdMap.get(publicId);
    if (filename == null) {
      return null;
    }
    
    InputStream is = this.getClass().getResourceAsStream("/" + filename);
    if (is == null){
      System.out.println("error : cannot find dtd " + filename);
      return null;
    }
    return new InputSource(is);
  }
}
