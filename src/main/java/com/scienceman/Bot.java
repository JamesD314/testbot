package com.scienceman;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.JDAImpl;

public class Bot {

	private static Connection sqlConnection;
	
	public static void main(String[] args) {
		// Variable declaration
		Properties apiKeys = new Properties();
		JDABuilder jda;
	
		// Loads in API keys
		try {
			apiKeys.load(Bot.class.getClassLoader().getResourceAsStream(".apiconfig.properties"));
		} catch (IOException e) {
			JDAImpl.LOG.error("Unable to load file .apiconfig.properties", e);
			System.exit(1);
		}
		
		// Connects to MySQL database and creates necessary tables
		SQLConnector.connect();
		try {
			SQLConnector.update("CREATE TABLE IF NOT EXISTS `guilds`"
					+ "(`guildId` BIGINT UNSIGNED NOT NULL,"
					+ " `prefix` VARCHAR(1) NULL,"
					+ "`channel` BIGINT UNSIGNED NULL,"
					+ "PRIMARY KEY (`guildId`),"
					+ "UNIQUE INDEX `id_UNIQUE`"
					+ "(`guildId` ASC) VISIBLE)"
					+ "DEFAULT CHARACTER SET = utf8;");
			SQLConnector.update("CREATE TABLE IF NOT EXISTS `channels`"
					+ "(`channelId` VARCHAR(45) NOT NULL,"
					+ "`playlistId` VARCHAR(45) NULL,"
					+ "PRIMARY KEY (`channelId`),"
					+ "UNIQUE INDEX `channelId_UNIQUE`"
					+ "(`channelId` ASC) VISIBLE)"
					+ "DEFAULT CHARACTER SET = utf8;");
		} catch (SQLException e) {
			JDAImpl.LOG.error("Unable to access or create necessary tables in MySQL database.", e);
			System.exit(1);
		}
		
		// Sets up YouTube API
		try {
			YouTubeConnector.setup("testbot", apiKeys.getProperty("youtube-data.key"));
		} catch (GeneralSecurityException | IOException e) {
			JDAImpl.LOG.error("Unable to connect to the YouTube Data API v3", e);
			System.exit(1);
		}
		
		// Connects to Discord account
		try {
			jda = JDABuilder.createDefault(apiKeys.getProperty("jda.key"));
			jda.setActivity(Activity.watching("!info"));
			jda.setStatus(OnlineStatus.ONLINE);
			jda.addEventListeners(new Commands());
			jda.build();
		} catch (LoginException e) {
			JDAImpl.LOG.error("Unable to log in to Discord account.", e);
			System.exit(1);
		}
	}
	
	public static Connection getSQLConnection() {
		return sqlConnection;
	}
}