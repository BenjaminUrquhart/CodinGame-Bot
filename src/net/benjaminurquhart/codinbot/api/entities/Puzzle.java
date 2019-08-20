package net.benjaminurquhart.codinbot.api.entities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Difficulty;
import net.benjaminurquhart.codinbot.api.enums.PuzzleType;
import net.benjaminurquhart.codinbot.api.enums.Route;

public class Puzzle {

	private PuzzleType type;
	private Difficulty difficulty;
	
	private String url;
	private String name;
	private String story;
	private String prettyId;
	private String background;
	private String description;
	private String learnDescription;
	
	private long imageID = -1, id = -1, attempts = -1, solves = -1, participants = -1;
	
	private boolean community;
	protected boolean expanded;
	
	public Puzzle(JSONObject json) {
		this.type = PuzzleType.of(json.optString("level", null));
		this.expanded = false;
		this.difficulty = type == PuzzleType.SOLO ? Difficulty.valueOf(json.getString("level").toUpperCase()) : null;
		
		this.prettyId = json.getString("id");
		
		this.name = json.getString("name");
		this.imageID = json.optLong("imageBinaryId", -1);
		
		this.url = "https://www.codingame.com/";
		switch(type) {
		case SOLO: url+="training/"+difficulty.name().toLowerCase()+"/"+prettyId;break;
		case MULTIPLAYER: url+="multiplayer/bot-programming/"+prettyId;break;
		case OPTIMIZATION: url+="multiplayer/optimization/"+prettyId;break;
		case CONTEST: url+="contests/"+prettyId;break;
		default: url+="404";
		}
	}
	
	public PuzzleType getType() {
		return type;
	}
	public Difficulty getDifficulty() {
		return difficulty;
	}
	public String getCoverUrl() {
		return imageID < 0 ? "https://static.codingame.com/assets/default_banner.be8cc728.jpg" : "https://static.codingame.com/servlet/fileservlet?id="+imageID;
	}
	public long getID() {
		return id;
	}
	public String getPrettyId() {
		return prettyId;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getLearnDescription() {
		return learnDescription;
	}
	public String getStory() {
		return story;
	}
	public String getBackground() {
		return background;
	}
	public boolean isCommunity() {
		return community;
	}
	public long getNumParticipants() {
		if(this.type == PuzzleType.SOLO) {
			throw new UnsupportedOperationException("Cannot get the number of participants in a solo game!");
		}
		return participants;
	}
	public long getNumAttempts() {
		return attempts;
	}
	public long getNumSolutions() {
		if(this.type == PuzzleType.MULTIPLAYER) {
			throw new UnsupportedOperationException("Multiplayer puzzles don't have solutions!");
		}
		return solves;
	}
	public URL getUrl() {
		try {
			return new URL(url);
		} 
		catch (MalformedURLException e) {
			throw new APIException(e);
		}
	}
	public String getUrlString() {
		return url;
	}
	public void expand() {
		if(expanded) {
			return;
		}
		try {
			JSONObject json = CodinGameAPI.API.getJSONObject(Route.GET_PUZZLE_INFO_BY_ID, new JSONArray().put(this.prettyId).put(JSONObject.NULL));
			this.id = json.getLong("id");
			this.solves = json.getLong("solvedCount");
			this.attempts = json.getLong("attemptCount");
			this.community = json.optBoolean("communityCreation", false);
			this.participants = json.optLong("globalTotal", json.optLong("total", -1));
			
			String html = json.getString("statement");
			if(html.contains("<!-- STORY -->")) {
				this.background = html.split("<!-- STORY -->")[1];
				background = background.replaceAll("<[^>]+>", "");
				background = StringEscapeUtils.unescapeHtml4(background);
				this.background = background.replace("\\n", "\n").replaceAll("(\\s){2,}", "$1").replace("\r", "").replaceAll("(\n{1,})", "\n").trim();
			}
			html = html.replaceAll("<[^>]+>", "");
			html = StringEscapeUtils.unescapeHtml4(html);
			if(json.has("contentDetails")) {
				JSONObject details = json.getJSONObject("contentDetails");
				this.description = details.getString("description");
				this.learnDescription = details.getString("learnDescription");
				
				learnDescription = learnDescription.replaceAll("<[^>]+>", "");
				learnDescription = StringEscapeUtils.unescapeHtml4(learnDescription);
				
				description = description.replaceAll("<[^>]+>", "");
				description = StringEscapeUtils.unescapeHtml4(description);
				
				this.story = details.getString("story");
				if(story.isEmpty()) {
					story = null;
				}
				else {
					story = story.replaceAll("<[^>]+>", "");
					story = StringEscapeUtils.unescapeHtml4(story);
				}
			}
			else {
				this.description = html;
			}
			this.description = description.replace("\\n", "\n").replaceAll("(\\s){2,}", "$1").replace("\r", "").replaceAll("(\n{1,})", "\n").trim();
		}
		catch (IOException | JSONException e) {
			throw new APIException(e);
		}
		expanded = true;
	}
	public String toString() {
		return String.format("Puzzle: %s (ID: %s, Type: %s, Difficulty: %s)", name, prettyId, type, difficulty == null ? "N/A" : difficulty);
	}
}
