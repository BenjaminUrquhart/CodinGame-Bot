package net.benjaminurquhart.codinbot.chat;

import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.security.auth.login.LoginException;

import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.APIException;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.enums.Route;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CodinGameChatListener extends WebSocketListener {
	
	private String chatToken;
	private boolean authed;
	
	public CodinGameChatListener(String email, String pass) {
		try {
			JSONArray info = new JSONArray();
			info.put(email);
			info.put(pass);
			info.put(true);
			JSONObject json = CodinGameAPI.API.getJSONObject(Route.LOGIN, info);
			if(json.has("chatToken")) {
				this.chatToken = json.getString("chatToken");
			}
			else {
				System.err.println(json);
				throw new LoginException("Invalid credentials");
			}
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	private String genRandID() {
		Random rand = ThreadLocalRandom.current();
		long r1 = rand.nextLong() % 0xFFFFFFFF;
		long r2 = rand.nextLong() % 0xFFFF;
		long r3 = rand.nextLong() % 0xFFFF;
		long r4 = rand.nextLong() % 0xFFFF;
		long r5 = rand.nextLong() % 0xFFFFFFFFFFFFL;
		String id = String.format("%8x-%4x-%4x-%4x-%12x", r1,r2,r3,r4,r5).replace(" ", "0");
		return id;
	}
	@Override
	public void onOpen(WebSocket ws, Response response) {
		System.out.println("Open: "+response);
		ws.send("<open xmlns=\"urn:ietf:params:xml:ns:xmpp-framing\" version=\"1.0\" xml:lang=\"en\" to=\"chat.codingame.com\"/>");
	}
	@Override
	public void onMessage(WebSocket ws, String text) {
		System.out.println("Received: "+text);
		if(!authed && text.startsWith("<stream:features")) {
			String b64 = new String(Base64.getEncoder().encodeToString(chatToken.getBytes()));
			ws.send("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">"+b64+"</auth>");
			authed = true;
		}
		else if(authed && text.startsWith("<stream:features")) {
			ws.send(
					"<iq xmlns=\"jabber:client\" type=\"set\" id=\""
					+genRandID()
					+"\"><bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>CGWebchat-"
					+String.format("%10x", ThreadLocalRandom.current().nextLong() % 0xFFFFFFFFFFL).replace(" ", "0")
					+"</resource></bind></iq>"
			);
		}
		else if(text.equals("<success xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>")) {
			ws.send("<open xmlns=\"urn:ietf:params:xml:ns:xmpp-framing\" version=\"1.0\" xml:lang=\"en\" to=\"chat.codingame.com\"/>");
		}
		else if(text.startsWith("<message")) {
			String message = text.split("<body>")[1].split("</body>")[0];
			String author = text.split("<NICKNAME>")[1].split("</NICKNAME>")[0];
			System.out.printf("[%s]: %s\n",author,message);
		}
	}
	@Override
	public void onClosing(WebSocket ws, int code, String reason) {
		System.out.println(code+": "+reason);
	}
	@Override
	public void onFailure(WebSocket ws, Throwable t, Response response) {
		System.out.println("Failure: "+response);
		t.printStackTrace();
	}
}
