package com.jalios.jcmsplugin.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestEntityResolver implements EntityResolver {
  private File dtdFile;

  public TestEntityResolver(File dtdFile) {
    this.dtdFile = dtdFile;
  }

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    return new InputSource(new FileInputStream(dtdFile));
  }
}

