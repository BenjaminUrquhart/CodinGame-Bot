package net.benjaminurquhart.codinbot.api.entities;

import org.json.JSONObject;

public class UserProfile {

	private JSONObject json;
	
	public UserProfile(JSONObject json) {
		this.json = json;
	}
	
	public JSONObject getJSON() {return json;}
}
