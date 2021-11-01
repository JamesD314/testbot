package com.scienceman;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.JDAImpl;

public class Commands extends ListenerAdapter {
	private final char defaultPrefix = '!';
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		// Ignore all bots
		if(event.getAuthor().isBot()) {
			return;
		}
		
		// Variable declaration
		String[] args = event.getMessage().getContentRaw().split(" ");
		char prefix = defaultPrefix;
		long channel = 0;
		String qry  = "";
		ResultSet sqlResult;
		
		// Queries DB to see if server is registered
		try {
			qry = "SELECT * FROM guilds WHERE id=" + event.getGuild().getId();
			sqlResult = SQLConnector.query(qry);
			if(sqlResult != null && !sqlResult.next()) {
				// Registers server
				qry = "INSERT INTO guilds (id, prefix, channel) "
						+ "VALUES(" + event.getGuild().getId() + ", '" + prefix + "', null);";
				SQLConnector.update(qry);
			}
			else {
				// Loads server information
				prefix = sqlResult.getString("prefix").charAt(0);
				channel = sqlResult.getLong("channel");				
			}
		} catch(SQLException e) {
			JDAImpl.LOG.error("SQL Error on query " + qry, e);
		}
		
		// Removes prefix from message
		if(args[0].charAt(0) == prefix) {
			args[0] = args[0].substring(1);
		}
		// ignores message if prefix not present
		else {
			return;
		}
		
		// Sets the channel if not already present
		if(channel == 0) {
			try {
				SQLConnector.update("UPDATE guilds SET channel='" + event.getChannel().getId() + "' WHERE id=" + event.getGuild().getId());
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
		 * Command: prefix
		 * Description: sets the prefix for commands to args[1].charAt(0)
		 * Usage: !prefix [character]
		 */
		else if(args[0].equalsIgnoreCase("prefix")) {
			if(args.length == 2 && args[1].length() == 1 && !Character.isLetterOrDigit(args[1].charAt(0))) {
				try {
					SQLConnector.update("UPDATE guilds SET prefix='" + args[1].charAt(0) + "' WHERE id=" + event.getGuild().getId());
					msg(event, "Set prefix to " + args[1].charAt(0));
				} catch(SQLException e) {
					JDAImpl.LOG.error("SQL Error", e);
					msg(event, "Unable to set prefix due to internal error. Please try again later.");
				}
			}
			else {
				msg(event, prefix + "prefix must be followed by a single non-alphanumeric character.\nUsage: " + prefix + "prefix !");
			}			
		}
		/**
		 * Command: channel
		 * Description: sets the channel to post updates in
		 * Usage: !channel
		 */
		else if(args[0].equalsIgnoreCase("channel")) {
			if(args.length != 1) {
				msg(event, "Please type **!channel** in the channel you wish to set as the default for notifications.");
				return;
			}
			try {
				SQLConnector.update("UPDATE guilds SET channel='" + event.getChannel().getId() + "' WHERE id=" + event.getGuild().getId());
				msg(event, "Set channel to <#" + event.getChannel().getId() + ">");
			} catch(SQLException e) {
				JDAImpl.LOG.error("SQL Error", e);
				msg(event, "Unable to set channel due to internal error. Please try again later.");
			}
		}
	}
	
	private void msg(GuildMessageReceivedEvent event, String msg) {
		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessage(msg).queue();
	}

}