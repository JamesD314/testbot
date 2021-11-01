package com.scienceman;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.dv8tion.jda.internal.JDAImpl;

public class SQLConnector {
	private static Connection sqlConnection;
	private static Statement stmt;
	
	// Change this to the default error handler
	private static void logError(String msg, Exception e) {
		JDAImpl.LOG.error(msg, e);
	}
	
	public static void connect() {
		Properties dbConfig;
		dbConfig = new Properties();
		try {
			dbConfig.load(SQLConnector.class.getClassLoader().getResourceAsStream(".dbconfig.properties"));
			sqlConnection = DriverManager.getConnection(
					dbConfig.getProperty("db.url"), dbConfig.getProperty("db.username"),
					dbConfig.getProperty("db.password"));
			stmt = sqlConnection.createStatement();
		} catch (IOException e) {
			logError("Unable to load file .dbconfig.properties.", e);
			System.exit(1);
		} catch (SQLException e) {
			logError("Unable to connect to MySQL server.", e);
			System.exit(1);
		}
	}
	
	public static ResultSet query(String q) throws SQLException {
		ResultSet result = null;
		result = stmt.executeQuery(q);
		return result;
	}
	
	public static void update(String q) throws SQLException {
		stmt.executeUpdate(q);
	}
}