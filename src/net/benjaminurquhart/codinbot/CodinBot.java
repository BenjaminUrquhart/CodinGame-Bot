package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.chat.CodinGameChatListener;
import net.benjaminurquhart.jch.CommandHandler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import okhttp3.Request;
import okhttp3.WebSocket;

@SuppressWarnings("unused")
public class CodinBot {

	private static String token;
	
	public static WebSocket WEBSOCKET;
	public static final boolean ENABLE_CHAT = false;
	
	static {
		try {
			File file = new File("codinbot-config.json");
			if(file.exists() && file.isFile() && ENABLE_CHAT) {
				JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
				//token = json.getString("token");
				if(json.has("cg-email") && json.has("cg-pass")) {
					CodinGameChatListener listener = new CodinGameChatListener(json.getString("cg-email"), json.getString("cg-pass"));
					WEBSOCKET = CodinGameAPI.CLIENT.newWebSocket(new Request.Builder().url("wss://chat.codingame.com/xmpp-websocket").build(), listener);
				}
			}
			token = new String(Files.readAllBytes(new File("token.txt").toPath())).trim();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(token).setEnabledCacheFlags(EnumSet.noneOf(CacheFlag.class)).build().awaitReady();
		CommandHandler<CodinBot> handler = new CommandHandler<>(
				new CodinBot(),
				jda.getSelfUser().getAsMention()+" ",
				"273216249021071360",
				"net.benjaminurquhart.codinbot.commands"
		);
		handler.setRatelimit(1, TimeUnit.SECONDS);
		jda.addEventListener(handler);
	}
}
