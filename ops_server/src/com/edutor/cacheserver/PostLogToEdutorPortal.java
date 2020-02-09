package com.edutor.cacheserver;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class PostLogToEdutorPortal implements Runnable {

	public LogData data;

	public PostLogToEdutorPortal() {
	}
	public PostLogToEdutorPortal(ServletContext pServeletContext,
			String pReportType, Exception exception, String userid,
			String userRequestUrl, Object pOtherData, String pDebugInfo) {
		data = new LogData(pServeletContext, pReportType, pOtherData,
				exception, userRequestUrl, userid, pDebugInfo);
	}

	@Override
	public void run() {
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("logdata", data.toString()));
		NetworkUtils.sendPost(NetworkUtils.CDN_REPORT_URL, params);
	}

}
