package net.benjaminurquhart.codinbot.chat;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.CodinGamer;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class ChatListener implements MessageListener {
	
	private Map<String, String> avatarCache;
	
	private Set<String> webhooks;
	private WebhookCluster cluster;
	
	private int messageCount;
	
	
	public ChatListener() {
		this.cluster = new WebhookCluster();
		this.avatarCache = new HashMap<>();
		this.webhooks = new HashSet<>();
		this.messageCount = -20;
		try {
			File file = new File("webhooks.json");
			if(!file.exists()) {
				Files.write(file.toPath(), "[]".getBytes());
			}
			JSONArray json = new JSONArray(new String(Files.readAllBytes(file.toPath())));
			json.forEach(w -> webhooks.add(String.valueOf(w)));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		webhooks.stream()
				.map(WebhookClient::withUrl)
				.forEach(cluster::addWebhooks);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> cluster.close()));
	}
	public Set<String> getWebhooks() {
		return webhooks;
	}
	public boolean addWebhook(String url) {
		if(webhooks.contains(url)) {
			return false;
		}
		webhooks.add(url);
		cluster.addWebhooks(WebhookClient.withUrl(url));
		return true;
	}
	public void removeWebhook(String url) {
		webhooks.remove(url);
		cluster.removeIf(client -> client.getUrl().equals(url));
	}

	@Override
	public void processMessage(Message message) {
		if(message == null || message.getBody() == null) {
			return;
		}
		if(messageCount++ < 0) {
			return;
		}
		String from = message.getFrom() == null ? "???" : message.getFrom().getResourceOrEmpty().toString();
		if(from.isEmpty()) {
			from = "???";
		}
		WebhookMessageBuilder builder = new WebhookMessageBuilder();
		if(!from.equals("???")) {
			builder.setAvatarUrl(avatarCache.computeIfAbsent(from, username -> {
				try {
					List<CodinGamer> users = CodinGameAPI.API.getUsersByName(username);
					for(CodinGamer user : users) {
						if(user.getName().equals(username)) {
							return user.getImageUrl();
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return "https://chat.is-going-to-rickroll.me/i/xLtF1gGPkNOhxw.png";
			}));
		}
		builder.setUsername(from);
		builder.setContent(MarkdownSanitizer.escape(message.getBody()));
		cluster.broadcast(builder.build());
	}
	public void closeCluster() {
		cluster.close();
	}
}
