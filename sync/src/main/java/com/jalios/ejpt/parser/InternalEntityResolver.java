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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
class InternalEntityResolver implements EntityResolver {
  private static final Logger logger = Logger.getLogger(InternalEntityResolver.class);
  private final Map<String, String> dtdMap = new HashMap<String, String>();

  public InternalEntityResolver() {
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.0//EN", "jcms-plugin-1.0.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.1//EN", "jcms-plugin-1.1.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.2//EN", "jcms-plugin-1.2.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.3//EN", "jcms-plugin-1.3.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.4//EN", "jcms-plugin-1.4.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.5//EN", "jcms-plugin-1.5.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.6//EN", "jcms-plugin-1.6.dtd");
    dtdMap.put("-//JALIOS//DTD JCMS-PLUGIN 1.7//EN", "jcms-plugin-1.7.dtd");
  }

  private boolean isPublicIdExist(String publicId){
    return dtdMap.get(publicId) != null;
  }
  
  public void put(String key, String value) {
    if (isPublicIdExist(key)) {
      return;
    }
    dtdMap.put(key, value);
  }

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    String filename = dtdMap.get(publicId);
    if (filename == null) {
      String message = "Unknown dtd specification : " + publicId;
      logger.error(message);
      throw new SAXException(message);
    }

    InputStream is = this.getClass().getResourceAsStream("/" + filename);
    if (is == null) {
      String message = "Internal DTD file not found with filename " + filename;
      logger.error(message);
      throw new SAXException(message);
    }

    return new InputSource(is);
  }
}
