package com.bwarner.siteanalysis.utils;

import org.slf4j.Logger;

public class Utils {

  public static void printLogHeader(final Logger log, final String title) {
    Utils.printLogHeader(log, title, null);
  }

  public static void printLogHeader(final Logger log, final String title, final String[] args) {
    log.info("#############################################");
    log.info("### ".concat(title));
    if (null != args)
      for (String arg : args) {
        log.info("### => " + arg);
      }
    log.info("#############################################");
  }

  public static String NEWLINE = System.getProperty("line.separator");

  public static String getStackTraceText(Throwable e) {
    String message = e.toString().concat(NEWLINE);
    StackTraceElement[] stackTrace = e.getStackTrace();
    for (StackTraceElement stackTraceElement : stackTrace)
      message = message.concat(stackTraceElement.toString()).concat(NEWLINE);
    return message;
  }
}
