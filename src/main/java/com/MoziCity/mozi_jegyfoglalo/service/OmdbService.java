package com.MoziCity.mozi_jegyfoglalo.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OmdbService {


    private static final String API_KEY = "5ffd42bdf3f8f1c362f1957bdccde063";


    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY + "&language=hu-HU&query=";


    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public static MovieData searchMovieByTitle(String title) {
        try {

            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = SEARCH_URL + encodedTitle;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();


            JsonArray results = json.getAsJsonArray("results");

            if (results.size() == 0) {
                return null; // Nincs találat
            }


            JsonObject movie = results.get(0).getAsJsonObject();


            String description = movie.get("overview").getAsString();
            String posterPath = movie.has("poster_path") && !movie.get("poster_path").isJsonNull()
                    ? movie.get("poster_path").getAsString()
                    : null;


            if (description.isEmpty()) {
                description = "Ehhez a filmhez sajnos még nincs magyar leírás az adatbázisban.";
            }


            String fullImageUrl = "";
            if (posterPath != null) {
                fullImageUrl = IMAGE_BASE_URL + posterPath;
            }

            return new MovieData(description, fullImageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class MovieData {
        public String description;
        public String imageUrl;

        public MovieData(String description, String imageUrl) {
            this.description = description;
            this.imageUrl = imageUrl;
        }
    }
}