package net.benjaminurquhart.codinbot.chat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.dns.minidns.MiniDnsResolver;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Route;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatManager {

	private ChatWatchDogThread watchdog;
	
	private ChatListener listener;
	
	private AbstractXMPPConnection chatConnection;
	private JSONObject codingamer;
	private MultiUserChat chat;
	private String channel;
	private int id = -1;
	
	private Throwable cause, rootCause;
	
	private ScheduledExecutorService executor;
	
	@SuppressWarnings("deprecation")
	public ChatManager(JSONObject config) {
		try {
			String email = config.getString("email");
			String password = config.getString("password");
			channel = config.optString("channel", "world");
			
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
			codingamer = info.getJSONObject("codinGamer");
			id = codingamer.getInt("userId");
			System.out.println("ID: "+id);
			SmackConfiguration.setDefaultPacketReplyTimeout(15000);
			XMPPTCPConnectionConfiguration chatConfig = XMPPTCPConnectionConfiguration.builder()
					.setUsernameAndPassword(String.valueOf(id), password)
					.setXmppDomain("chat.codingame.com")
					.setConnectTimeout(15*1000)
					.build();
			listener = new ChatListener(this);
			chatConnection = new XMPPTCPConnection(chatConfig);
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(watchdog = new ChatWatchDogThread(this), 0, 1, TimeUnit.MINUTES);
		}
		catch(Exception e) {
			cause = e;
			rootCause = e;
			while(rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}
			e.printStackTrace();
		}
	}
	public AbstractXMPPConnection getConnection() {
		return chatConnection;
	}
	public MultiUserChat getChat() {
		return chat;
	}
	public JSONObject getUser() {
		return codingamer;
	}
	public int getUserID() {
		return id;
	}
	public String getChannel() {
		return channel;
	}
	public ChatWatchDogThread getWatchDog() {
		return watchdog;
	}
	protected void setChat(MultiUserChat chat) {
		this.chat = chat;
	}
	public ChatListener getListener() {
		return listener;
	}
	public void setListener(ChatListener listener) {
		this.listener = listener;
	}
	public Throwable getFailureCause() {
		return cause;
	}
	public Throwable getRootFailureCause() {
		return rootCause;
	}
}
