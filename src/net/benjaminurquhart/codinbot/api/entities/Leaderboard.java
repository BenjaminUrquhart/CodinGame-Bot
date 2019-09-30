package net.benjaminurquhart.codinbot.api.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class Leaderboard {

	private Map<String, Integer> languageMap;
	private List<Contestant> contestants;
	
	private Puzzle puzzle;

	public Leaderboard(Puzzle puzzle, JSONObject json) {
		if(json.has("id") && json.has("message")) {
			if(json.getInt("id") == -1 && json.getString("message").equals("internal error")) {
				throw new UnsupportedOperationException("Attempted to get stats for a non-ranked puzzle!");
			}
		}
		JSONObject langs = json.getJSONObject("programmingLanguages");
		this.languageMap = new HashMap<>();
		this.contestants = json.getJSONArray("users")
				  		  	   .toList()
				  		  	   .stream()
				  		  	   .map(Map.class::cast)
				  		  	   .map(JSONObject::new)
				  		  	   .filter(j -> j.has("agentId"))
				  		  	   .map(Contestant::new)
				  		  	   .collect(Collectors.toList());
		
		for(String key : langs.keySet()) {
			languageMap.put(key, langs.getInt(key));
		}
	}
	public Map<String, Integer> getLanguages() {
		return Collections.unmodifiableMap(this.languageMap);
	}
	public List<Contestant> getContestants() {
		return Collections.unmodifiableList(this.contestants);
	}
	public Puzzle getPuzzle() {
		return puzzle;
	}
}
