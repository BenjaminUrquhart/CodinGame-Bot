package net.benjaminurquhart.codinbot.commands;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.FileOutputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Eval extends Command<CodinBot> {

	private ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
	
	
	private String eval(GuildMessageReceivedEvent event, CodinBot self) throws ScriptException{
		se.put("api", CodinGameAPI.API);
		
		se.put("channel", event.getChannel());
		se.put("guild", event.getGuild());
		se.put("msg", event.getMessage());
		se.put("user", event.getAuthor());
		se.put("jda", event.getJDA());
		se.put("event", event);
		se.put("self", self);
		String toEval = event.getMessage().getContentRaw().split(" ", 3)[2];
		return String.valueOf(se.eval(toEval));
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		if(event.getAuthor().getId().equals("273216249021071360")){
			try{
				String out = eval(event, self);
				if(out.length() > 1990){
					File file = new File("outputcg.txt");
					if(file.exists()){
						file.delete();
					}
					file.createNewFile();
					FileOutputStream stream = new FileOutputStream(file);
					stream.write(out.getBytes());
					stream.close();
					channel.sendFile(file).queue();
				}
				else{
					channel.sendMessage("```" + out + "```").queue();
				}
			}
			catch(Exception e){
				e.printStackTrace();
				channel.sendMessage(e.toString()).queue();
			}
		}
	}
	@Override
	public boolean hide(){
		return true;
	}
}
