package net.benjaminurquhart.codinbot.api;

public enum Route {

	NEW_CONTRIB_COUNT("Contribution/getNewContributionCount"),
	FIND_UNSEEN_NOTIFICATIONS("Notification/findUnseenNotifications"),
	
	GET_TOTAL_ACHIEVEMENT_PROGRESS("CodinGamer/findTotalAchievementProgress"),
	GET_BEST_ACHIEVEMENTS("Achievement/findBestByCodingamerId"),
	GET_POINTS_BY_HANDLE("CodinGamer/findCodingamePointsStatsByHandle"),
	GET_CLASH_RANKING("Leaderboards/getCodinGamerClashRanking"),
	GET_STATS("CodinGamer/findCodingamePointsStatsByHandle"),
	
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
