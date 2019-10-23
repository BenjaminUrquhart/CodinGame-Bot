package net.benjaminurquhart.codinbot.commands;

import java.net.URL;

import javax.imageio.ImageIO;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.Contest;
import net.benjaminurquhart.codinbot.util.ImageUtil;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetContest extends Command<CodinBot> {

	public GetContest() {
		super("contest");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		
		channel.sendTyping().queue($ -> {
			try {
				Contest contest = CodinGameAPI.API.getNextContest();
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(ImageUtil.getAverageColor(ImageIO.read(new URL(contest.getCoverUrl()))));
				eb.setImage(contest.getCoverUrl());
				eb.setAuthor(contest.getName(), contest.getUrlString());
				eb.setDescription(contest.getDescription());
				eb.addField("Type:", contest.getInfo(), true);
				eb.addField("Time:", contest.getDateString(), true);
				eb.addField("Misc. Info:", String.format("Puzzle Type: %s\nDifficulty: %s (%s)", contest.getType(), contest.getDifficulty(), contest.getDifficulty() == null ? "N/A" : contest.getDifficulty().name()), true);
				eb.setTimestamp(contest.getStartTime());
				eb.setFooter("Starts on", "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/120/microsoft/209/calendar_1f4c5.png");
				channel.sendMessage(eb.build()).queue();
			}
			catch(Exception e) {
				e.printStackTrace();
				channel.sendMessage(e.toString()).queue();
			}
		});
	}
	@Override
	public String getDescription() {
		return "retrives the upcoming contest";
	}
}
