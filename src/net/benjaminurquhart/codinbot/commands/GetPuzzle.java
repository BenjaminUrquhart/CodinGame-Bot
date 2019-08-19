package net.benjaminurquhart.codinbot.commands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.Puzzle;
import net.benjaminurquhart.codinbot.api.enums.PuzzleType;
import net.benjaminurquhart.codinbot.util.ImageUtil;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class GetPuzzle extends Command<CodinBot> {

	public GetPuzzle() {
		super("puzzle", "puzzle name");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		channel.sendTyping().queue($ -> {
			try {
				List<Puzzle> puzzles = CodinGameAPI.getPuzzlesByName(args[2]);
				if(puzzles.isEmpty()) {
					channel.sendMessage("No puzzles by that name were found. Please try again.").queue();
					return;
				}
				Puzzle puzzle = null;
				for(Puzzle p : puzzles) {
					if(p.getName().equalsIgnoreCase(args[2])) {
						puzzle = p;
						break;
					}
				}
				if(puzzle == null) {
					puzzle = puzzles.get(0);
				}
				EmbedBuilder eb = new EmbedBuilder();
				
				eb.setTitle(puzzle.getName(), puzzle.getUrlString());
				eb.setImage(puzzle.getCoverUrl());
				
				eb.addField("Type:", puzzle.getType().toString(), false);
				
				if(puzzle.getType() == PuzzleType.MULTIPLAYER || puzzle.getType() == PuzzleType.OPTIMIZATION) {
					BufferedImage image = ImageIO.read(new URL(puzzle.getCoverUrl()));
					eb.setColor(ImageUtil.getAverageColor(image));
				}
				else {
					switch(puzzle.getDifficulty()) {
					case TUTORIAL: eb.setColor(Color.CYAN); break;
					case EASY: eb.setColor(Color.GREEN);break;
					case MEDIUM: eb.setColor(Color.YELLOW);break;
					case HARD: eb.setColor(Color.ORANGE);break;
					case EXPERT: eb.setColor(Color.RED);break;
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
					if(puzzle.getLearnDescription() != null) {
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
						if(puzzle.getStory() != null) {
							eb.addField("Story:", puzzle.getStory(), false);
						}
						if(puzzle.getBackground() != null) {
							String background = puzzle.getBackground();
							if(background.length() > 1024) {
								background = background.substring(0,1020)+"...";
							}
							eb.addField("Background:", background, false);
						}
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

}
