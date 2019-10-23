package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.dns.minidns.MiniDnsResolver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Route;
import net.benjaminurquhart.jch.CommandHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressWarnings("deprecation")
public class CodinBot {

	private static String token;
	
	public static final boolean ENABLE_CHAT = false;
	
	public static AbstractXMPPConnection chatConnection;
	
	static {
		try {
			File file = new File("codinbot-config.json");
			if(file.exists() && file.isFile()) {
				JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
				token = json.getString("token");
				if(json.has("chat")) {
					try {
						JSONObject config = json.getJSONObject("chat");
						String email = config.getString("email");
						String password = config.getString("password");
						String channel = config.optString("channel", "world");
						
						JSONArray requestData = new JSONArray().put(email)
															   .put(password)
															   .put(true);
						Request request = new Request.Builder()
								.url(Route.LOGIN.toString())
								.addHeader("Content-Type", "application/json")
								.method(Route.LOGIN.getMethod(), RequestBody.create(MediaType.parse("application/json"), requestData.toString()))
								.build();
						
						Response response = CodinGameAPI.CLIENT.newCall(request).execute();
						
						JSONObject info = new JSONObject(response.body().string());
						JSONObject codingamer = info.getJSONObject("codinGamer");
						int id = codingamer.getInt("userId");
						
						MiniDnsResolver.setup();
						chatConnection = new XMPPTCPConnection(String.valueOf(id), password, "chat.codingame.com");
						chatConnection.connect().login();
						
						EntityBareJid channelID = (EntityBareJid) JidCreate.bareFrom(channel+"@conference.codingame.com");
								
						System.err.printf("Logged into CodinGame chat as %s (%d)\n", codingamer.getString("pseudo"), id);
						System.err.println("Channel: #"+channel);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
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
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(token.trim()).setEnabledCacheFlags(EnumSet.noneOf(CacheFlag.class)).build().awaitReady();
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
