package net.benjaminurquhart.codinbot.commands;

import java.awt.Color;
//import java.util.ArrayList;
//import java.util.HashSet;
import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;

//import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.CodinBot;
import net.benjaminurquhart.codinbot.api.CodinGameAPI;
import net.benjaminurquhart.codinbot.api.entities.CodinGamer;
import net.benjaminurquhart.codinbot.api.entities.UserProfile;
//import net.benjaminurquhart.codinbot.api.enums.Route;
import net.benjaminurquhart.jch.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class GetProfile extends Command<CodinBot> {

	public GetProfile() {
		super("profile","username/handle");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, CodinBot self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		channel.sendTyping().queue($-> {
			try {
				List<CodinGamer> users = null;
				CodinGamer user = null;
				try {
					users = CodinGameAPI.API.getUsersByName(args[2]);
					if(users.size()>1) {
						for(CodinGamer cg : users) {
							if(cg.getName().equals(args[2])) {
								user = cg;
								break;
							}
						}
						if(user == null) {
							for(CodinGamer cg : users) {
								if(cg.getName().equalsIgnoreCase(args[2])) {
									user = cg;
									break;
								}
							}
						}
						if(user == null) user = users.get(0);
					}
					else {
						user = users.get(0);
					}
				}
				catch(Exception e) {
					try {
						user = CodinGameAPI.API.getUserByhandle(args[2]);
					}
					catch(Exception exec) {
						exec.printStackTrace();
						channel.sendMessage("Unknown username/handle").queue();
						return;
					}
				}
				UserProfile profile = user.getProfile();
				EmbedBuilder eb = new EmbedBuilder();
				JSONObject json = profile.getJSON();
				int rank = json.getJSONObject("codingamePointsRankingDto").getInt("codingamePointsRank");
				String title = "";
				if(rank<=100) {
					title = "Guru";
				}
				else if(rank<=500) {
					title = "Grand Master";
				}
				else if(rank<=2500) {
					title = "Master";
				}
				else if(rank<=5000) {
					title = "Mentor";
				}
				else if(rank<=10000) {
					title = "Disciple";
				}
				else if(rank<=20000) {
					title = "Craftsman";
				}
				else {
					title = "Rookie";
				}
				//System.err.println(user.getImageUrl());
				
				JSONObject gamer = json.getJSONObject("codingamer");
				int level = gamer.getInt("level");
				eb.setColor(Color.GREEN);
				if(level > 9) {
					eb.setColor(Integer.parseInt("995734",16));
				}
				if(level > 19) {
					eb.setColor(Integer.parseInt("969493",16));
				}
				if(level > 29) {
					eb.setColor(Integer.parseInt("d4a200",16));
				}
				if(level > 39) {
					eb.setColor(Color.RED);
				}
				eb.setThumbnail(user.getImageUrl());
				eb.setAuthor(user.getName(),"https://www.codingame.com/profile/"+user.getHandle());
				eb.setDescription(gamer.has("tagline")?gamer.getString("tagline"):"*this user has not set a tagline*");
				eb.addField("Level", String.valueOf(level), true);
				eb.addField("Rank", rank+" ("+title+")", true);
				eb.addField("Country", gamer.getString("countryId"), true);
				eb.addField("Bio",gamer.has("biography")?gamer.getString("biography"):"*this user has not created a biography*", false);
				
				/*
				JSONArray achievements = CodinGameAPI.getJSONArray(Route.GET_BEST_ACHIEVEMENTS, new JSONArray().put(gamer.getInt("userId")).put(3));
				
				// Don't judge
				List<JSONObject> legend = new ArrayList<>();
				List<JSONObject> gold = new ArrayList<>();
				List<JSONObject> silver = new ArrayList<>();
				List<JSONObject> bronze = new ArrayList<>();
				
				for(Object obj : achievements) {
					json = (JSONObject) obj;
					if(json.getInt("progress")<json.getInt("progressMax")) {
						continue;
					}
					switch(json.getString("level")) {
					case "PLATINUM": legend.add(json); break;
					case "GOLD": gold.add(json); break;
					case "SILVER": silver.add(json); break;
					case "BRONZE": bronze.add(json); break;
					}
				}
				List<JSONObject> all = new ArrayList<>();
				
				all.addAll(legend);
				all.addAll(gold);
				all.addAll(silver);
				all.addAll(bronze);
				
				//all.stream().map(j->j.getString("title")+": "+j.getString("description")).forEach(System.out::println);
				
				List<JSONObject> lang = new ArrayList<>();
				List<JSONObject> puzzle = new ArrayList<>();
				
				Set<String> seen = new HashSet<>();
				
				int index = 0;
				int iterations = 0;
				
				while(lang.size()<3&&iterations++<500) {
					if(index>=all.size()) break;
					if(all.get(index).getString("categoryId").equals("LANGUAGE")) {
						if(seen.add(all.get(index).getString("id").replaceAll("_\\d+$", ""))) {
							lang.add(all.get(index));
						}
					}
					index++;
				}
				iterations = 0;
				index = 0;
				seen.clear();
				while(puzzle.size()<3&&iterations++<500) {
					if(index>=all.size()) break;
					if(all.get(index).getString("categoryId").equals("BEST")) {
						if(seen.add(all.get(index).getString("id").replaceAll("_\\d+$", ""))) {
							puzzle.add(all.get(index));
						}
					}
					index++;
				}
				eb.addField("Best language achievements", 
						    lang.stream()
						    	.map(j->j.getString("level")+": **"+j.getString("title")+"**:\n"+j.getString("description"))
						    	.collect(Collectors.joining("\n"))
						    ,false);
				eb.addField("Best puzzle achievements", 
					    puzzle.stream()
					    	  .map(j->j.getString("level")+": **"+j.getString("title")+"**:\n"+j.getString("description"))
					    	  .collect(Collectors.joining("\n"))
					    ,false);*/
				(users!=null&&users.size()>1?channel.sendMessage("Warning: "+users.size()+" users share a similar username. Showing the one with matching capitalization/first in the list").embed(eb.build()):channel.sendMessage(eb.build())).queue();
			}
			catch(Exception e) {
				e.printStackTrace();
				channel.sendMessage(e.toString()).queue();
			}
		});
	}
	@Override
	public String getDescription() {
		return "retrives basic info about this user";
	}
}
