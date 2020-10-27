package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import net.benjaminurquhart.codinbot.chat.ChatManager;
import net.benjaminurquhart.codinbot.chat.ChatRelay;
import net.benjaminurquhart.jch.CommandHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


public class CodinBot {

	private static JDA jda;
	private static String token;
	
	private static JSONObject config;
	
	private ChatManager chatManager;
	private ChatRelay relay;
	
	static {
		try {
			File file = new File("codinbot-config.json");
			if(file.exists() && file.isFile()) {
				JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
				token = json.getString("token");
				if(json.has("chat")) {
					config = json.getJSONObject("chat");
				}
			}
			else {
				token = new String(Files.readAllBytes(new File("token.txt").toPath()));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			if(token == null) {
				System.exit(1);
			}
		}
	}
	
	public static final CodinBot INSTANCE = new CodinBot();
	
	private CodinBot() {
		if(config != null) {
			if(config.has("disabled") && config.getBoolean("disabled")) {
				System.out.println("Chat manually disabled");
				return;
			}
			chatManager = new ChatManager(config);
			relay = new ChatRelay(chatManager);
		}
		else {
			System.out.println("Chat disabled (No config)");
		}
	}
	public static JDA getJDA() {
		return jda;
	}
	public ChatManager getChatManager() {
		return chatManager;
	}
	public ChatRelay getChatRelay() {
		return relay;
	}
	public static void main(String[] args) throws Exception {
		jda = JDABuilder.createDefault(token.trim()).setEnabledCacheFlags(EnumSet.noneOf(CacheFlag.class)).setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS)).build();
		CommandHandler<CodinBot> handler = new CommandHandler<>(
				INSTANCE,
				null,
				"273216249021071360",
				"net.benjaminurquhart.codinbot.commands"
		);
		handler.setRatelimit(1, TimeUnit.SECONDS);
		jda.addEventListener(handler);
		if(INSTANCE.getChatRelay() != null) {
			jda.addEventListener(INSTANCE.getChatRelay());
		}
	}
}
