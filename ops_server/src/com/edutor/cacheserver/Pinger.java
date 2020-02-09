package com.edutor.cacheserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Pinger implements Runnable {
	
	private URL pingUrl;

	public Pinger(URL pPingUrl) {
		this.pingUrl = pPingUrl;
	}

	@Override
	public void run() {
		try {
			URLConnection connection = pingUrl.openConnection();
			connection.setConnectTimeout(30000);
			connection.connect();
			InputStream is = (InputStream)connection.getContent();
			int r = 0;
			do {
				r = is.read();
			} while (r != -1);
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
