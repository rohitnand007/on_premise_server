package com.edutor.cacheserver;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	public static String MANUAL_DOWNLOADS_DIR;
	public static String DB_CONNECTION_MANAGER = "DB_CONNECTION_MANAGER";

	private static final long PING_FREQUENCY = 10;//minutes
	private static final long UPDATE_CACHE_STATUS_FREQUENCY = 1; //days

	private ScheduledExecutorService jobscheduler;
	private String webAppPath;
	private static String CDN_USER_ID;
	public static String CDN_DEVICE_ID;
	private static final String CDB_ID = "";
	
    public void contextInitialized(ServletContextEvent servletContextEvent){
    	final ServletContext ctx = servletContextEvent.getServletContext();
        webAppPath = ctx.getRealPath("/");
        //initialize DB Connection
        CDN_DEVICE_ID = ctx.getInitParameter("edutor_cdn_id");
        CDN_USER_ID = ctx.getInitParameter("edutor_cdn_user_id");
        MANUAL_DOWNLOADS_DIR=ctx.getInitParameter("asset_path");
        initializeDBConnection(ctx);       
        initiate_log4j(ctx);

        jobscheduler = Executors.newSingleThreadScheduledExecutor();
        ctx.setAttribute("JobScheduler", jobscheduler);
        
        jobscheduler.schedule(new Runnable(){
			@Override
			public void run() {
				CacheStatus cStatus = new CacheStatus(ctx);
				cStatus.post();
				CDNMetadata mCdnMetadata = new CDNMetadata();
				mCdnMetadata.post();
			}
        	
        }, 0, TimeUnit.SECONDS);
        
        
        try {
			startPingToEdutorPortal(CDN_DEVICE_ID);
			updateCacheStatusToEdutor(ctx);
		} catch (MalformedURLException e) {
			//TODO ADD logging
			e.printStackTrace();
			schedulePostLogJob(ctx, ReportType.REDIRECT_FAILED,e, null, CDN_DEVICE_ID,NetworkUtils.PING_URL+"?device_id="+CDN_DEVICE_ID);
		}
    }
    
    public void initializeDBConnection(ServletContext ctx) {
    	if (ctx==null) {
			return;
		}
    	String mysqlURL = ctx.getInitParameter("mysqlURL");
        String dbURL = ctx.getInitParameter("dbURL");
        String user = ctx.getInitParameter("dbUser");
        String pwd = ctx.getInitParameter("dbPassword");
        DBConnectionManager connectionManager;
		try {
			if (ctx.getAttribute(DB_CONNECTION_MANAGER)==null) {
				connectionManager = DBConnectionManager.createInstance(mysqlURL,dbURL, user, pwd);
		        ctx.setAttribute(DB_CONNECTION_MANAGER, connectionManager);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		CdnApplicationLogger.debug(user,"db Connection", "DB Connection initialized successfully.");
	}
    
    private void initiate_log4ajprop(ServletContext ctx) {
//    	ConfigurationFactory.setConfigurationFactory(new EdutorLog4jConfiguration());
	}

	public void schedulePostLogJob(ServletContext pServeletContext,
			String ReportType, Exception exception, Object pOtherData,String userid,String mRequestUrl) {
		ScheduledExecutorService jobscheduler = (ScheduledExecutorService) pServeletContext
				.getAttribute("JobScheduler");
		if (jobscheduler == null){
			System.exit(-1);;
			return; // log and return
		}
		
		jobscheduler.schedule(new PostLogToEdutorPortal(pServeletContext,ReportType, exception,userid,mRequestUrl, pOtherData, null), 2, TimeUnit.SECONDS);
	}

    private void startPingToEdutorPortal(String cdnDeviceId) throws MalformedURLException {
		URL pingUrl = new URL(NetworkUtils.PING_URL+"?device_id="+cdnDeviceId);
		jobscheduler.scheduleAtFixedRate(new Pinger(pingUrl), 0, PING_FREQUENCY, TimeUnit.MINUTES);
	}
    
    private void updateCacheStatusToEdutor(ServletContext ctx) throws MalformedURLException{
		jobscheduler.scheduleAtFixedRate(new CacheStatus(ctx), 0, UPDATE_CACHE_STATUS_FREQUENCY, TimeUnit.DAYS);
    }

	private void initiate_log4j(ServletContext ctx) {
		//initialize log4j
//        tryInitiateLogger(ctx);
        System.out.println("log4j configured properly -- 00");
        CdnApplicationLogger.error(CDN_DEVICE_ID,"log4j getRootLogger properly -- 2");
	}

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Connection con = (Connection) servletContextEvent.getServletContext().getAttribute("DBConnection");
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService jobscheduler = (ScheduledExecutorService) servletContextEvent.getServletContext().getAttribute("JobScheduler");
        
        try {
			jobscheduler.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
     
}