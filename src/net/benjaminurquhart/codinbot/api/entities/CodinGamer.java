package net.benjaminurquhart.codinbot.api.entities;

import org.json.JSONArray;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.Route;

public class CodinGamer {

	private String name,handle,imageID;
	
	private UserProfile profile;
	
	public CodinGamer(String name, String handle, String imageID) {
		this.imageID = imageID;
		this.handle = handle;
		this.name = name;
		
		try {
			profile = new UserProfile(CodinGameAPI.getJSONObject(Route.GET_STATS, new JSONArray().put(handle)));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public String getName() {
		return name;
	}
	public String getHandle() {
		return handle;
	}
	public String getImageUrl() {
		return "https://static.codingame.com/servlet/fileservlet?id="+imageID;
	}
	public UserProfile getProfile() {
		return profile;
	}
	public String toString() {
		return this.getClass().getSimpleName()+": "+name+" ("+handle+")";
	}
}
