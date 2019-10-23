package net.benjaminurquhart.codinbot.api.entities;

import java.time.Instant;

import org.json.JSONObject;

public class Contest extends Puzzle {

	private Instant start, end;
	
	private String description;
	private String dateString;
	private String info;
	
	public Contest(JSONObject json) {
		super(json);
		super.expanded = true;
		
		JSONObject description = new JSONObject(json.getString("descriptionJson"));
		
		this.description = description.getString("challengeDescription");
		this.dateString = description.optString("challengeDate", null);
		this.info = description.getString("challengeInfo").replaceAll("<[^>]+>", "");
		
		this.start = Instant.ofEpochMilli(json.getLong("date"));
		this.end = Instant.ofEpochMilli(json.getLong("endApplicationsDate"));
	}
	@Override
	public String getDescription() {
		return description;
	}
	public String getInfo() {
		return info;
	}
	public String getDateString() {
		return dateString;
	}
	public Instant getStartTime() {
		return start;
	}
	public Instant getEndTime() {
		return end;
	}
	
	@Override
	public String toString() {
		return String.format("Contest: %s (ID: %s, Info: %s, Start: %s, End: %s)", this.getName(), this.getPrettyId(), this.getInfo(), this.getStartTime(), this.getEndTime());
	}
	@Override
	public void expand() {
		throw new UnsupportedOperationException("Contests cannot be expanded");
	}
}
