package net.benjaminurquhart.codinbot.commands;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.chat.ChatListener;
import net.benjaminurquhart.jch.Command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class LinkChat extends Command<CodinBot> {
	
	private Set<String> webhooks;
	private File file;
	
	private ChatListener listener;
	private volatile boolean addedListener;
	
	public LinkChat() {
		super("link");
		webhooks = new HashSet<>();
		try {
			file = new File("webhooks.json");
			if(!file.exists()) {
				Files.write(file.toPath(), "[]".getBytes());
			}
			JSONArray json = new JSONArray(new String(Files.readAllBytes(file.toPath())));
			json.forEach(w -> webhooks.add(String.valueOf(w)));
			listener = new ChatListener(webhooks);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		if(self.getChat() == null) {
			channel.sendMessage("Chat is unavailable at the moment\nConnection: "+self.getConnection()+"\nMUC: "+self.getChat()).queue();
			return;
		}
		Member selfMember = event.getGuild().getSelfMember();
		if(!selfMember.hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
			channel.sendMessage("I require the Manage Webhooks permission").queue();
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
				if(!event.getMember().hasPermission(channel, Permission.MANAGE_WEBHOOKS)) {
					channel.sendMessage("You do not have the Manage Webhooks permission!").queue();
					return;
				}
				channel.createWebhook(self.getChat().getRoom().toString()).queue(wh -> {
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
	@Override
	public String[] getAliases() {
		MultiUserChat chat = this.getHandler().getSelf().getChat();
		if(chat != null && !addedListener) {
			chat.addMessageListener(listener);
			addedListener = true;
		}
		return super.getAliases();
	}
}
