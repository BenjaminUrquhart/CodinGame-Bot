package net.benjaminurquhart.codinbot.api.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Route;

public class CodinGamer {

	private String name,handle;
	private long imageID;
	private int id;
	
	private UserProfile profile;
	
	
	public CodinGamer(JSONObject json) {
		this(json.getString("name"), (json.has("userId") && json.get("userId") instanceof Number) ? json.getInt("userId") : -1, String.valueOf(json.has("handle") ? json.get("handle") : json.get("id")), json.optLong("imageBinaryId", -1));
	}
	public CodinGamer(String name, int id, String handle, long imageID) {
		this.imageID = imageID;
		this.handle = handle;
		this.name = name;
		this.id = id;
	}
	public int getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getHandle() {
		return handle;
	}
	public String getImageUrl() {
		return imageID < 0 ? null : "https://static.codingame.com/servlet/fileservlet?id="+imageID;
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
