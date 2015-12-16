package com.fr.atgext.management.client;

public class ClientUtils {
  /**
   * Checks that a string is not blank
   * @param pString
   * @return
   */
  public static boolean isBlankString(String pString) {
    if (pString == null || "".equals(pString.trim()))
      return true;
    return false;
  }
  
  public static String getFilePath(String pFilePath) {
    // TODO construct a relative path if the specified path is not an absolute path
    return null;
  }
  
  /**
   * Prints an error message
   * @param pMessage
   */
  public static void printError(String pMessage) {
    System.err.println("**** Error : " + pMessage);
  }
  
  public static void printError(String pMessage, Throwable pException) {
    System.err.println("**** Error : " + pMessage);
    pException.printStackTrace(System.err);
  }
  
  /**
   * Prints debug message
   * @param pMessage
   */
  public static void printDebug(String pMessage) {
    System.out.println("**** Debug : " + pMessage);
  }
  
  /**
   * Prints warning messages
   * @param pMessage
   */
  public static void printWarning(String pMessage) {
    System.err.println("**** Warning : " + pMessage);
  }
}
