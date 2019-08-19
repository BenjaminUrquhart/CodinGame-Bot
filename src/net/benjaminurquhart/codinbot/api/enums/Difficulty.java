package net.benjaminurquhart.codinbot.api.enums;

public enum Difficulty {

	TUTORIAL("Tutorial"),
	CODEGOLF("Code Golf"),
	WORLDCUP("Contest"),
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard"),
	EXPERT("Very Hard");
	
	private final String name;
	
	private Difficulty(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}
