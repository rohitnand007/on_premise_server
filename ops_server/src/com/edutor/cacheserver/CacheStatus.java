package com.edutor.cacheserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

public class CacheStatus implements Runnable{
	
	private transient ServletContext sc;
	public List<String> assets ;
	public String cdnid;

	

	
	public CacheStatus(ServletContext sc){
		this.sc = sc;
		cdnid = AppContextListener.CDN_DEVICE_ID;
	}
	
	public void post(){
		assets = getAssetListFromDB();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String json= new Gson().toJson(this);
		params.add(new BasicNameValuePair("cachestatus", json));
    	NetworkUtils.sendPost(NetworkUtils.CDN_CACHE_STATUS_URL, params);
	}
	
	private List<String> getAssetListFromDB(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String query = "select filename from files ;";
			DBConnectionManager dbConnectionManager = (DBConnectionManager) sc.getAttribute(AppContextListener.DB_CONNECTION_MANAGER);
			Connection connection = dbConnectionManager.getConnection();
			ps = connection.prepareStatement(query);
			rs  = ps.executeQuery();
			List<String> list = new ArrayList<String>();
			while (rs != null && rs.next()) {
				list.add(rs.getString("filename"));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			CdnApplicationLogger.error(AppContextListener.CDN_DEVICE_ID,"Database connection",e);
			schedulePostLogJob(sc,ReportType.LOCAL_SQL_READ_FAILED, e,null);
		} catch (Exception e) {
			e.printStackTrace();
			CdnApplicationLogger.error(AppContextListener.CDN_DEVICE_ID,"Database connection",e);
			schedulePostLogJob(sc,ReportType.LOCAL_SQL_READ_FAILED, e,null);
		} finally {
			DBConnectionManager.close(rs, ps);
		}
		return null;
	}
	
	public void schedulePostLogJob(ServletContext pServeletContext,
			String ReportType, Exception exception, Object pOtherData) {
		ScheduledExecutorService jobscheduler = (ScheduledExecutorService) pServeletContext
				.getAttribute("JobScheduler");
		if (jobscheduler == null)
			return; // log and return
		jobscheduler.schedule(new PostLogToEdutorPortal(pServeletContext,ReportType, exception,"",NetworkUtils.CDN_CACHE_STATUS_URL, pOtherData, null), 2, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		post();
	}
}
