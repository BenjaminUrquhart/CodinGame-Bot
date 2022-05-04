package net.benjaminurquhart.codinbot.chat;

import java.util.List;
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
import net.benjaminurquhart.codinbot.api.entities.CodinGamer;
import net.benjaminurquhart.codinbot.api.enums.Route;

public class ChatManager {

	private ChatWatchDogThread watchdog;
	
	private ChatListener listener;
	
	private AbstractXMPPConnection chatConnection;
	private CodinGamer codingamer;
	private MultiUserChat chat;
	private String channel;
	private int id = -1;
	
	private Throwable cause, rootCause;
	
	private ScheduledExecutorService executor;
	
	@SuppressWarnings("deprecation")
	public ChatManager(JSONObject config) {
		try {
			JSONObject info = null;
			JSONArray requestData = null;
			String password = config.getString("password");
			channel = config.optString("channel", "world");
			
			MiniDnsResolver.setup();
			
			// Numerical user id isn't supported since I can't
			// find a way to get user info just based on that.
			// Next best thing is the handle hash thing.
			if(config.has("handle")) {
				String handle = config.getString("handle");
				System.out.println("Logging in with handle " + handle);
				codingamer = CodinGameAPI.API.getUserByHandle(handle);
			}
			else if(config.has("username")) {
				String username = config.getString("username");
				System.out.println("Logging in as user " + username);
				List<CodinGamer> matches = CodinGameAPI.API.getUsersByName(username);
				
				for(CodinGamer gamer : matches) {
					if(gamer.getName().equals(username)) {
						codingamer = gamer;
						break;
					}
				}
				if(codingamer == null) {
					throw new IllegalStateException("No users matching that name");
				}
				else {
					// Users from search results have minimal information attached to them
					System.out.println("Getting user id");
					codingamer = CodinGameAPI.API.getUserByHandle(codingamer.getHandle());
				}
			}
			else {
				/*
				 * Ok so, they added a captcha to the login endpoint.
				 * This means we can't login anymore with just 
				 * email + password. 
				 * 
				 * Still gonna keep this here just in case something changes.
				 */
				String email = config.getString("email");
				System.out.println("Getting ID for user with email "+email);
				
				requestData = new JSONArray().put(email)
											 .put(password)
											 .put(true)
											 .put("CODINGAME")
											 .put(JSONObject.NULL);
				
				info = CodinGameAPI.API.getJSONObject(Route.LOGIN, requestData);
				codingamer = new CodinGamer(info);
			}
			id = codingamer.getID();
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
	public CodinGamer getUser() {
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
