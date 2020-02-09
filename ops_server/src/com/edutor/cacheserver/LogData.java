package com.edutor.cacheserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.ServletContext;

import com.google.gson.Gson;
/*
 * {
"ReportType":"LOCAL_DOWNLOAD_FAILED",
"UserRequestUrl":"",
"UserId":"EA-001",
"crash":"asd",
"DebugInfo":"",
"OtherData":"",
"CdnId":"",
"OccuredTime":""
}
*/

public class LogData {
	public transient ServletContext mServeletContext;
	public String ReportType;
	//public Object OtherData;
	public String UserRequestUrl;
	public String UserId;
	public String DebugInfo;
	//TODO why is this private??
	private String crash;
	public String CdnId;
	public String OccuredTime;

	
	
	public LogData(ServletContext mServeletContext, String mReportType,
			Object otherData, Exception mException, String mUserRequestUrl,
			String mUserId, String mDebugInfo) {
		this.mServeletContext = mServeletContext;
		this.ReportType = mReportType;
		//this.OtherData = otherData;
		this.crash = getCrash(mException);
		this.UserRequestUrl = mUserRequestUrl;
		this.UserId = mUserId;
		this.DebugInfo = mDebugInfo;
		this.CdnId = mServeletContext.getInitParameter("edutor_cdn_user_id");
		this.OccuredTime = new Date().toString();
	}


	private String getCrash(Exception mException) {
		if(mException == null)
			return null;
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		mException.printStackTrace(pw);
		return sw.toString();
	}


	public LogData() {
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}