package net.benjaminurquhart.codinbot.commands;

import java.util.List;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.Clash;
import net.benjaminurquhart.codinbot.api.entities.CodinGamer;

import net.benjaminurquhart.jch.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GetClash extends Command<CodinBot> {

	public GetClash() {
		super("clash");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		
		channel.sendTyping().queue($ -> {
			try {
				List<Clash> clashes = CodinGameAPI.API.getPendingClashes();
				if(clashes.isEmpty()) {
					channel.sendMessage("No clashes available. Go here to create your own: https://www.codingame.com/multiplayer/clashofcode").queue();
					return;
				}
				EmbedBuilder eb = new EmbedBuilder();
				List<CodinGamer> gamers;
				StringBuilder sb;
				long start;
				for(Clash clash : clashes) {
					gamers = clash.getParticipants();
					start = clash.getTimeUntilStartMillis();
					sb = new StringBuilder();
					for(CodinGamer gamer : gamers) {
						sb.append(gamer.getName());
						sb.append('\n');
					}
					if(gamers.size()<clash.getMaxPlayers()) {
						sb.append(String.format("[Join (%dm%ds remaining)](%s)", (start/1000)/60, (start/1000)%60, clash.getLink()));
					}
					else {
						sb.append(String.format("[Clash is full! %dm%ds until it starts](%s)", (start/1000)/60, (start/1000)%60, clash.getLink()));
					}
					eb.addField(String.format("%d/%d users", clash.getParticipants().size(), clash.getMaxPlayers()), sb.toString(), false);
				}
				eb.setTitle("Found "+clashes.size()+" available clash"+(clashes.size()<2?"":"es"));
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
		return "retrives all clashes that are accepting players";
	}
}
