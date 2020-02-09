package com.edutor.cacheserver;

import org.apache.log4j.Logger;

public class CdnApplicationLogger {
	
	private static Logger ApplicationLogger = Logger.getLogger(CdnApplicationLogger.class.getName());
	
	public static void debug(String userID, String url, String message){
		ApplicationLogger.debug(userID + "\t" + message + "\t" + url);
	}
	
	public static void error(String userID, String url, String message){
		ApplicationLogger.error(userID + "\t" + message + "\t" + url);
	}
	
	public static void info(String userID, String url, String message){
		ApplicationLogger.info(userID + "\t" + message + "\t" + url);
	}

	public static void error(String userID, String message) {
		ApplicationLogger.error(userID + "\t" + message);
	}

	public static void error(String cDN_DEVICE_ID, String message, Exception e) {
		ApplicationLogger.error(cDN_DEVICE_ID + "\t" + message, e);
	}
	
}
