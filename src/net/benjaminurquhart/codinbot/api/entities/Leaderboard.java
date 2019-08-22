package net.benjaminurquhart.codinbot.api.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class Leaderboard {

	private Map<String, Integer> languageMap;
	private List<CodinGamer> gamers;
	
	private Puzzle puzzle;
	
	@SuppressWarnings("unchecked")
	public Leaderboard(Puzzle puzzle, JSONObject json) {
		if(json.has("id") && json.has("message")) {
			if(json.getInt("id") == -1 && json.getString("message").equals("internal error")) {
				throw new UnsupportedOperationException("Attempted to get stats for a non-ranked puzzle!");
			}
		}
		JSONObject langs = json.getJSONObject("programmingLanguages");
		this.languageMap = new HashMap<>();
		this.gamers = json.getJSONArray("users")
				  		  .toList()
				  		  .stream()
				  		  .map(Map.class::cast)
				  		  .map(m -> (Map<String,Object>)m.get("codingamer"))
				  		  .map(user -> new CodinGamer(
				  				  (String)user.get("pseudo"), 
				  				  (String)user.get("publicHandle"), 
				  				  String.valueOf(user.get("avatar")))
				  		  ).collect(Collectors.toList());
		
		for(String key : langs.keySet()) {
			languageMap.put(key, langs.getInt(key));
		}
	}
	public Map<String, Integer> getLanguages() {
		return Collections.unmodifiableMap(this.languageMap);
	}
	public List<CodinGamer> getUsers() {
		return Collections.unmodifiableList(this.gamers);
	}
	public Puzzle getPuzzle() {
		return puzzle;
	}
}
