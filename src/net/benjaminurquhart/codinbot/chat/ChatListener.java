package net.benjaminurquhart.codinbot.chat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.CodinGamer;

public class ChatListener implements MessageListener {
	
	private Map<String, String> avatarCache;
	
	private Set<String> webhooks;
	private WebhookCluster cluster;
	
	
	public ChatListener(Set<String> webhooks) {
		this.cluster = new WebhookCluster(webhooks.size());
		this.avatarCache = new HashMap<>();
		this.webhooks = webhooks;
		webhooks.stream()
				.map(WebhookClient::withUrl)
				.forEach(cluster::addWebhooks);
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
		builder.setContent(message.getBody());
		cluster.broadcast(builder.build());
	}
	
}
