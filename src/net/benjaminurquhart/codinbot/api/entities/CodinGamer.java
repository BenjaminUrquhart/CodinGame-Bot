package net.benjaminurquhart.codinbot.api.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Route;

public class CodinGamer {

	private String name,handle;
	private long imageID;
	
	private UserProfile profile;
	
	
	public CodinGamer(JSONObject json) {
		this(json.getString("name"), String.valueOf(json.getLong("id")), String.valueOf(json.getLong("imageBinaryId")));
	}
	public CodinGamer(String name, String handle, String imageID) {
		this.imageID = Long.parseLong(imageID);
		this.handle = handle;
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public String getHandle() {
		return handle;
	}
	public String getImageUrl() {
		return imageID < 0 ? "https://chat.is-going-to-rickroll.me/i/xLtF1gGPkNOhxw.png" : "https://static.codingame.com/servlet/fileservlet?id="+imageID;
	}
	public UserProfile getProfile() {
		if(profile == null) {
			try {
				profile = new UserProfile(CodinGameAPI.API.getJSONObject(Route.GET_POINTS_BY_HANDLE, new JSONArray().put(handle)));
			}
			catch(Exception e) {
				throw new APIException(e);
			}
		}
		return profile;
	}
	public String toString() {
		return this.getClass().getSimpleName()+": "+name+" ("+handle+")";
	}
}
