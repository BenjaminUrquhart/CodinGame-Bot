package net.benjaminurquhart.codinbot.chat;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChatRelay extends ListenerAdapter {
	private ChatManager manager;
	private Set<String> channels;
	private File file;
	
	public ChatRelay(ChatManager manager) {
		this.channels = new HashSet<>();
		try {
			file = new File("channels.json");
			if(!file.exists()) {
				Files.write(file.toPath(), "[]".getBytes());
			}
			JSONArray json = new JSONArray(new String(Files.readAllBytes(file.toPath())));
			json.forEach(w -> channels.add(String.valueOf(w)));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		this.manager = manager;
	}
	public boolean addChannel(String id) {
		boolean out = channels.add(id);
		this.save();
		return out;
	}
	public boolean removeChannel(String id) {
		boolean out = channels.remove(id);
		this.save();
		return out;
	}
	public void save() {
		try {
			Files.write(file.toPath(), new JSONArray(channels).toString().getBytes());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		User user = event.getAuthor();
		if(user.isBot() || event.getMessage().isWebhookMessage()) {
			return;
		}
		if(!channels.contains(event.getChannel().getId())) {
			return;
		}
		Message msg = event.getMessage();
		if(msg.getMentionedUsers().contains(event.getJDA().getSelfUser())) {
			return;
		}
		String author = event.getGuild().getId().equals("466965651135922206") ? event.getMember().getEffectiveName() : user.getAsTag();
		String out = String.format("%s:\n%s", author, msg.getContentDisplay());
		System.out.printf("%s (%d/%d): %s\n", 
				msg.getAuthor().getAsTag(), 
				msg.getAuthor().getIdLong(), 
				msg.getGuild().getIdLong(), 
				msg.getContentRaw()
		);
		try {
			manager.getChat().sendMessage(out);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
