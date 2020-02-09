package com.edutor.cacheserver;


public class Result {

	public boolean	isSuccess	= false;
	public String	message		= "Couldn't contact server";
	public String	desc;

	public Result() {

	}

	public Result(boolean isSuccess, String errorMsg, String errorDesc) {
		this.isSuccess = isSuccess;
		this.message = errorMsg;
		this.desc = errorDesc;
	}

	public Result(boolean isSuccess, String errorMsg) {
		this.isSuccess = isSuccess;
		this.desc = errorMsg;
		this.message = "Couldn't contact server";
	}

	public class NetworkError {
		public static final String	NO_NETWORK	= "Network unvailable";
	}

	public class ServerError {
		public static final String	NO_RESPONSE				= "Server not responding";
		public static final String	INVALID_RESPONSE		= "Server invalid response";
		public static final String	PROTOCOL_EXCEPTION		= "Client protocol Exception";
		// public static final String INVALID_RESPONSE =
// "Server invalid response";
		public static final String	SOCKET_TIMEOUT			= "Socket time out";
		public static final String	CONNECTION_CLOSED		= "Connection closed";
		public static final String	IO_EXCEPTION			= "Communication error - IO";
		public static final String	COMMUNICATION_EXCEPTION	= "Communication error - G";
		public static final String	INVALID_USER			= "Invalid user";
	}
	
	/*private Response oldCode(String url, String range, String filePath,
	File servingFile) {
if(range == null || range.trim().isEmpty()){
	
}

System.out.println("-----serving file--------");
System.out.println("filePath "+ filePath);
if(!servingFile.exists()){
	try {
		return redirectToPortal(url);
	} catch (URISyntaxException e) {
		e.printStackTrace();
	}
}

FileInputStream imageStream = null;
try {
	imageStream = new FileInp PreparedStatement ps = null;
 try {
     ps = con.prepareStatement("insert into files(name,email,country, password) values (?,?,?,?)");
     ps.setString(1, "");
     ps.execute();
     return true;
 } catch (SQLException e) {
     e.printStackTrace();
     logger.error("Database connection problem");
     return false;
 } catch(Exception e) {
	 e.printStackTrace();
	 return false;
 } finally{
     try {
         ps.close();
     } catch (SQLException e) {
         logger.error("SQLException in closing PreparedStatement");
     }
 }utStream(servingFile);
} catch (FileNotFoundException e) {
	System.out.println("Exception ");
	e.printStackTrace();
	try {
		return redirectToPortal(url);
	} catch (URISyntaxException e1) {
		e1.printStackTrace();
	}
}

System.out.println("sending response");

return Response
		.ok(imageStream, MediaType.APPLICATION_OCTET_STREAM)
		.header("content-disposition","attachment; "+FINAL_NAME_HEADER+"\""+servingFile.getName()+"\"")
		.build();
}
*/
	
	
	/*private void downloadFileFromPortal(String url, String saveDir)
	throws URISyntaxException, IOException {
URL link = new URL(getPortalURI(url).toString());
HttpURLConnection httpConn = (HttpURLConnection) link.openConnection();
int responseCode = httpConn.getResponseCode();
// check if file or redirect url
 if file - download file - insert url,filename,localFilePath,md5..else
 * - downloadFileFromPortal
 
if (responseCode == HttpURLConnection.HTTP_OK) {
	String redirectURL = httpConn.getHeaderField("Location");
	if (redirectURL != null && !(redirectURL.trim() == "")) {
		downloadFileFromPortal(redirectURL, saveDir);
	}
	String fileName = null; //getFileNameFromHeaders(httpConn);
	if (fileName != null) {
		InputStream inputStream = httpConn.getInputStream();
		String saveFilePath = saveDir + File.separator + fileName;
		FileOutputStream outputStream = new FileOutputStream(
				saveFilePath);
		int bytesRead = -1;
		byte[] buffer = new byte[4096];
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
		inputStream.close();
		System.out.println("download complete - " + fileName);
		insertRecord();
	}
}
httpConn.disconnect();
}*/
}
