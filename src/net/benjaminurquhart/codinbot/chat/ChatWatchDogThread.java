package net.benjaminurquhart.codinbot.chat;

import java.security.cert.CertificateExpiredException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONObject;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

public class ChatWatchDogThread extends Thread {
	
	private ChatManager manager;
	private Throwable root, cause;
	
	private volatile boolean running;
	
	protected ChatWatchDogThread(ChatManager manager) {
		this.manager = manager;
	}
	@Override
	public void run() {
		if(running) {
			return;
		}
		AbstractXMPPConnection chatConnection = manager.getConnection();
		JSONObject codingamer = manager.getUser();
		MultiUserChat chat = manager.getChat();
		String channel = manager.getChannel();
		try {
			if(chatConnection.isAuthenticated() && chat != null && chat.isJoined()) {
				return;
			}
			running = true;
			EntityBareJid channelID = (EntityBareJid) JidCreate.bareFrom(channel+"@conference.codingame.com");
			Resourcepart nick = Resourcepart.from(codingamer.getString("pseudo"));
			if(!chatConnection.isConnected()) {
				chatConnection.connect().login();
				System.out.printf("Logged into CodinGame chat as %s (%d)\n", codingamer.getString("pseudo"), this.manager.getUserID());
				if(chat != null) {
					chat.leaveSync();
				}
			}
			if(chat != null && !chat.isJoined()) {
				System.out.println("Attempting to rejoin channel #"+channel+"...");
				chat.createOrJoin(nick);
				System.out.println("Rejoined channel #"+channel);
				running = false;
				return;
			}
			if(chat == null) {
				MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatConnection);
				chat = manager.getMultiUserChat(channelID);
				chat.createOrJoin(nick);
				this.manager.setChat(chat);
				System.out.println("Joined channel #"+channel);
				chat.addMessageListener((message) -> {
					if(message == null || message.getBody() == null) {
						System.err.println("Received null message ("+message+")");
						return;
					}
					String from = message.getFrom() == null ? "???" : message.getFrom().getResourceOrEmpty().toString();
					System.out.println(from+": "+message.getBody());
				});
				chat.addMessageListener(this.manager.getListener());
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						System.out.println("Shutting down!");
						AbstractXMPPConnection conn = this.manager.getConnection();
						MultiUserChat chatroom = this.manager.getChat();
						if(chatroom != null) {
							chatroom.leaveSync();
						}
						if(conn != null) {
							conn.disconnect();
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}));
				System.out.println("Added listeners");
			}
		}
		catch(Exception e) {
			cause = e;
			root = e;
			while(root.getCause() != null) {
				root = root.getCause();
			}
			if(root instanceof CertificateExpiredException) {
				System.err.println("Expired certificateTM");
				if(chatConnection != null) {
					chatConnection.disconnect();
				}
			}
			else {
				System.err.println("Exception in WatchDog thread:");
				e.printStackTrace();
			}
		}
		running = false;
	}
	public Throwable getFailureCause() {
		return cause;
	}
	public Throwable getRootFailureCause() {
		return root;
	}
}
