package net.benjaminurquhart.codinbot;

import java.io.File;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import net.benjaminurquhart.jch.CommandHandler;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.utils.cache.CacheFlag;

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
