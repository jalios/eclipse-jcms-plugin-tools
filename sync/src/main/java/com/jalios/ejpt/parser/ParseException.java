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

/**
 * 
 * @author Xuan Tuong LE - lxtuong@gmail.com
 * 
 */
public class ParseException extends Exception {
  private static final long serialVersionUID = 6325569199321822020L;

  public ParseException(String message) {
    super(message);
  }
}
