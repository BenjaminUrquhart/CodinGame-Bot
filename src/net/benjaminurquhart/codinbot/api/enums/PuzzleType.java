package net.benjaminurquhart.codinbot.api.enums;

public enum PuzzleType {

	SOLO,
	MULTIPLAYER,
	OPTIMIZATION;
	
	public static PuzzleType of(String s) {
		switch(s) {
		case "multi":return MULTIPLAYER;
		case "optim":return OPTIMIZATION;
		}
		return SOLO;
	}
}
