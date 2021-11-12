package com.scienceman;

public class YouTubeObject {
	private String channelName, channelId, playlistId;
	
	public YouTubeObject(String channelName, String channelId, String playlistId) {
		this.channelName = channelName;
		this.channelId = channelId;
		this.playlistId = playlistId;
	}
	@Override
	public String toString() {
		return channelName + ", " + channelId + ", " + playlistId;
	}
}
