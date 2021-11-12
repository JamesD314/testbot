package com.scienceman;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.JDAImpl;

class Guild {
	long id, channel;
	char prefix;
	
	Guild(long id, char prefix){
		this.id = id;
		this.prefix = prefix;
	}
}

class YouTubeChannel {
	String name, playlist;
	
	YouTubeChannel(String name, String playlist){
		this.name = name;
		this.playlist = playlist;
	}
	
	YouTubeChannel(String name){
		this.name = name;
	}
}

public class Commands extends ListenerAdapter {
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		// Ignore all bots
		if(event.getAuthor().isBot()) {
			return;
		}
		
		// Variable declaration
		Guild guild = new Guild(event.getGuild().getIdLong(), '!');
		YouTubeChannel ytChannel;
		String[] args = event.getMessage().getContentRaw().split(" ");
		String qry  = "";
		ResultSet sqlResult;
		
		// Queries DB to see if server is registered
		try {
			qry = "SELECT * FROM guilds WHERE guildId=" + guild.id;
			sqlResult = SQLConnector.query(qry);
			if(!sqlResult.next()) {
				// Registers server
				qry = "INSERT INTO guilds (guildId, guild.prefix, channel) "
						+ "VALUES(" + guild.id + ", '" + guild.prefix + "', null);";
				SQLConnector.update(qry);
			}
			else {
				// Loads server information
				guild.prefix = sqlResult.getString("guild.prefix").charAt(0);
				guild.channel = sqlResult.getLong("channel");				
			}
		} catch(SQLException e) {
			JDAImpl.LOG.error("SQL Error on query " + qry, e);
		}
		
		// Removes guild.prefix from message
		if(args[0].charAt(0) == guild.prefix) {
			args[0] = args[0].substring(1);
		}
		// ignores message if guild.prefix not present
		else {
			return;
		}
		
		// Sets the channel if not already present
		if(guild.channel == 0) {
			guild.channel = event.getChannel().getIdLong();
			try {
				SQLConnector.update("UPDATE guilds SET channel='" + guild.channel + "' WHERE guildId=" + guild.id);
			} catch(SQLException e) {
				JDAImpl.LOG.error("SQL Error", e);
			}
		}
		
		/**
		 * Command: status
		 * Description: Messages if the bot is online
		 * Usage: !status
		 */
		if(args[0].equalsIgnoreCase("status")) {
			msg(event, "This bot is online!");
		}
		/**
		 * Command: guild.prefix
		 * Description: sets the guild.prefix for commands to args[1].charAt(0)
		 * Usage: !guild.prefix [character]
		 */
		else if(args[0].equalsIgnoreCase("prefix")) {
			if(args.length == 2 && args[1].length() == 1 && !Character.isLetterOrDigit(args[1].charAt(0))) {
				guild.prefix = args[1].charAt(0);
				try {
					SQLConnector.update("UPDATE guilds SET guild.prefix='" + guild.prefix + "' WHERE guildId=" + event.getGuild().getId());
					msg(event, "Set guild.prefix to " + guild.prefix);
				} catch(SQLException e) {
					JDAImpl.LOG.error("SQL Error", e);
					msg(event, "Unable to set prefix due to internal error. Please try again later.");
				}
			}
			else {
				msg(event, "**\\" + guild.prefix + "prefix** must be followed by a single non-alphanumeric character.\nUsage: " + guild.prefix + "guild.prefix !");
			}			
		}
		/**
		 * Command: channel
		 * Description: sets the channel to post updates in
		 * Usage: !channel
		 */
		else if(args[0].equalsIgnoreCase("channel")) {
			if(args.length == 1) {
				try {
					SQLConnector.update("UPDATE guilds SET channel='" + event.getChannel().getId() + "' WHERE guildId=" + event.getGuild().getId());
					msg(event, "Set channel to <#" + event.getChannel().getId() + ">");
				} catch(SQLException e) {
					JDAImpl.LOG.error("SQL Error", e);
					msg(event, "Unable to set channel due to internal error. Please try again later.");
				}
			}
			else {
				msg(event, "Please type **\\" + guild.prefix + "channel** in the channel you wish to set as the default for notifications.");
			}
		}
		/**
		 * Command add
		 */
		else if(args[0].equalsIgnoreCase("add")) {
			if(args.length == 2) {
				ytChannel = new YouTubeChannel(args[1]);
				// Check if channel is already registered in DB
				try {
					sqlResult = SQLConnector.preparedSelect("SELECT * FROM channels WHERE channelId=?", args[1]);
					if(sqlResult.next()) {
						// Sets the playlist id
						ytChannel.playlist = sqlResult.getString("playlistId");
					}
					else {
						// Queries the YouTube Data API to get playlist
						ytChannel.playlist = YouTubeConnector.getPlaylist(ytChannel.name);
						
						// Prints if not found
						if(ytChannel.playlist == null || ytChannel.playlist == "") {
							msg(event, "Unable to find YouTube channel with the name **" + ytChannel.name + "**.\n"
									+ "Please check your spelling and try again.");
							return;
						}
						
						//
					}
				} catch (SQLException e) {
					JDAImpl.LOG.error("SQL Error", e);
					msg(event, "Unable to add channel due to internal error. Please try again later");
				}
			}
			else {
				msg(event, "Please type **\\" + guild.prefix + "** followed by the YouTube channel you want to follow");
			}
		}
	}
	
	private void msg(GuildMessageReceivedEvent event, String msg) {
		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessage(msg).queue();
	}

}