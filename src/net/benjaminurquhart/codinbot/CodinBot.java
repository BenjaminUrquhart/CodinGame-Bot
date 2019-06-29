package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;

import net.benjaminurquhart.jch.CommandHandler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class CodinBot {

	private static String token;
	
	static {
		try {
			token = new String(Files.readAllBytes(new File("token.txt").toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	public static void main(String[] args) throws Exception {
		JDA jda = new JDABuilder(token).build().awaitReady();
		jda.addEventListener(new CommandHandler<CodinBot>(new CodinBot(),jda.getSelfUser().getAsMention()+" ",null,"net.benjaminurquhart.codinbot.commands"));
	}
}
