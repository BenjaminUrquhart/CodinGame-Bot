package net.benjaminurquhart.codinbot.commands;

import java.io.File;
import java.nio.file.Files;
import java.util.Set;

import org.json.JSONArray;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.chat.ChatListener;
import net.benjaminurquhart.codinbot.chat.ChatManager;
import net.benjaminurquhart.jch.Command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class LinkChat extends Command<CodinBot> {
	
	private Set<String> webhooks;
	private File file;
	
	private ChatListener listener;
	
	public LinkChat() {
		super("link");
		listener = CodinBot.INSTANCE.getChatManager().getListener();
		webhooks = listener.getWebhooks();
		file = new File("webhooks.json");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		ChatManager manager = self.getChatManager();
		if(manager.getChat() == null || !manager.getConnection().isAuthenticated()) {
			Throwable root = manager.getWatchDog() == null ? manager.getRootFailureCause() : manager.getWatchDog().getRootFailureCause();
			Throwable cause = manager.getWatchDog() == null ? manager.getFailureCause() : manager.getWatchDog().getFailureCause();
			channel.sendMessage(String.format("Chat is currently unavailable\nConnection: %s\nMUC: %s\nCause: %s\nRoot Cause: %s", manager.getConnection(), manager.getChat(), cause, root)).queue();
			return;
		}
		Member selfMember = event.getGuild().getSelfMember();
		if(!selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
			channel.sendMessage("I require the Manage Webhooks permission").queue();
			return;
		}
		if(!event.getMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
			channel.sendMessage("You do not have the Manage Webhooks permission!").queue();
			return;
		}
		channel.retrieveWebhooks().queue(webhooks -> {
			Webhook hook = null;
			for(Webhook webhook : webhooks) {
				if(webhook.getOwner().getId().equals(selfMember.getId())) {
					hook = webhook;
					break;
				}
			}
			if(hook == null) {
				channel.createWebhook(manager.getChat().getRoom().toString()).queue(wh -> {
					try {
						this.listener.addWebhook(wh.getUrl());
						Files.write(file.toPath(), new JSONArray(this.webhooks).toString().getBytes());
						channel.sendMessage("Linked!").queue();
					}
					catch(Exception e) {
						channel.sendMessage("Failed to save webhook URL! You will need to relink this channel after a restart.\n"+e.toString()).queue();
					}
				});
				return;
			}
			else if(!this.listener.addWebhook(hook.getUrl())) {
				channel.sendMessage("This channel is already linked!").queue();
				return;
			}
			try {
				Files.write(file.toPath(), new JSONArray(this.webhooks).toString().getBytes());
				channel.sendMessage("Linked!").queue();
			}
			catch(Exception e) {
				channel.sendMessage("Failed to save webhook URL! You will need to relink this channel after a restart.\n"+e.toString()).queue();
			}
		});
	}
}
