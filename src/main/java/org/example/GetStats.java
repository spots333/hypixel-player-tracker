package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles communication with the Hypixel API to retrieve player statistics.
 *
 * This class encapsulates all HTTP requests to the Hypixel API, managing
 * API key authentication and response parsing. It provides a simple interface
 * for fetching complete player data as JSON objects while handling common
 * error conditions like network failures and API rate limiting.
 *
 * The class respects Hypixel API guidelines:
 *   Uses proper API key authentication
 *   Handles HTTP response codes appropriately
 *   Returns null for network failures to allow retry logic
 *   Returns empty JSON objects for API errors
 *
 */
public class GetStats {
    /** The Hypixel API key used for authentication */
    private String API_KEY;

    /** Base URL for all Hypixel API requests */
    private static final String API_BASE_URL = "https://api.hypixel.net";

    /** HTTP connection instance for API requests */
    private static HttpURLConnection connection;

    /**
     * Constructs a GetStats instance with the provided Hypixel API key.
     *
     * Initializes the connection settings and prepares the instance for
     * making API requests. The API key should be obtained from the Hypixel
     * Developer Portal and have appropriate permissions.
     *
     * @param apiKey the Hypixel API key for authentication
     */
    public GetStats(String apiKey) {
        this.API_KEY = apiKey;
        initializeConnection();
    }

    /**
     * Retrieves complete player data from the Hypixel API.
     *
     * Fetches all available statistics for the specified player UUID from
     * the Hypixel API /player endpoint. This includes game statistics, rank
     * information, cosmetic data, and achievement progress across all Hypixel
     * game modes.
     *
     * Response handling:
     *   HTTP 200: Returns parsed JSON object with player data
     *   Other HTTP codes: Returns empty JSON object (indicates API key issues)
     *   Network errors: Returns null (allows for retry logic)
     *
     * @param playerUUID the UUID of the player to retrieve data for
     * @return JsonObject containing player data, empty JsonObject for API errors, or null for network failures
     */
    public JsonObject getPlayerDataJSON (String playerUUID){
        try {
            String apiUrl = API_BASE_URL + "/player?key=" + API_KEY + "&uuid=" + playerUUID;
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = getResponseBody(connection);
                JsonElement jsonElement = JsonParser.parseString(responseBody);
                return jsonElement.getAsJsonObject();
            } else {
                System.out.println("Hypixel API Error: " + responseCode);
                return new JsonObject(); //lets us know the api key is invalid
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads and returns the complete response body from an HTTP connection.
     *
     * Utility method for reading the full response content from an established
     * HTTP connection. Handles buffered reading and proper resource cleanup.
     *
     * @param connection the established HTTP connection to read from
     * @return the complete response body as a string
     * @throws IOException if an error occurs while reading the response
     */
    private static String getResponseBody (HttpURLConnection connection) throws IOException {
        StringBuilder responseBody = new StringBuilder();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        }
        reader.close();
        //System.out.println(responseBody); //used for debugging purposes
        return responseBody.toString();
    }

    /**
     * Initializes the HTTP connection with default settings.
     *
     * Sets up a base connection configuration that can be reused for multiple
     * API requests. This helps maintain consistent connection settings and
     * reduces setup overhead for subsequent requests.
     */
    private void initializeConnection() {
        try {
            String apiUrl = API_BASE_URL + "/player?key=" + API_KEY;
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}