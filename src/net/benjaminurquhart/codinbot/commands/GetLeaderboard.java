package net.benjaminurquhart.codinbot.commands;

import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.Contestant;
import net.benjaminurquhart.codinbot.api.entities.Leaderboard;
import net.benjaminurquhart.codinbot.api.entities.Puzzle;
import net.benjaminurquhart.codinbot.util.ImageUtil;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetLeaderboard extends Command<CodinBot> {

	public GetLeaderboard() {
		super("leaderboard", "puzzle");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split(" ", 3);
		channel.sendTyping().queue($ -> {
			try {
				Leaderboard board = null;
				Puzzle puzzle = null;
				
				if(args.length < 3) {
					puzzle = CodinGameAPI.API.getNextContest();
					if(puzzle == null) {
						channel.sendMessage("No active contest found.").queue();
						return;
					}
					board = puzzle.getLeaderboard();
				}
				else {
					List<Puzzle> puzzles = CodinGameAPI.API.getPuzzlesByName(args[2]);
					for(Puzzle p : puzzles) {
						try {
							board = p.getLeaderboard();
							puzzle = p;
							break;
						}
						catch(Exception e) {
							System.err.println(p+"\n");
							e.printStackTrace();
						}
					}
					if(puzzle == null) {
						if(puzzles.isEmpty()) {
							channel.sendMessage("No puzzles found by that name").queue();
						}
						else {
							channel.sendMessage("All "+puzzles.size()+" puzzles found did not have a leaderboard attached to them").queue();
						}
						return;
					}
				}

				EmbedBuilder eb = new EmbedBuilder();
				
				List<Contestant> gamers = board.getContestants();
				Map<String, Integer> langs = new HashMap<>();
				String lang;
				
				for(Contestant contestant : gamers) {
					lang = contestant.getLanguage();
					langs.put(lang, langs.containsKey(lang) ? langs.get(lang) + 1 : 1);
				}
				
				eb.setAuthor(puzzle.getName(), puzzle.getUrlString());
				eb.setColor(ImageUtil.getAverageColor(ImageIO.read(new URL(puzzle.getCoverUrl()))));
				eb.setImage(puzzle.getCoverUrl());
				
				StringBuilder sb = new StringBuilder();
				String s;
				
				Contestant gamer;
				for(int i = 0; i < Math.min(10, gamers.size()); i++) {
					gamer = gamers.get(i);
					s = gamer.getName();
					if(s == null) {
						s = "Anonymous";
					}
					sb.append(i);
					sb.append(": ");
					sb.append(s);
					sb.append(" (");
					sb.append(gamer.getLanguage());
					sb.append(", ");
					sb.append(gamer.getScore());
					sb.append(")\n");
				}
				eb.addField("Top Users:", sb.toString().trim(), true);
				
				List<String> langsSorted = langs.keySet().stream()
														 .sorted((l1,l2) -> langs.get(l2)-langs.get(l1))
														 .collect(Collectors.toList());
				sb = new StringBuilder();
				
				for(int i = 0; i < Math.min(10, langsSorted.size()); i++) {
					s = langsSorted.get(i);
					sb.append(s);
					sb.append(": ");
					sb.append(String.format("%.2f%%", langs.get(s)/(double)gamers.size()*100.0));
					sb.append("\n");
				}
				eb.addField("Top* Languages:", sb.toString().trim(), true);
				eb.setFooter("* Top languages are determined by the top "+gamers.size()+" players as of ", "https://chat.is-going-to-rickroll.me/i/0pZHzb238aZqmQ.png");
				eb.setTimestamp(Instant.now());
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
		return new String[] {"stats"};
	}
	@Override
	public String getDescription() {
		return "retrives basic leaderboard info about the given puzzle";
	}
}
