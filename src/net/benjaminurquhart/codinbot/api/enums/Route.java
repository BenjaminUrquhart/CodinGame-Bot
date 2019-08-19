package net.benjaminurquhart.codinbot.api.enums;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;

public enum Route {

	NEW_CONTRIB_COUNT("Contribution/getNewContributionCount"),
	FIND_UNSEEN_NOTIFICATIONS("Notification/findUnseenNotifications"),
	
	GET_PUZZLE_INFO_BY_ID("Puzzle/findProgressByPrettyId"),
	
	GET_TOTAL_ACHIEVEMENT_PROGRESS("CodinGamer/findTotalAchievementProgress"),
	GET_BEST_ACHIEVEMENTS("Achievement/findBestByCodingamerId"),
	GET_POINTS_BY_HANDLE("CodinGamer/findCodingamePointsStatsByHandle"),
	GET_CLASH_RANKING("Leaderboards/getCodinGamerClashRanking"),
	
	GET_NEXT_CONTEST_ID("Challenge/findNextVisibleChallenge"),
	GET_CONTEST_BY_ID("Challenge/findWorldCupByPublicId"),
	
	SEARCH("search/search");
	
	private final String path,method;
	
	private Route(String path) {
		this("POST", path);
	}
	private Route(String method, String path) {
		this.path = path;
		this.method = method;
	}
	public String getMethod() {
		return method;
	}
	public String toString() {
		return CodinGameAPI.BASE_PATH+path;
	}
}
