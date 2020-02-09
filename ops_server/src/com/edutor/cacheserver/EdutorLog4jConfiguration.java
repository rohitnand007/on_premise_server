package com.edutor.cacheserver;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class EdutorLog4jConfiguration extends DefaultConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7055880795201623344L;
	private static final String LOG_FILE_NAME = "123.log";
	public static final String LOG_FILE_NAME_PATTERN = LOG_FILE_NAME + "-%d{dd-MM-yyy}";
	 public static final String PATTERN_LAYOUT = "[%d] [%t] [%-5level] - %msg (%logger{1}:%L) %n%throwable";
	    
	 
	public EdutorLog4jConfiguration() {
		setName("app-log4j2");
		String root = System.getProperty("APP_ROOT", "/tmp");
		if (!root.endsWith("/")) {
			root += "/";
		}
		// MARKER
		Layout<? extends Serializable> layout = PatternLayout.createLayout(
				PATTERN_LAYOUT, null, null, null, isShutdownHookEnabled, isShutdownHookEnabled, null, root);

		String oneDay = TimeUnit.DAYS.toMillis(1) + "";
		String oneMB = (1024 * 1024) + "";
		final TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy
				.createPolicy(oneDay, "true");
		final SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy
				.createPolicy(oneMB);
		final CompositeTriggeringPolicy policy = CompositeTriggeringPolicy
				.createPolicy(timeBasedTriggeringPolicy,
						sizeBasedTriggeringPolicy);
		final DefaultRolloverStrategy strategy = DefaultRolloverStrategy
				.createStrategy("7", "1", null, Deflater.DEFAULT_COMPRESSION
						+ "", this);
		
		
		
		String fileName = "c:/"+ LOG_FILE_NAME;
		String filePattern = LOG_FILE_NAME_PATTERN;
		String append = "true";
		String name = "app-log-file-appender";
		String bufferedIO = "true";
		String bufferSizeStr = "true";
		String immediateFlush = "true";
		Filter filter = null;
		String ignore = null;
		String advertise = null;
		String advertiseURI = null;
		Configuration config = null;
		
		Appender appender = RollingFileAppender.createAppender(fileName,
				filePattern, append, name, bufferedIO, bufferSizeStr,
				immediateFlush, policy, strategy, layout, filter, ignore,
				advertise, advertiseURI, config); 
		addAppender(appender);
//		org.apache.logging.log4j.Level level;
//		getRootLogger().addAppender(appender, level, filter);;
	}

}