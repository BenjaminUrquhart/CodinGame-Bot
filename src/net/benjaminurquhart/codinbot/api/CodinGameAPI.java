package net.benjaminurquhart.codinbot.api;

import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.entities.*;
import net.benjaminurquhart.codinbot.api.enums.Route;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CodinGameAPI {

	public static final String BASE_PATH = "https://www.codingame.com/services/";
	
	public static final OkHttpClient CLIENT = new OkHttpClient();
	public static final CodinGameAPI API = new CodinGameAPI();
	
	private CodinGameAPI() {}
	
	public List<CodinGamer> getUsersByName(String name) {
		try {
			JSONArray response = getJSONArray(Route.SEARCH, new JSONArray().put(name).put("en"));
			return response.toList().stream()
									.map(Map.class::cast)
									.filter(json -> json.get("type").equals("USER"))
									.map(json -> new CodinGamer((String)json.get("name"),(String)json.get("id"),String.valueOf(json.get("imageBinaryId"))))
									.collect(Collectors.toList());
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public CodinGamer getUserByhandle(String handle) {
		try {
			JSONObject json = getJSONObject(Route.GET_POINTS_BY_HANDLE, new JSONArray().put(handle));
			json = json.getJSONObject("codingamer");
			return new CodinGamer((String)json.get("name"),(String)json.get("id"),String.valueOf(json.get("imageBinaryId")));
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public List<Puzzle> getPuzzlesByName(String name) {
		try {
			JSONArray response = getJSONArray(Route.SEARCH, new JSONArray().put(name).put("en"));
			return response.toList().stream()
									.map(Map.class::cast)
									.filter(json -> json.get("type").equals("PUZZLE"))
									.map(json -> new Puzzle(new JSONObject(json)))
									.collect(Collectors.toList());
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public Contest getNextContest() {
		try {
			JSONObject basic = getJSONObject(Route.GET_NEXT_CONTEST_ID, new JSONArray());
			JSONObject full = getJSONObject(Route.GET_CONTEST_BY_ID, new JSONArray().put(basic.getString("publicId")).put(JSONObject.NULL));
			full = full.getJSONObject("challenge");
			full.put("imageBinaryId", full.optLong("cover1Id", basic.optLong("coverId", -1)));
			full.put("id", basic.getString("publicId"));
			full.put("name", full.getString("title"));
			full.put("level", full.getString("type"));
			return new Contest(full);
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public List<Clash> getPendingClashes() {
		try {
			JSONArray array = getJSONArray(Route.GET_PENDING_CLASHES, new JSONArray());
			return array.toList().stream()
								 .map(Map.class::cast)
								 .map(JSONObject::new)
								 .map(Clash::new)
								 .collect(Collectors.toList());
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public JSONArray getJSONArray(Route route, JSONArray data) throws IOException {
		Response response = makeRequest(route, data.toString());
		String str = response.body().string();
		//System.err.println(str);
		return new JSONArray(str);
	}
	public JSONObject getJSONObject(Route route, JSONArray data) throws IOException {
		Response response = makeRequest(route, data.toString());
		String str = response.body().string();
		//System.err.println(str);
		return new JSONObject(str);
	}
	
	private Response makeRequest(Route route, String data) throws IOException {
		//System.err.println(route.getMethod()+" "+route);
		//System.err.println(data);
		@SuppressWarnings("deprecation")
		Request request = new Request.Builder()
				.url(route.toString())
				.addHeader("Content-Type", "application/json")
				.method(route.getMethod(),RequestBody.create(MediaType.parse("application/json"), data))
				.build();
		
		Response response = CLIENT.newCall(request).execute();
		return response;
	}
}
