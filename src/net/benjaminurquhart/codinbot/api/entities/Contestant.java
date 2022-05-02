package net.benjaminurquhart.codinbot.api.entities;

import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.enums.League;

public class Contestant extends CodinGamer {

	private double score;
	private League league;
	private String submissionLang;
	
	private long agentId, rank, leagueRank;
	
	public Contestant(JSONObject json) {
		super(convert(json));
		
		//System.err.println(json);
		this.score = json.getDouble("score");
		this.submissionLang = json.getString("programmingLanguage");
		
		JSONObject league = json.getJSONObject("league");
		
		this.league = League.values()[league.getInt("divisionCount")-league.getInt("divisionIndex")-1];
		
		this.rank = json.getLong("rank");
		this.agentId = json.getLong("agentId");
		this.leagueRank = json.getLong("localRank");
	}
	private static JSONObject convert(JSONObject json) {
		JSONObject out = new JSONObject();
		JSONObject gamer = json.getJSONObject("codingamer");
		out.put("id", gamer.getLong("userId"));
		out.put("name", gamer.optString("pseudo", "Anonymous"));
		out.put("handle", gamer.getString("publicHandle"));
		out.put("imageBinaryId", gamer.optLong("avatar", -1));
		return out;
	}
	
	public String getLanguage() {
		return submissionLang;
	}
	public League getLeague() {
		return league;
	}
	public double getScore() {
		return score;
	}
	public long getAgentId() {
		return agentId;
	}
	public long getGlobalRank() {
		return rank;
	}
	public long getLeagueRank() {
		return leagueRank;
	}
	
	@Override
	public String toString() {
		return String.format("Contestant (Name: %s, Lang: %s, League: %s, Score: %.2f, Rank: %d, League Rank: %d)", this.getName(), this.getLanguage(), this.getLeague(), this.getScore(), this.getGlobalRank(), this.getLeagueRank());
	}
}
