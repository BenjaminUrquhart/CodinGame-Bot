package net.benjaminurquhart.codinbot.commands;

import java.awt.Color;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.chat.ChatManager;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetStatus extends Command<CodinBot> {
	
	public GetStatus() {
		super("status");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		try {
			channel.sendTyping().queue();
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.GREEN);
			eb.setTitle("Status");
			boolean apiDown = false;
			long start, duration;
			Request request;
			Response response = null;
			try {
				request = new Request.Builder()
							.url("https://codingame.com")
							.get()
							.addHeader("User-Agent", "CodinBot Status Check")
							.build();
				start = System.currentTimeMillis();
				response = CodinGameAPI.CLIENT.newCall(request).execute();
				duration = System.currentTimeMillis()-start;
				eb.addField("Website", "Online ("+duration+"ms)", true);
				response.close();
			}
			catch(Exception e) {
				eb.setColor(Color.YELLOW);
				eb.addField("Website", "Offline" + (response == null ? "" : " (HTTP "+response.code()+")"), true);
			}
			try {
				request = new Request.Builder()
							.url("https://www.codingame.com/services/ClientEvent/postEvent")
							.post(RequestBody.create(MediaType.get("application/json"), "[\"API Status Check\",null,{}]"))
							.addHeader("User-Agent", "CodinBot Status Check")
							.build();
				start = System.currentTimeMillis();
				response = CodinGameAPI.CLIENT.newCall(request).execute();
				duration = System.currentTimeMillis()-start;
				if(response.code() != 204) {
					throw new IllegalStateException("Expected 204 status code, got "+response.code());
				}
				eb.addField("API", "Online ("+duration+"ms)", true);
				response.close();
			}
			catch(Exception e) {
				apiDown = true;
				eb.setColor(Color.YELLOW);
				eb.addField("API", "Offline" + (response == null ? "" : " (HTTP "+response.code()+")"), true);
			}
			try {
				String chatState = "Unknown";
				ChatManager manager = self.getChatManager();
				AbstractXMPPConnection conn = manager.getConnection();
				MultiUserChat chat = manager.getChat();
				if(!conn.isAuthenticated()) {
					eb.setColor(Color.YELLOW);
					chatState = "Unauthenticated";
					if(manager.getWatchDog() != null) {
						Throwable root = manager.getWatchDog().getRootFailureCause();
						if(root != null) {
							chatState += "\n" + root;
						}
					}
				}
				else if(chat == null || !chat.isJoined()) {
					eb.setColor(Color.YELLOW);
					chatState = "Not bound to MUC";
				}
				else {
					chatState = "Connected (Channel: #"+manager.getChannel()+")";
				}
				eb.addField("Chat", chatState, true);
			}
			catch(Exception e) {
				eb.setColor(Color.YELLOW);
				eb.addField("Chat", e.toString(), true);
			}
			if(apiDown) {
				eb.setColor(Color.RED);
				eb.setDescription("API is down!");
			}
			channel.sendMessage(eb.build()).queue();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
