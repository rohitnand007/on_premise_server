package com.edutor.cacheserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

@Path("/downloader/{url:(.*?)}")
public class Downloader {

	private final String FINAL_NAME_HEADER = "filename=";

	// public static String EDUTOR_STUDENT_ID = null; // TODO addign the student
	// id in each userrequest call
	// public static String EDUTOR_STUDENT_PASSWORD = null;
	// public String mUserRequestUrl = "";

	// NOTE: not requested by edutor platform right now
	@HEAD
	public Response header(@PathParam("url") final String requestUrl,
			@Context ServletContext sc,
			@HeaderParam("user_id") String edutor_user_id) {
		// Accept-Ranges: bytes
		// Content-Type: image/png
		CdnApplicationLogger.debug(edutor_user_id, requestUrl, "HEAD:- header");
		try {
			String localPath = getLocalFilePathFromURL(requestUrl, sc,
					edutor_user_id);
			File asset = new File(localPath);
			return Response
					.ok()
					.header(HttpHeaders.CONTENT_TYPE,
							MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_LENGTH, asset.length())
					.header("Accept-Ranges", "bytes")
					.header("content-disposition",
							"attachment; " + FINAL_NAME_HEADER + "\""
									+ asset.getName() + "\"").build();

		} catch (URISyntaxException e) {
			e.printStackTrace();

			return Response.status(404).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(404).build();
		}

	}

	private Response redirectToPortal(String url, String edutor_user_id)
			throws URISyntaxException {
		CdnApplicationLogger.info(edutor_user_id, url, "redirecting");
		URI uri = getPortalURI(url);
		// return Response.ok("Redirecting to portal").build();
		return Response.temporaryRedirect(uri).build();
	}

	private URI getPortalURI(String url) throws URISyntaxException {
		if (url.startsWith("http")) {
			return new URI(url.replace("http:", "https"));
		}else if (url.startsWith("https:")) {
			return new URI(url);
		}
		URI uri = new URI(NetworkUtils.PORTAL_URL + url);
		return uri;
	}

	@GET
	public Response getServeFile(@PathParam("url") final String requestUrl,
			@Context ServletContext sc, @HeaderParam("Range") String range,
			@QueryParam("edutor_id") String edutor_user_id) {

	    CdnApplicationLogger.info(edutor_user_id, requestUrl,
				"GET:- getServeFile");
		if (requestUrl.equals("") || requestUrl.contains("favicon")) {
			return null;
		}
		try {
			schedulePostLogJob(sc, ReportType.LOCAL_SERVE_FILE_REQUESTED, null,
					null, edutor_user_id, requestUrl);
			return serveFile(requestUrl, sc, range, edutor_user_id);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof FileNotFoundException) {
				schedulePostLogJob(sc, ReportType.LOCAL_FILE_PATH_NOT_FOUND, e,
						null, edutor_user_id, requestUrl);
			} else {
				schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_FAILED, e,
						null, edutor_user_id, requestUrl);
			}
			try {
				schedulePostLogJob(sc, ReportType.REDIRECTING_TO_PORTAL, null,
						null, edutor_user_id, requestUrl);
				return redirectToPortal(requestUrl, edutor_user_id);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				schedulePostLogJob(sc, ReportType.REDIRECT_FAILED, e1, null,
						edutor_user_id, requestUrl);
			}
		}
		return null;
	}

	@POST
	public Response postServeFile(@PathParam("url") final String requestUrl,
			@Context ServletContext sc, @HeaderParam("Range") String range,
			@HeaderParam("user_id") String edutor_user_id) {

		try {
			return serveFile(requestUrl, sc, range, edutor_user_id);
		} catch (Exception e) {
			e.printStackTrace();
			schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_FAILED, e, null,
					edutor_user_id, requestUrl);
			try {
				return redirectToPortal(requestUrl, edutor_user_id);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
				schedulePostLogJob(sc, ReportType.REDIRECT_FAILED, e1, null,
						edutor_user_id, requestUrl);
			}
		}
		return null;
	}

	@DELETE
	public Response deleteServeFile(@PathParam("url") final String requestUrl,
			@Context ServletContext sc, @HeaderParam("Range") String range,
			@HeaderParam("user_id") String edutor_user_id) {
		try {
			return serveFile(requestUrl, sc, range, edutor_user_id);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO send error log to portal
			schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_FAILED, e, null,
					edutor_user_id, requestUrl);
			try {
				return redirectToPortal(requestUrl, edutor_user_id);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				schedulePostLogJob(sc, ReportType.REDIRECT_FAILED, e1, null,
						edutor_user_id, requestUrl);
			}
		}
		return null;
	}

	@PUT
	public Response putServeFile(@PathParam("url") final String requestUrl,
			@Context ServletContext sc, @HeaderParam("Range") String range,
			@HeaderParam("user_id") String edutor_user_id) {

		try {
			return serveFile(requestUrl, sc, range, edutor_user_id);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO send error log to portal
			schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_FAILED, e, null,
					edutor_user_id, requestUrl);
			try {
				return redirectToPortal(requestUrl, edutor_user_id);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				schedulePostLogJob(sc, ReportType.REDIRECT_FAILED, e1, null,
						edutor_user_id, requestUrl);

			}
		}
		return null;
	}

	private Response serveFile(String url, ServletContext sc, String range,
			String userId) throws URISyntaxException, IOException {
		// TODO First check in database for url then connect to portal to fetch
		// metadata
		String localPath = getLocalFilePathFromURL(url, sc, userId);
		if (localPath == null) {
			throw new FileNotFoundException();
			// downloadFileFromPortal(url, AUTO_DOWNLOADS_DIR); PHASE 2
		}
		System.out.println(" build stream");
		return buildStream(sc,new File(localPath), range, userId, url);

	}

	private String getFileNameFromHeaders(HttpResponse httpConn) {
		System.out
				.println("---------------------------- get file name from headers");
		// extracts file name from header field
		Header dispositionHeader = httpConn
				.getFirstHeader("Content-Disposition");
		String disposition = null;

		if (dispositionHeader != null)
			disposition = dispositionHeader.getValue();
		else
			return null;
		System.out.println(disposition);
		int index = disposition.indexOf("filename=\"");
		System.out.println(index);

		String fileName = null;
		if (index > 0) {
			fileName = disposition.substring(index + 10,
					disposition.length() - 1);
		} else if ((index = disposition.indexOf("filename =\"")) > 0) {
			fileName = disposition.substring(index + 11,
					disposition.length() - 1);
		}
		System.out.println(index);
		System.out.println(fileName);
		return fileName;
	}

	private boolean insertRecordInToFilesTable(String url,
			ServletContext pServeletContext, String filename, String filepath,
			String edutor_user_id) {
		// TODO : save url,filename,localFilePath,file_md5,url_md5,date_created
		// etc...
		DBConnectionManager dbConnectionManager = (DBConnectionManager) pServeletContext.getAttribute(AppContextListener.DB_CONNECTION_MANAGER);
		PreparedStatement ps = null;

		try {
			Connection con = dbConnectionManager.getConnection();
			ps = con.prepareStatement("insert into files(url,filename,relativefilepath,file_md5,url_md5) values (?,?,?,?,?)");
			ps.setString(1, url);
			ps.setString(2, filename);
			ps.setString(3, filepath);
			ps.setString(
					4,
					getMd5String(getMd5File(AppContextListener.MANUAL_DOWNLOADS_DIR
							+ File.separator + filepath)));
			ps.setString(5, getMd5String(getMd5(url)));
			ps.execute();
			schedulePostLogJob(pServeletContext,
					ReportType.LOCAL_FILE_DATA_INSERT_IN_DB, null,
					null, edutor_user_id, url);
			return true;
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			schedulePostLogJob(pServeletContext,
					ReportType.LOCAL_SQL_INSERT_FAILED, e, null,
					edutor_user_id, url);
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			CdnApplicationLogger.error(edutor_user_id,
					"Database connection problem", e);
			schedulePostLogJob(pServeletContext,
					ReportType.LOCAL_SQL_INSERT_FAILED, e, null,
					edutor_user_id, url);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			CdnApplicationLogger.error(edutor_user_id,
					"Database problem - generic issue", e);
			schedulePostLogJob(pServeletContext,
					ReportType.LOCAL_SQL_INSERT_FAILED, e, null,
					edutor_user_id, url);
			return false;
		} finally {
			DBConnectionManager.close(null, ps);
		}
	}

	private byte[] getMd5File(String filepath) throws NoSuchAlgorithmException,
			IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		DigestInputStream dis = null;

		try (InputStream is = Files.newInputStream(Paths.get(filepath))) {
			dis = new DigestInputStream(is, md);
		}

		if (dis != null)
			dis.close();

		byte[] digest = md.digest();
		return digest;
	}

	private byte[] getMd5(String url) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md;
		byte[] bytesOfMessage;
		md = MessageDigest.getInstance("MD5");
		bytesOfMessage = url.getBytes("UTF-8");
		return md.digest(bytesOfMessage);
	}

	private static String getMd5String(byte[] mdbytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			String hex = Integer.toHexString(0xff & mdbytes[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private String getLocalFilePathFromURL(final String requestUrl,
			ServletContext sc, String edutor_user_id)
			throws URISyntaxException, IOException {
		/*
		 * check URL in db if exist return localpath else check for localfile in
		 * MANUAL_DOWNLOADS_DIR if filefound - insert in db return path else
		 * return null
		 */
		String localPath = getFilePathFromDb(requestUrl, sc, edutor_user_id,
				requestUrl);
		if (localPath != null) {
			schedulePostLogJob(sc, ReportType.LOCAL_FILE_FOUND_IN_DB, null, null,
					edutor_user_id, requestUrl);
			return localPath;
		}else {
			schedulePostLogJob(sc, ReportType.LOCAL_FILE_NOT_FOUND_IN_DB, null, null,
					edutor_user_id, requestUrl);
		}
		String cdnLoginId = sc.getInitParameter("edutor_cdn_user_id");
		String cdnPassword = sc.getInitParameter("edutor_cdn_password");
		String cdnDeviceId = sc.getInitParameter("edutor_cdn_id");
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("user[edutorid]",cdnLoginId));
		params.add(new BasicNameValuePair("user[password]", cdnPassword));
		params.add(new BasicNameValuePair("user[deviceid]",cdnDeviceId));
		BasicHttpContext localContext = new BasicHttpContext();
		HttpHost targetHost = new HttpHost(NetworkUtils.PORTAL_HOST_NAME, 80, "http");
		DefaultHttpClient httpclient = new DefaultHttpClient();
		// /users/session.json
		Result signInResult = NetworkUtils.getSignInResponse(targetHost, httpclient, localContext, params, "/users/session.json");
		
		if (!signInResult.isSuccess) {
			schedulePostLogJob(sc, ReportType.CDN_SIGNIN_FAILED, null, null,
					edutor_user_id, requestUrl);
			return null;
		}
		
		try {
			URI uri = getPortalHttpURI(requestUrl);
			HttpGet httpGet = new HttpGet(uri);
			httpGet.addHeader("device_id", cdnDeviceId);
			HttpResponse httpResponse = httpclient.execute(httpGet);	
			CdnApplicationLogger.debug(edutor_user_id, requestUrl,
					"  httpResponse  " + httpResponse);
			int statusCode = 0;
			if (httpResponse != null) {
				StatusLine statusLine = httpResponse.getStatusLine();
				if (statusLine != null)
					statusCode = statusLine.getStatusCode();
			}

			CdnApplicationLogger.debug(edutor_user_id, requestUrl,
					"status code " + statusCode);

			if (statusCode == HttpURLConnection.HTTP_OK) {
				String fileNameFromHeaders = getFileNameFromHeaders(httpResponse);
				CdnApplicationLogger.debug(edutor_user_id, requestUrl,
						"  fileNameFromHeaders  " + fileNameFromHeaders);

				if (fileNameFromHeaders == null) {
					CdnApplicationLogger.debug(edutor_user_id, requestUrl,
							"fileNameFromHeaders is null");
					schedulePostLogJob(sc,
							ReportType.LOCAL_FILE_NAME_FROM_HEADERS_NULL, null,
							null, edutor_user_id, requestUrl);

					return null;
				}

				File localFile = new File(
						AppContextListener.MANUAL_DOWNLOADS_DIR
								+ File.separator + fileNameFromHeaders);
				String relativePath = fileNameFromHeaders;
				System.out.println("file exists -"+localFile.exists() );
				if (localFile.exists()) {
					schedulePostLogJob(sc,
							ReportType.LOCAL_FILE_NAME_FROM_HEADERS+"="+fileNameFromHeaders, null,
							null, edutor_user_id, requestUrl);
					schedulePostLogJob(sc, ReportType.LOCAL_FILE_FOUND,
							null, null, edutor_user_id, requestUrl);
					boolean status = insertRecordInToFilesTable(
								requestUrl, sc,
								fileNameFromHeaders, relativePath, edutor_user_id);
					return localFile.getAbsolutePath();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CdnApplicationLogger.info(edutor_user_id, requestUrl,
					e.getMessage());
			schedulePostLogJob(sc, ReportType.AUTHENTICATION_FAILED, e, null,
					edutor_user_id, requestUrl);
		}
		return null;
	}

	private URI getPortalHttpURI(String url) throws URISyntaxException {

		if (url.startsWith("http:")) {
			return new URI(url);
		}else if (url.startsWith("https:")) {
			return new URI(url.replace("http:", "https:"));
		}
		URI uri = new URI(NetworkUtils.PORTAL_HTTP_URL + url);
		return uri;
	}

	private String getFilePathFromDb(String url, ServletContext sc,
			String edutor_user_id, String pUserRequestUrl) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String query = "select relativefilepath from files where url_md5 = ? ;";
			DBConnectionManager dbConnectionManager = (DBConnectionManager) sc.getAttribute(AppContextListener.DB_CONNECTION_MANAGER); 
			Connection connection = dbConnectionManager.getConnection();
			ps = connection.prepareStatement(query);
			ps.setString(1, getMd5String(getMd5(url)));
			rs = ps.executeQuery();
			while (rs != null && rs.next()) {
				String path = AppContextListener.MANUAL_DOWNLOADS_DIR
						+ File.separator + rs.getString("relativefilepath");
				return path;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			CdnApplicationLogger
					.error(edutor_user_id, "Database connection", e);
			schedulePostLogJob(sc, ReportType.LOCAL_SQL_READ_FAILED+"-"+e.getMessage(), e, null,
					edutor_user_id, pUserRequestUrl);
		} catch (Exception e) {
			e.printStackTrace();
			CdnApplicationLogger
					.error(edutor_user_id, "Database connection", e);
			schedulePostLogJob(sc, ReportType.LOCAL_SQL_READ_FAILED, e, null,
					edutor_user_id, pUserRequestUrl);
		} finally {
			DBConnectionManager.close(rs, ps);
		}
		return null;
	}

	private Response buildStream(ServletContext sc,final File asset, final String range,
			String userID, String url) throws IOException {
		CdnApplicationLogger.info(userID, url,
				"buildStream " + asset.getAbsolutePath());
		// range not requested : Firefox, Opera, IE do not send range headers
		if (range == null) {
			System.out.println("Range is null");
			StreamingOutput streamer = new StreamingOutput() {
				@Override
				public void write(final OutputStream output)
						throws IOException, WebApplicationException {
					FileInputStream fileInputStream = new FileInputStream(asset);
					final FileChannel inputChannel = fileInputStream
							.getChannel();
					final WritableByteChannel outputChannel = Channels
							.newChannel(output);
					try {
						inputChannel.transferTo(0, inputChannel.size(),
								outputChannel);
					} finally {
						// closing the channels
						inputChannel.close();
						outputChannel.close();
						try {
							fileInputStream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_STARTED, null, null,
					userID, url);
			return Response
					.ok(streamer, MediaType.APPLICATION_OCTET_STREAM)
					.status(200)
					.header("content-disposition",
							"attachment; " + FINAL_NAME_HEADER + "\""
									+ asset.getName() + "\"")
					.header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
		}
		String[] ranges = range.split("=")[1].split("-");
		final int from = Integer.parseInt(ranges[0]);

		int to = 0;

		if (ranges.length < 2) {
			to = (int) (asset.length() - 1);
		} else if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		if (to >= asset.length()) {
			to = (int) (asset.length() - 1);
		}

		final String responseRange = String.format("bytes %d-%d/%d", from, to,
				asset.length());
		System.out.println("responseRange   " + responseRange);
		final RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);

		final int len = to - from + 1;
		final MediaStreamer streamer = new MediaStreamer(len, raf);
		Response.ResponseBuilder res = Response
				.ok(streamer)
				.status(206)
				.header("Accept-Ranges", "bytes")
				.header("Content-Range", responseRange)
				.header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
				.header(HttpHeaders.LAST_MODIFIED,
						new Date(asset.lastModified()));
		schedulePostLogJob(sc, ReportType.LOCAL_DOWNLOAD_STARTED, null, null,
				userID, url);
		return res.build();
	}

	// TODO add logging params here
	public void schedulePostLogJob(ServletContext pServeletContext,
			String ReportType, Exception exception, Object pOtherData,
			String edutor_user_id, String pUserRequestUrl) {
		ScheduledExecutorService jobscheduler = (ScheduledExecutorService) pServeletContext
				.getAttribute("JobScheduler");
		System.out.println("scheduling");
		if (jobscheduler == null)
			return; // log and return
		System.out.println("scheduling   2");
		jobscheduler.schedule(new PostLogToEdutorPortal(pServeletContext,
				ReportType, exception, edutor_user_id, pUserRequestUrl,
				pOtherData, null), 2, TimeUnit.SECONDS);
	}

}