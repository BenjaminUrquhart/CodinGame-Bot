package net.benjaminurquhart.codinbot.commands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.Puzzle;
import net.benjaminurquhart.codinbot.api.enums.Difficulty;
import net.benjaminurquhart.codinbot.api.enums.PuzzleType;
import net.benjaminurquhart.codinbot.util.ImageUtil;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class GetPuzzle extends Command<CodinBot> {

	private static final Map<String, Predicate<Puzzle>> PREDICATES;
	
	static {
		PREDICATES = new HashMap<>();
		
		PREDICATES.put("easy", p -> p.getDifficulty() == Difficulty.EASY || p.getDifficulty() == Difficulty.TUTORIAL);
		PREDICATES.put("medium", p -> p.getDifficulty() == Difficulty.MEDIUM);
		PREDICATES.put("hard", p -> p.getDifficulty() == Difficulty.HARD);
		PREDICATES.put("expert", p -> p.getDifficulty() == Difficulty.EXPERT);
		
		PREDICATES.put("solo", p -> p.getType() == PuzzleType.SOLO && p.getDifficulty() != Difficulty.CODEGOLF);
		PREDICATES.put("multi", p -> p.getType() == PuzzleType.MULTIPLAYER);
		PREDICATES.put("optim", p -> p.getType() == PuzzleType.OPTIMIZATION);
		PREDICATES.put("golf", p -> p.getDifficulty() == Difficulty.CODEGOLF);
	}
	public GetPuzzle() {
		super("puzzle", "puzzle name");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().replaceAll("(\\s{0,})--[fF][uU][lL][lL](\\s{0,})", "").split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		final boolean all = event.getMessage().getContentRaw().toLowerCase().contains("--full");
		channel.sendTyping().queue($ -> {
			try {
				List<Puzzle> puzzles = CodinGameAPI.API.getPuzzlesByName(args[2]);
				if(puzzles.isEmpty()) {
					channel.sendMessage("No puzzles by that name were found. Please try again.").queue();
					return;
				}
				if(!args[1].equalsIgnoreCase("puzzle")) {
					Predicate<Puzzle> filter = null;
					switch(args[1].toLowerCase()) {
					case "easy":
					case "tutorial": filter = PREDICATES.get("easy"); break;
					case "medium": filter = PREDICATES.get("medium"); break;
					case "hard": filter = PREDICATES.get("hard"); break;
					case "expert":
					case "veryhard": filter = PREDICATES.get("expert"); break;
					case "solo": filter = PREDICATES.get("solo"); break;
					case "multi":
					case "multiplayer": filter = PREDICATES.get("multi"); break;
					case "optim":
					case "optimization": filter = PREDICATES.get("optim"); break;
					case "golf":
					case "codegolf": filter = PREDICATES.get("golf"); break;
					default: filter = p -> true; break;
					}
					puzzles = puzzles.stream().filter(filter).collect(Collectors.toList());
					if(puzzles.isEmpty()) {
						channel.sendMessage("No puzzles by that name were found in the category `"+args[1].toLowerCase().replace("`","")+"`. Please try again.").queue();
						return;
					}
				}
				Puzzle puzzle = null;
				for(Puzzle p : puzzles) {
					if(p.getName().equalsIgnoreCase(args[2])) {
						puzzle = p;
						break;
					}
				}
				if(puzzle == null) {
					for(Puzzle p : puzzles) {
						if(!p.getCoverUrl().equals("https://static.codingame.com/assets/default_banner.be8cc728.jpg")) {
							puzzle = p;
							break;
						}
					}
				}
				if(puzzle == null) {
					puzzle = puzzles.get(0);
				}
				EmbedBuilder eb = new EmbedBuilder();
				
				eb.setTitle(puzzle.getName(), puzzle.getUrlString());
				eb.setImage(puzzle.getCoverUrl());
				
				eb.addField("Type:", puzzle.getDifficulty() == Difficulty.CODEGOLF ? "CODEGOLF" : puzzle.getType().toString(), false);
				
				if(puzzle.getType() != PuzzleType.SOLO || puzzle.getDifficulty() == Difficulty.CODEGOLF) {
					BufferedImage image = ImageIO.read(new URL(puzzle.getCoverUrl()));
					eb.setColor(ImageUtil.getAverageColor(image));
				}
				else {
					switch(puzzle.getDifficulty()) {
					case TUTORIAL: eb.setColor(Integer.parseInt("CFB53B",16)); break;
					case EASY: eb.setColor(Color.GREEN);break;
					case MEDIUM: eb.setColor(Color.YELLOW);break;
					case HARD: eb.setColor(Integer.parseInt("ff7300",16));break;
					case EXPERT: eb.setColor(Color.RED);break;
					default: break; // Should never happen
					}
					eb.addField("Difficulty:", puzzle.getDifficulty().toString(), false);
				}
				try {
					puzzle.expand();
					String description = puzzle.getDescription();
					if(description.length() > 1024) {
						description = description.substring(0,1020)+"...";
					}
					eb.setDescription(description);
					if(all && puzzle.getLearnDescription() != null) {
						String additional = puzzle.getLearnDescription();
						if(additional.length() > 1024) {
							additional = additional.substring(0,1020)+"...";
						}
						eb.addField("Additional info:", additional, true);
					}
					if(puzzle.getType() == PuzzleType.MULTIPLAYER) {
						eb.addField("Players in arena:", String.valueOf(puzzle.getNumParticipants()), true);
					}
					else {
						eb.addField("Puzzle stats:", String.format("Started: %d\nSolved: %d\nCompletion rate: %.0f%%", puzzle.getNumAttempts(), puzzle.getNumSolutions(), puzzle.getNumSolutions()/(double)puzzle.getNumAttempts()*100), true);
					}
					if(all && puzzle.getStory() != null) {
						String story = puzzle.getStory();
						if(story.length() > 1024) {
							story = story.substring(0,1020)+"...";
						}
						eb.addField("Story:", story, false);
					}
					if(all && puzzle.getBackground() != null) {
						String background = puzzle.getBackground();
						if(background.length() > 1024) {
							background = background.substring(0,1020)+"...";
						}
						eb.addField("Background:", background, false);
					}
				}
				catch(APIException e) {
					eb.setDescription("Failed to get full puzzle details: " + e.getCause());
				}
				channel.sendMessage(eb.build()).queue();
			}
			catch(Exception e) {
				e.printStackTrace();
				channel.sendMessage(e.toString()).queue();
			}
		});
	}
	
	@Override
	public String[] getAliases() {
		return new String[] {"solo","easy","medium","hard","expert","veryhard","golf","codegolf","multi","multiplayer","optim","optimization"};
	}
	@Override
	public String getDescription() {
		return "retrives info about the given puzzle";
	}
}
