package com.carlncarl.isi.project;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

public class FreebaseManager {
	public static String API_KEY = "AIzaSyAu403usc7kB8fyEu9ZJDmbGrKaUU-P2KA";

	public ArrayList<String> searchInFreebase(String name, String t) {
		try {
			String type = null;
			String[] result = new String[2];
			String resultQuery = null;
			
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/mqlread");
			
			switch (t) {
			case "r":
				type = "/film/director";
				result[0] = "/film/director/film";
				resultQuery = "\"" + result[0] + "\": []";
				break;
			case "R":
				type = "/film/film";
				result[0] = "/film/film/directed_by";
				resultQuery = "\"" + result[0] + "\": []";
				break;
			case "T":
				url.put("lang", "/lang/pl");
				type = "/film/film_genre";
				result[0] = "/film/film_genre/films_in_this_genre";
				resultQuery = "\"" + result[0] + "\": []";
				break;
			case "t":
				url.put("lang", "/lang/pl");
				type = "/film/film";
				result[0] = "/film/film/genre";
				resultQuery = "\"" + result[0] + "\": []";
				break;
			case "W":
				type = "/film/film";
				result[0] = "starring";
				result[1] = "actor";
				resultQuery = "\"starring\": [{\"actor\": null}]";
				break;
			case "w":
				type = "/film/actor";
				result[0] = "/film/actor/film";
				result[1] = "film";
				resultQuery = "\"/film/actor/film\": [{\"film\": null}]";
				break;
			default:
				break;
			}

			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();

			String query = "[{\"id\":null,\"name\":\"" + name
					+ "\",\"type\":\"" + type + "\"," + resultQuery + "}]";
			
			url.put("query", query);
			//url.put("lang", "/lang/pl");
			url.put("key", API_KEY);
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONArray results = (JSONArray) response.get("result");
			ArrayList<String> values = new ArrayList<>();

			for (Object obj : results) {
				JSONArray value = JsonPath.read(obj, "$." + result[0]);

				for (Object object : value) {
					if (object == null) {
						continue;
					}
					if (result[1] != null) {
						values.add((String) ((JSONObject) object).get(result[1]));
					} else {
						values.add((String) object);
					}
				}
				break;
			}

			return values;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}