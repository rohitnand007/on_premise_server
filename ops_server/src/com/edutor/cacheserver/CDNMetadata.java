package com.edutor.cacheserver;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class CDNMetadata {
	//Using shortnames for creating short jsons
	public String os;
	public String hw;
	public Map<String, ArrayList<byte[]>> lipm; //interface_to_localip_map;;
	public Map<String, ArrayList<String>> pipm;//interface_to_publicip_map;;
	public Map<String, Map<String, String>> logical_volume; //parition -- size -- freespace -- used space;
	public String cdnid;

	//public 
	public CDNMetadata(){
		this.cdnid = AppContextListener.CDN_DEVICE_ID;
		try {
			lipm = getNetworkInterfacesToLocalIP();
			pipm = getNetworkInterfacesToPublicIP();
			logical_volume = getLogicalVolumeProperties();
			getOsConfiguration();
			getHwConfiguration();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void getHwConfiguration() {
		StringBuilder sb = new StringBuilder();
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			sb.append("hostname : "+ip.getHostName());
		} catch (Exception e) {
		
		}
		sb.append("os:"+ System.getProperty("os.name")+",");
		sb.append("os.arch"+ System.getProperty("os.arch")+",");
		sb.append("os.version"+ System.getProperty("os.version")+",");
		
		sb.append(System.getenv("PROCESSOR_IDENTIFIER")+",");
		sb.append(System.getenv("PROCESSOR_ARCHITECTURE")+",");
		sb.append(System.getenv("PROCESSOR_ARCHITEW6432")+",");
		sb.append(System.getenv("NUMBER_OF_PROCESSORS")+",");

		
			
		/* Total number of processors or cores available to the JVM */
		sb.append("Available processors (cores): "
				+ Runtime.getRuntime().availableProcessors()+",");
		
		/* Total amount of free memory available to the JVM */
		sb.append("Free memory (bytes): "
				+ Runtime.getRuntime().freeMemory()+",");
		
		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		sb.append("Maximum memory (bytes): "
				+ (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory)+",");
		
		/* Total memory currently in use by the JVM */
		sb.append("Total memory (bytes): "
				+ Runtime.getRuntime().totalMemory()+",");
		hw = sb.toString();

	}

	public void getOsConfiguration() {
	        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
	 
	        Map<String, String> systemProperties = runtimeBean.getSystemProperties();
	        Set<String> keys = systemProperties.keySet();
	        StringBuilder sb = new StringBuilder();
	        for (String key : keys) {
	            String value = systemProperties.get(key);
	            sb.append("["+key+"] = "+value+",");
	        }
	        this.os = sb.toString();
	    }

	 
	private Map<String, ArrayList<byte[]>> getNetworkInterfacesToLocalIP() throws SocketException{
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		Map<String, ArrayList<byte[]>> map = new HashMap<String, ArrayList<byte[]>>();
        for (NetworkInterface netint : Collections.list(nets)){
        	ArrayList<byte[]> ipList = new ArrayList<byte[]>(); 
        	map.put(netint.getName(), ipList);
        	for (InetAddress inetAddress : Collections.list(netint.getInetAddresses())) {
        		ipList.add(inetAddress.getAddress());
            }
        	System.out.println(netint.getName()+" - done---------------------");
        }
		return map;
	}
	
	private Map<String, ArrayList<String>> getNetworkInterfacesToPublicIP() throws IOException {
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (String inetCard : lipm.keySet()) {
			ArrayList<String> publicIpList = new ArrayList<String>();
			map.put(inetCard, publicIpList);
			for (byte[] ip : lipm.get(inetCard)) {
				RequestConfig config = RequestConfig.custom().
						setLocalAddress(InetAddress.getByAddress(ip)).build();
				HttpGet httpGet = new HttpGet("http://ipinfo.io/json");
				httpGet.setConfig(config);
				CloseableHttpClient httpClient=HttpClients.createDefault();;
				try {
					CloseableHttpResponse response = httpClient.execute(httpGet);
					StatusLine sl = response.getStatusLine(); 
					if (sl!=null && sl.getStatusCode() == HttpURLConnection.HTTP_OK) {
						String publicIp = EntityUtils.toString(response.getEntity());
						publicIpList.add(publicIp);
					}
				} catch (Exception e) {
					System.out.println("config "+ InetAddress.getByAddress(ip));
					e.printStackTrace();
				}finally {
					httpClient.close();
				}
			}
			
		}
		return map;
	}
	
	private Map<String, Map<String, String>> getLogicalVolumeProperties() {
	    FileSystemView fsv = FileSystemView.getFileSystemView();
	    File[] roots = fsv.getRoots();
	    Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
	    for (File root : roots) {
	    	Map<String, String> partitionData = new HashMap<String, String>();
	    	partitionData.put("displayname", fsv.getSystemDisplayName(root));
	    	partitionData.put("totalspace", root.getTotalSpace()+"");
	    	partitionData.put("usablespace", root.getUsableSpace()+"");
	    	partitionData.put("freespace", root.getFreeSpace()+"");
	    	map.put(root+"", partitionData);
	    }
		return map;
	}
	
	 public void post(){
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String json= new Gson().toJson(this);
			System.out.println("CDNMetadata - post : "+json);
			params.add(new BasicNameValuePair("metadata", json));
	    	NetworkUtils.sendPost(NetworkUtils.CDN_METADATA_URL, params);
	    }
}
