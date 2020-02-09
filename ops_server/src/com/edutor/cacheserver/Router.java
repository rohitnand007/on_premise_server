package com.edutor.cacheserver;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class Router implements Filter{

	private static final int Diskspoolbuffersizemb = 30;
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI();
		String topfolder = path.substring(1);
		String userID = AppContextListener.CDN_DEVICE_ID;
		
		CdnApplicationLogger.debug(userID,path,"requested dofilter");
		CdnApplicationLogger.debug(userID,path,"topfolder "+topfolder);
		
		Enumeration<String> headerNames = req.getHeaderNames();
		while(headerNames.hasMoreElements()){
			String each = headerNames.nextElement();
			CdnApplicationLogger.debug(userID,path,each +" -- "+req.getHeader(each));
		}
		if (topfolder.contains("/")) {
			topfolder = topfolder.substring(0, topfolder.indexOf("/"));
		}

		if (topfolder.equals("status")){
			request.getRequestDispatcher("/serverstatus").forward(
					request, response);
		} else {
			CdnApplicationLogger.debug(userID,path,"downloader "+ path);
			request.getContentLength();
			try {
				request.getRequestDispatcher("/downloader/"+path).forward(
						request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
//		initiateEhCache();
	}

	private void initiateEhCache() {
		CacheManager cacheManger = CacheManager.create();
		boolean overFlowToDisk = true;
		int max_elements_in_memory = 2000;
		boolean isEternal = false;
		long ttl = 0; //unlimited
		long timeToIdleSeconds = 0; // unlimited
		boolean diskPersistent = true;
		long diskExpiryThreadIntervalSeconds = 120; //seconds, NOTE: observe this, Unknown parameter
		
		
		int maxElementsonDisk = 300;
		int DiskspoolbuffersizeMb = 30;
		boolean clearOnFlush = true;
		
		RegisteredEventListeners registeryEventLlisteners = null;
		BootstrapCacheLoader bootstrapcacheLoader = null;
		
		Cache memCache = new Cache("EDUTOR_FILE_CACHE", max_elements_in_memory, MemoryStoreEvictionPolicy.LRU,
				overFlowToDisk,"disk_storage_path",isEternal ,ttl,timeToIdleSeconds ,diskPersistent, diskExpiryThreadIntervalSeconds,registeryEventLlisteners,bootstrapcacheLoader,maxElementsonDisk , Diskspoolbuffersizemb,clearOnFlush);
		
		CacheConfiguration cacheConfig = memCache.getCacheConfiguration();
		cacheConfig.setDiskPersistent(true);
		cacheConfig.setLogging(true);
		
		Element test = memCache.get("");
	}

}
