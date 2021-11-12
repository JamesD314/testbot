package com.scienceman;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;

import net.dv8tion.jda.internal.JDAImpl;

public class YouTubeConnector {
	
	private static String API_KEY;
	private static YouTube youTube;
	
	public static void setup(String name, String apiKey) throws GeneralSecurityException, IOException {
		API_KEY = apiKey;
		youTube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
				.setApplicationName(name).build();
	}
	
	/**
	 * 
	 * @param channelName The name of the YouTube Channel
	 * @return The id of the uploads playlist of the given channel, null if error or invalid channel
	 */
	public static YouTubeObject getYouTubeObject(String channelName) {
		ArrayList<String> list = new ArrayList<>();
		list.add("contentDetails");
        try {
        	// First attempt query using Channels to save quota
        	YouTube.Channels.List request = youTube.channels().list(list);
        	ChannelListResponse response = request.setKey(API_KEY).setForUsername(channelName)
        			.setFields("items/id,items/contentDetails/relatedPlaylists/uploads").execute();
        	if(response.getItems().size() == 1) {
        		return new YouTubeObject(channelName, response.getItems().get(0).getId(),
        				response.getItems().get(0).getContentDetails().getRelatedPlaylists().get("uploads").toString());
        	}
        	else {
        		// Attempts query using search if prior failed
            	list.set(0, "snippet");
            	YouTube.Search.List request2 = youTube.search().list(list);
            	list.set(0, "channel");
            	SearchListResponse response2 = request2.setQ(channelName).setType(list).execute();
            	if(response2.getItems().size() > 0) {
            		
            	}
        	}
        	
        } catch (Exception e) {
        	JDAImpl.LOG.error("Error occurred when querying YouTube Data API", e);
        }
        
        return ytObject;
	}
	
	public static ArrayList<String> getNewVideos(String playlistId) {
		ArrayList<String> videos = new ArrayList<>();
		return videos;
	}
}
