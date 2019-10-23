package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.dns.minidns.MiniDnsResolver;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

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
	
	private AbstractXMPPConnection chatConnection;
	private MultiUserChat chat;
	
	private static JSONObject config;
	
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
	public CodinBot() {
		if(config != null) {
			if(config.has("disabled") && config.getBoolean("disabled")) {
				System.out.println("Chat manually disabled");
				return;
			}
			try {
				String email = config.getString("email");
				String password = config.getString("password");
				String channel = config.optString("channel", "world");
				
				System.out.println("Getting ID for user with email "+email);
				
				JSONArray requestData = new JSONArray().put(email)
													   .put(password)
													   .put(true);
				MiniDnsResolver.setup();
				Request request = new Request.Builder()
						.url(Route.LOGIN.toString())
						.addHeader("Content-Type", "application/json")
						.method(Route.LOGIN.getMethod(), RequestBody.create(MediaType.parse("application/json"), requestData.toString()))
						.build();
				
				Response response = CodinGameAPI.CLIENT.newCall(request).execute();
				
				JSONObject info = new JSONObject(response.body().string());
				JSONObject codingamer = info.getJSONObject("codinGamer");
				int id = codingamer.getInt("userId");
				
				System.out.println("ID: "+id);
				
				SmackConfiguration.setDefaultPacketReplyTimeout(15000);
				XMPPTCPConnectionConfiguration chatConfig = XMPPTCPConnectionConfiguration.builder()
						.setUsernameAndPassword(String.valueOf(id), password)
						.setXmppDomain("chat.codingame.com")
						.setConnectTimeout(15*1000)
						.build();
				chatConnection = new XMPPTCPConnection(chatConfig);
				chatConnection.connect().login();
				System.out.println("Connected to XMPP server. Joining channel #"+channel+"...");
				EntityBareJid channelID = (EntityBareJid) JidCreate.bareFrom(channel+"@conference.codingame.com");
				Resourcepart nick = Resourcepart.from(codingamer.getString("pseudo"));
				
				MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatConnection);
				chat = manager.getMultiUserChat(channelID);
				chat.createOrJoin(nick);
				while(chat.pollMessage() != null);
				chat.addMessageListener((message) -> {
					if(message == null || message.getBody() == null) {
						System.err.println("Received null message ("+message+")");
						return;
					}
					String from = message.getFrom() == null ? "???" : message.getFrom().getResourceOrEmpty().toString();
					System.out.println(from+": "+message.getBody());
				});
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						System.out.println("Shutting down!");
						if(chatConnection != null) {
							chat.leaveSync();
							chatConnection.disconnect();
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}));
				System.out.println("Success!");
				System.out.printf("Logged into CodinGame chat as %s (%d)\n", codingamer.getString("pseudo"), id);
				System.out.println("Channel: #"+channel);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Chat disabled (No config)");
		}
	}
	public MultiUserChat getChat() {
		return chat;
	}
	public AbstractXMPPConnection getConnection() {
		return chatConnection;
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
