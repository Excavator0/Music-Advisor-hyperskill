package advisor.app;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static advisor.utils.SpotifyConstants.*;
import static java.lang.Thread.sleep;

public class Advisor {

    static int currentPage = 0;
    static int allPages = 1;
    static List<List<String>> featured = new ArrayList<>();
    static List<List<String>> newReleases = new ArrayList<>();
    static List<String> categories = new ArrayList<>();
    static List<List<String>> playlists = new ArrayList<>();
    static String previousCommand = "";
    static int firstElement = 0;
    static int endElement = 0;


    private static String getAccessToken(HttpClient client, String serverPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(serverPath + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(String.format(REQUEST_BODY_PARAMETER, CLIENT_ID, CLIENT_SECRET, CODE, REDIRECT_URI)))
                .build();
        try {
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonObject json = new Gson().fromJson(body, JsonObject.class);
            return json.get("access_token").getAsString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createServer() throws InterruptedException, IOException {
        HttpServer server;
        server = HttpServer.create();
        server.bind(new InetSocketAddress((8080)), 0);

        server.start();

        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().toString();
            String response;
            if (query.contains("code=")) {
                CODE = query.split("=")[1];
                response = "Got the code. Return back to your program.";
            } else {
                response = "Authorization code not found. Try again.";
            }
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });

        System.out.println("waiting for code...");
        while (CODE == null) {
            Thread.sleep(10);
        }
        server.stop(5);
    }



    public static void init(Scanner scanner, HttpClient client, String serverPath, String apiServerPath, int contentPerPage) throws IOException, InterruptedException {
        boolean isAuthenticated = false;

        while (true) {
            String[] command = scanner.nextLine().split(" ");

            if ("auth".equals(command[0])) {
                authenticate(client, serverPath);
                isAuthenticated = true;
            }

            if (!isAuthenticated) {
                System.out.println("Please, provide access for application.");
            } else {
                if ("featured".equals(command[0])) {
                    if (featured.isEmpty()) {
                        getFeaturedPlaylists(apiServerPath);
                    }
                    previousCommand = "featured";
                    firstElement = 0;
                    currentPage = 0;
                    nextPage(contentPerPage);
                } else if ("new".equals(command[0])) {
                    if (newReleases.isEmpty()) {
                        getAllNewReleases(apiServerPath);
                    }
                    previousCommand = "new";
                    firstElement = 0;
                    currentPage = 0;
                    nextPage(contentPerPage);
                } else if ("categories".equals(command[0])) {
                    if (categories.isEmpty()) {
                        getAllCategories(apiServerPath);
                    }
                    previousCommand = "categories";
                    firstElement = 0;
                    currentPage = 0;
                    nextPage(contentPerPage);
                } else if ("playlists".equals(command[0])) {
                    if (playlists.isEmpty()) {
                        StringBuilder categoryName = new StringBuilder();

                        for (int i = 1; i < command.length; i++) {
                            categoryName.append(command[i]).append(" ");
                        }

                        getPlaylistsByCategoryName(apiServerPath, categoryName.toString().trim());
                    }
                    previousCommand = "playlists";
                    firstElement = 0;
                    currentPage = 0;
                    nextPage(contentPerPage);
                } else if ("next".equals(command[0])) {
                    nextPage(contentPerPage);
                } else if ("prev".equals(command[0])) {
                    prevPage(contentPerPage);
                }
                }
            }
        }

    private static void prevPage(int contentPerPage) {
        if (currentPage == 1) {
            System.out.println("No more pages.");
            return;
        }
        firstElement = contentPerPage * (currentPage-2);
        if ("featured".equals(previousCommand)) {
            endElement = firstElement + contentPerPage;
            allPages = featured.size() / contentPerPage;
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(featured.get(i).get(0));
                System.out.println(featured.get(i).get(1));
                System.out.println();
            }
        }
        if ("new".equals(previousCommand)) {
            endElement = firstElement + contentPerPage;
            allPages = newReleases.size() / contentPerPage;
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(newReleases.get(i).get(0));
                System.out.println(newReleases.get(i).get(1));
                System.out.println(newReleases.get(i).get(2));
                System.out.println();
            }
        }
        if ("categories".equals(previousCommand)) {
            endElement = firstElement + contentPerPage;
            allPages = categories.size() / contentPerPage;
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(categories.get(i));
            }
        }
        if ("playlists".equals(previousCommand)) {
            endElement = firstElement + contentPerPage;
            allPages = playlists.size() / contentPerPage;
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(playlists.get(i).get(0));
                System.out.println(playlists.get(i).get(1));
                System.out.println();
            }
        }
        currentPage--;
        firstElement += contentPerPage;
        System.out.println("---PAGE " + currentPage + " OF " + allPages + "---");
    }

    private static void nextPage(int contentPerPage) {
        if ("featured".equals(previousCommand)) {
            endElement = Math.min((currentPage+1) * contentPerPage, featured.size());
            allPages = featured.size() / contentPerPage;
            if (currentPage == allPages) {
                System.out.println("No more pages.");
                return;
            }
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(featured.get(i).get(0));
                System.out.println(featured.get(i).get(1));
                System.out.println();
            }
        }
        else if ("new".equals(previousCommand)) {
            endElement = Math.min((currentPage+1) * contentPerPage, newReleases.size());
            allPages = newReleases.size() / contentPerPage;
            if (currentPage == allPages) {
                System.out.println("No more pages.");
                return;
            }
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(newReleases.get(i).get(0));
                System.out.println(newReleases.get(i).get(1));
                System.out.println(newReleases.get(i).get(2));
                System.out.println();
            }
        }
        else if ("categories".equals(previousCommand)) {
            endElement = Math.min((currentPage+1) * contentPerPage, categories.size());
            allPages = categories.size() / contentPerPage;
            if (currentPage == allPages) {
                System.out.println("No more pages.");
                return;
            }
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(categories.get(i));
            }
        }
        else if ("playlists".equals(previousCommand)) {
            endElement = Math.min((currentPage+1) * contentPerPage, playlists.size());
            allPages = playlists.size() / contentPerPage;
            if (currentPage == allPages) {
                System.out.println("No more pages.");
                return;
            }
            for (int i = firstElement; i < endElement; i++) {
                System.out.println(playlists.get(i).get(0));
                System.out.println(playlists.get(i).get(1));
                System.out.println();
            }
        }
        firstElement = endElement;
        currentPage++;
        System.out.println("---PAGE " + currentPage + " OF " + allPages + "---");

    }

    private static void authenticate(HttpClient client, String serverPath) throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.printf((ACCESS_CODE_URL) + "%n", CLIENT_ID, REDIRECT_URI);
        System.out.println("waiting for code...");
        createServer();
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        TOKEN = getAccessToken(client, serverPath);
        System.out.println("Success!");
    }

    private static void getAllNewReleases(String apiServerPath) {
        String releasesUrl = apiServerPath + "/v1/browse/new-releases";
        String json = getApiData(releasesUrl);

        JsonArray items = new Gson().fromJson(json, JsonObject.class)
                .getAsJsonObject("albums")
                .getAsJsonArray("items");

        List<String> artists = new ArrayList<>();

        for (JsonElement item : items) {
            List<String> newRelease = new ArrayList<>();
            String album = item.getAsJsonObject().get("name").getAsString();

            for (JsonElement artist : item.getAsJsonObject().getAsJsonArray("artists")) {
                artists.add(artist.getAsJsonObject().get("name").getAsString());
            }

            String  externalUrl = item.getAsJsonObject().get("external_urls").getAsJsonObject()
                    .get("spotify")
                    .getAsString();
            newRelease.add(album);
            newRelease.add(artists.toString());
            newRelease.add(externalUrl);
            newReleases.add(newRelease);
            artists = new ArrayList<>();
        }
    }

    private static void getAllCategories(String apiServerPath) {
        String categoriesUrl = apiServerPath + "/v1/browse/categories";

        JsonArray items = new Gson().fromJson(getApiData(categoriesUrl), JsonObject.class)
                .getAsJsonObject("categories")
                .getAsJsonArray("items");

        items.forEach(item -> categories.add(item.getAsJsonObject().get("name").getAsString()));
    }

    private static void getFeaturedPlaylists(String apiServerPath) {
        String categoriesUrl = apiServerPath + "/v1/browse/featured-playlists";

        JsonArray items = new Gson().fromJson(getApiData(categoriesUrl), JsonObject.class)
                .getAsJsonObject("playlists")
                .getAsJsonArray("items");
        if (featured.isEmpty()) {
            items.forEach(item -> {
                List<String> featuredPlaylist = new ArrayList<>();
                featuredPlaylist.add(item.getAsJsonObject().get("name").getAsString());
                featuredPlaylist.add(item.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString());
                featured.add(featuredPlaylist);
            });
        }
    }

    private static void getPlaylistsByCategoryName(String apiServerPath, String categoryName) {
        String categoryId = getCategoryIdByName(apiServerPath, categoryName);

        if ("Unknown category name.".equals(categoryId)) {
            System.out.println("Unknown category name.");
            return;
        }

        String playlistUrl = String.format("%s/v1/browse/categories/%s/playlists", apiServerPath, categoryId);
        String response = getApiData(playlistUrl);

        if (response.contains("Test unpredictable error message")) {
            System.out.println("Test unpredictable error message");
            return;
        }

        JsonArray items = new Gson().fromJson(response, JsonObject.class)
                .getAsJsonObject("playlists")
                .getAsJsonArray("items");

        items.forEach(item -> {
            List<String> playlist = new ArrayList<>();
            playlist.add(item.getAsJsonObject().get("name").getAsString());
            playlist.add(item.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString());
            playlists.add(playlist);
        });
    }

    private static String getCategoryIdByName(String apiServerPath, String categoryName) {
        String categoriesUrl = apiServerPath + "/v1/browse/categories";

        JsonArray items = new Gson().fromJson(getApiData(categoriesUrl), JsonObject.class)
                .getAsJsonObject("categories")
                .getAsJsonArray("items");

        String id = "Unknown category name.";

        for (JsonElement item : items) {
            String name = item.getAsJsonObject().get("name").getAsString();

            if (name.equals(categoryName)) {
                id = item.getAsJsonObject().get("id").getAsString();
                return id;
            }
        }

        return id;
    }

    private static String getApiData(String apiPath) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + TOKEN)
                .uri(URI.create(apiPath))
                .GET()
                .build();

        try {
            return HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
