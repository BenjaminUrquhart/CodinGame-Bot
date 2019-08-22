package net.benjaminurquhart.codinbot.api.entities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class Clash {
	
	private List<CodinGamer> gamers;
	private String handle;
	
	private long millis, minPlayers, maxPlayers;
	
	public Clash(JSONObject json) {
		this.handle = json.getString("publicHandle");
		this.millis = json.getLong("msBeforeStart");
		
		this.minPlayers = json.getLong("nbPlayersMin");
		this.maxPlayers = json.getLong("nbPlayersMax");
		
		this.gamers = json.getJSONArray("players")
						  .toList()
						  .stream()
						  .map(Map.class::cast)
						  .map(user -> new CodinGamer(
								  (String)user.get("codingamerNickname"), 
								  (String)user.get("codingamerHandle"), 
								  String.valueOf(user.get("codingamerAvatarId")))
						  ).collect(Collectors.toList());
	}
	public long getTimeUntilStartMillis() {
		return millis;
	}
	public long getMinPlayers() {
		return minPlayers;
	}
	public long getMaxPlayers() {
		return maxPlayers;
	}
	public String getLink() {
		return "https://www.codingame.com/clashofcode/clash/"+handle;
	}
	public List<CodinGamer> getParticipants() {
		return Collections.unmodifiableList(gamers);
	}
}
