package net.benjaminurquhart.codinbot.api;

import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import net.benjaminurquhart.codinbot.api.entities.CodinGamer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CodinGameAPI {

	public static final String BASE_PATH = "https://www.codingame.com/services/";
	
	public static final OkHttpClient CLIENT = new OkHttpClient();
	
	public static List<CodinGamer> getUsersByName(String name) {
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
	public static CodinGamer getUserByhandle(String handle) {
		try {
			JSONObject json = getJSONObject(Route.GET_POINTS_BY_HANDLE, new JSONArray().put(handle));
			json = json.getJSONObject("codingamer");
			return new CodinGamer((String)json.get("name"),(String)json.get("id"),String.valueOf(json.get("imageBinaryId")));
		}
		catch(Exception e) {
			throw new APIException(e);
		}
	}
	public static JSONArray getJSONArray(Route route, JSONArray data) throws IOException {
		return new JSONArray(makeRequest(route, data.toString()).body().string());
	}
	public static JSONObject getJSONObject(Route route, JSONArray data) throws IOException {
		Response response = makeRequest(route, data.toString());
		String str = response.body().string();
		//System.err.println(str);
		return new JSONObject(str);
	}
	
	private static Response makeRequest(Route route, String data) throws IOException {
		Request request = new Request.Builder()
				.url(route.toString())
				.addHeader("Content-Type", "application/json")
				.method(route.getMethod(),RequestBody.create(MediaType.parse("application/json"), data))
				.build();
		
		Response response = CLIENT.newCall(request).execute();
		return response;
	}
}
