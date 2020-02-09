package com.edutor.cacheserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.List;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.edutor.cacheserver.Result.ServerError;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class NetworkUtils {
	
//	public static final String PRODUCION_PORTAL_URL = "https://portal.myedutor.com/";
	public static final String PRODUCION_PORTAL_URL = "https://portal.ignitorlearning.com/";
//	public static final String PRODUCION_PORTAL_URL = "http://abcde.fortunapix.com/";

	
	
	public static final String TEST_PORTAL_URL = "https://88.80.185.134/";

	//public static final String PRODUCION_PORTAL_HOST_NAME = "portal.myedutor.com";
	public static final String PRODUCION_PORTAL_HOST_NAME = "portal.ignitorlearning.com";
//	public static final String PRODUCION_PORTAL_HOST_NAME = "abcde.fortunapix.com";

	
	
	public static final String TEST_PORTAL_HOST_NAME = "88.80.185.134";


	public static final String PORTAL_URL = PRODUCION_PORTAL_URL;
	public static final String PORTAL_HOST_NAME = PRODUCION_PORTAL_HOST_NAME;
	
	public static final String PORTAL_HTTP_URL =  PORTAL_URL.replace("https:", "http:");
	public static final String PING_URL = PORTAL_HTTP_URL+"ping_server.json";
	public static final String CDN_METADATA_URL = PORTAL_HTTP_URL+"send_cdn_metadata.json";
	public static final String CDN_CACHE_STATUS_URL = PORTAL_HTTP_URL+"send_cdn_cache_status.json";
	public static final String CDN_REPORT_URL = PORTAL_HTTP_URL+"send_cdn_logs.json";



	public static Result getSignInResponse(HttpHost targetHost, HttpClient httpclient, HttpContext localContext,
			List<BasicNameValuePair> params,
			String loginRestUrl) throws IOException, UnsupportedEncodingException,
			ClientProtocolException {
		try {
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
			HttpPost httppost = new HttpPost(loginRestUrl);
			httppost.setEntity(formEntity);

			HttpResponse postResponse = null;

			try {
				postResponse = httpclient.execute(targetHost, httppost, localContext);
			}
			catch (SocketTimeoutException e) {
				e.printStackTrace();
			}
			catch (ConnectionClosedException e) {
				e.printStackTrace();
			}
			catch (ClientProtocolException cpe) {
				cpe.printStackTrace();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			HttpEntity entity = postResponse.getEntity();
			String postResString = null;
			try {
				postResString = EntityUtils.toString(entity);
			}
			catch (ParseException e1) {
				e1.printStackTrace();
				return new Result(false, ServerError.COMMUNICATION_EXCEPTION + " " + e1.getMessage());
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				return new Result(false, ServerError.IO_EXCEPTION + " " + ioe.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
				return new Result(false, ServerError.COMMUNICATION_EXCEPTION + " " + e.getMessage());
			}

			try {
				postResponse.getEntity().consumeContent();
			}
			catch (IOException e) {
				e.printStackTrace();
				return new Result(false, ServerError.COMMUNICATION_EXCEPTION + " " + e.getMessage());
			}
			catch (Exception e1) {
				e1.printStackTrace();
				e1.printStackTrace();
			}

			if (postResString != null) {
				JsonObject signInResponse = null;
				try {
					System.out.println("postResString " + postResString);
					JsonElement jelement = new JsonParser().parse(postResString);
					signInResponse = jelement.getAsJsonObject();
					//signInResponse = (JsonObject)new JsonParser().parse(postResString);
				}
				catch (JsonParseException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (signInResponse == null)
					return new Result(false, ServerError.NO_RESPONSE);
				try {
					System.out.println("RESPONSE  " +  "postResString " + postResString);
					String signIn = "sign_in";
						String sign_in = signInResponse.get(signIn).toString();
						if (!sign_in.equals("true"))
							return new Result(	false, ServerError.INVALID_USER);
						else
							return new Result(true, postResString);
				}
				catch (JsonParseException e) {
					e.printStackTrace();
					return new Result(false, ServerError.INVALID_RESPONSE + " " + e.getMessage());
				}
			} else
				return new Result(false, ServerError.NO_RESPONSE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new Result(false, ServerError.COMMUNICATION_EXCEPTION);
	}

	
	public static void sendPost(String URL, List<NameValuePair> nameValuePairs) {
		HttpClient client = new DefaultHttpClient();
	    HttpPost post = new HttpPost(URL);
	    try {
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        HttpResponse httpResponse = client.execute(post);
	        System.out.println("sendPostResponse-"+URL);
	        System.out.println("sendPostResponse-"+httpResponse.getStatusLine().toString());
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.out.println("sendPosterror-"+URL);
	        System.out.println("sendPosterror-"+e.getMessage());

	     }
	}

	/*public static String executePostDataResponse(HttpHost targetHost, HttpClient httpclient, HttpContext localContext,
			Object pClass, String restUrl, String pDeviceID) {

		HttpResponse postResponse = null;
		try {
			HttpPost httpPost = new HttpPost(restUrl);

			String gsonString = pClass.toString();
			System.out.println(" Posting to web" + gsonString);
			StringEntity input = new StringEntity(gsonString);
			input.setContentType("application/json");
			httpPost.addHeader("Content-type", "application/json");
			httpPost.addHeader("device_id", pDeviceID);
			httpPost.setEntity(input);

			postResponse = httpclient.execute(targetHost, httpPost, localContext);
			HttpEntity entity = postResponse.getEntity();
			String postResString = EntityUtils.toString(entity);
			System.out.println("RESPONSE POST " + postResString);
			return postResString;
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static HttpClient getClient(String EDUTOR_ID, String EDUTOR_PASSWORD) {
		HttpClient httpclient;
		httpclient = new DefaultHttpClient();
		ClientConnectionManager mgr = httpclient.getConnectionManager();
		HttpParams params = httpclient.getParams();
		httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
		HttpParams httpParams = httpclient.getParams();
		httpParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		HttpConnectionParams.setConnectionTimeout(httpParams, 40000);
		HttpConnectionParams.setSoTimeout(httpParams, 40000);

		((DefaultHttpClient) httpclient).getCredentialsProvider()
				.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(EDUTOR_ID, EDUTOR_PASSWORD));
		((DefaultHttpClient) httpclient).addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				AuthState state = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
				if (state.getAuthScheme() == null) {
					BasicScheme scheme = new BasicScheme();
					CredentialsProvider credentialsProvider = (CredentialsProvider) context
							.getAttribute(ClientContext.CREDS_PROVIDER);
					Credentials credentials = credentialsProvider.getCredentials(AuthScope.ANY);
					if (credentials == null) {
						throw new HttpException();
					}
					state.setAuthScope(AuthScope.ANY);
					state.setAuthScheme(scheme);
					state.setCredentials(credentials);
				}
			}
		}, 0);

		return httpclient;
	}
	*/
}
