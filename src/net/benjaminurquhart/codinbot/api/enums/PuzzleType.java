package net.benjaminurquhart.codinbot.api.enums;

public enum PuzzleType {

	SOLO,
	CONTEST,
	MULTIPLAYER,
	OPTIMIZATION;
	
	public static PuzzleType of(String s) {
		switch(s) {
		case "BATTLE":return CONTEST;
		case "multi":return MULTIPLAYER;
		case "optim":return OPTIMIZATION;
		}
		return s == null ? CONTEST : SOLO;
	}
}
