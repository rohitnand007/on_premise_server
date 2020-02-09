package com.edutor.cacheserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnectionManager {

	private static Connection connection;
	private static String dbURL, user, pwd;
	private static DBConnectionManager dbConnectionManager;
	
	private DBConnectionManager(String mysqlURL, String dbURL, String user,
			String pwd) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		createDB(mysqlURL, user, pwd);
		createTable(dbURL, user, pwd);
		this.dbURL = dbURL;
		this.user = user;
		this.pwd = pwd;
		this.connection = DriverManager.getConnection(dbURL, user, pwd);
		dbConnectionManager = this;	
	}
	
	public static DBConnectionManager createInstance(String mysqlURL, String dbURL, String user,
			String pwd) throws ClassNotFoundException, SQLException{
		if (dbConnectionManager==null) {
			dbConnectionManager = new DBConnectionManager(mysqlURL,dbURL,user,pwd);
		}
		return dbConnectionManager;
	}

	private void createDB(String mysqlURL, String user, String pwd)
			throws SQLException {
		Connection connection = DriverManager
				.getConnection(mysqlURL, user, pwd);
		String createSql = "CREATE DATABASE IF NOT EXISTS edutorcacheserver";
		PreparedStatement ps = connection.prepareStatement(createSql);
		ps.execute();
		ps.close();
	}

	private void createTable(String dbUrl, String user, String pwd)
			throws SQLException {
		Connection connection = DriverManager.getConnection(dbUrl, user, pwd);
		String createSql = "CREATE TABLE IF NOT EXISTS `files` ( `url` Text NOT NULL,"
				+ " `filename` varchar(255) NOT NULL,"
				+ " `relativefilepath` varchar(1000) NOT NULL,"
				+ " `file_md5` varchar(255) NOT NULL, "
				+ "`url_md5` varchar(255), "
				+ "`is_sync` char(1) DEFAULT 'n', "
				+ "`date_created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "PRIMARY KEY (url_md5) );";
		PreparedStatement ps = connection.prepareStatement(createSql);
		ps.execute();
		ps.close();
	}

	public Connection getConnection() throws SQLException{
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}else {
				connection = DriverManager.getConnection(dbURL, user, pwd);
				return connection;
			}
		} catch (Exception e) {
			e.printStackTrace();
			connection = DriverManager.getConnection(dbURL, user, pwd);
		}
		return connection;
	}

	public static void close(ResultSet rs, PreparedStatement ps){

		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
		} catch (SQLException e) {
			CdnApplicationLogger.error(AppContextListener.CDN_DEVICE_ID,
					"The result set cannot be closed.", e);
		}
		try {
			if (ps != null && !ps.isClosed()) {
				ps.close();
			}
		} catch (SQLException e) {
			CdnApplicationLogger.error(AppContextListener.CDN_DEVICE_ID,
					"The statement cannot be closed.", e);
		}
	}
}