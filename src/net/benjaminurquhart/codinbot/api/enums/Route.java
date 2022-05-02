package net.benjaminurquhart.codinbot.api.enums;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;

public enum Route {

	LOGIN("Codingamer/loginSite"),
	
	NEW_CONTRIB_COUNT("Contribution/getNewContributionCount"),
	FIND_UNSEEN_NOTIFICATIONS("Notification/findUnseenNotifications"),
	
	GET_LEADERBOARD_BY_PUZZLE_ID("Leaderboards/getFilteredPuzzleLeaderboard"),
	GET_PUZZLE_INFO_BY_ID("Puzzle/findProgressByPrettyId"),
	
	GET_TOTAL_ACHIEVEMENT_PROGRESS("CodinGamer/findTotalAchievementProgress"),
	GET_BEST_ACHIEVEMENTS("Achievement/findBestByCodingamerId"),
	GET_POINTS_BY_HANDLE("CodinGamer/findCodingamePointsStatsByHandle"),
	GET_CLASH_RANKING("Leaderboards/getCodinGamerClashRanking"),
	GET_PENDING_CLASHES("ClashOfCode/findPendingClashes"),
	
	GET_LEADERBOARD_BY_CONTEST_ID("Leaderboards/getFilteredChallengeLeaderboard"),
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
