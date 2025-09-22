package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handles conversion between Minecraft usernames and UUIDs using Mojang APIs.
 *
 * This class provides methods to convert between Minecraft usernames and their
 * corresponding UUIDs using official Mojang API endpoints. It includes fallback
 * mechanisms using multiple API endpoints to ensure reliability even when some
 * services are unavailable.
 *
 * Key features:
 *   Username to UUID conversion with primary and fallback endpoints
 *   UUID to current username conversion
 *   Proper error handling for network failures and invalid names/UUIDs
 *   JSON response parsing with appropriate error codes
 *
 * API Endpoints Used:
 *   Primary: api.minecraftservices.com (Microsoft endpoint)
 *   Fallback: api.mojang.com (Legacy Mojang endpoint)
 *
 */
public class UsernameAndUUIDConverter {

    /** Primary API endpoint for username to UUID conversion (Microsoft services) */
    private static final String MOJANG_API_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/"; //"https://api.mojang.com/users/profiles/minecraft/"

    /** Legacy API endpoint for UUID to username conversion */
    private static final String MOJANG_API_URL_OLD = "https://api.mojang.com/user/profile";

    /**
     * Constructs a new UsernameAndUUIDConverter instance.
     *
     * No initialization is required as all methods are self-contained
     * and manage their own HTTP connections.
     */
    public UsernameAndUUIDConverter() {
    }

    /**
     * Reads the complete response body from an established HTTP connection.
     *
     * This utility method handles the buffered reading of HTTP response data
     * and properly closes resources. It's used internally by all API request
     * methods to extract response content.
     *
     * @param connection the established HTTP connection to read from
     * @return the complete response body as a string
     * @throws IOException if an error occurs while reading the response stream
     */
    private String getResponseBody (HttpURLConnection connection) throws IOException {
        StringBuilder responseBody = new StringBuilder();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        }
        reader.close();
        //System.out.println(responseBody); //addition
        return responseBody.toString();
    }

    /**
     * Converts a Minecraft username to its corresponding UUID using the primary API.
     *
     * This method uses the Microsoft Minecraft Services API to retrieve the UUID
     * for a given username. This is the primary method for username-to-UUID conversion
     * and should be preferred when available.
     *
     * Response handling:
     *   HTTP 200: Returns the UUID string
     *   HTTP 404: Returns empty string (username not found)
     *   Other HTTP codes: Returns empty string and logs error
     *   Network errors: Returns null
     *
     * @param username the Minecraft username to convert (case-insensitive)
     * @return the UUID string without hyphens, empty string if username invalid, null for network errors
     */
    public String getUuidFromUsername(String username){
        try {
            String apiUrl = MOJANG_API_URL  + username;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = getResponseBody(connection);
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                return jsonObject.get("id").getAsString();
            } else {
                System.out.println("Mojang API Error for username " + username +": " + responseCode);
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a Minecraft username to UUID using the fallback API endpoint.
     *
     * This method provides a fallback option when the primary API is unavailable
     * or fails. It uses the legacy Mojang API endpoint which may have different
     * rate limiting or availability characteristics.
     *
     * This should be used as a secondary option when getUuidFromUsername() fails
     * or returns an empty result, providing additional reliability for the conversion
     * process
     *
     * @param username the Minecraft username to convert (case-insensitive)
     * @return the UUID string without hyphens, empty string if username invalid, null for network errors
     */
    public String getUuidFromUsername2(String username){
        try {
            String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = getResponseBody(connection);
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                return jsonObject.get("id").getAsString();
            } else {
                System.out.println("Mojang API Error for username " + username +": " + responseCode);
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a UUID to the current Minecraft username for that account.
     *
     * This method retrieves the current username associated with a given UUID.
     * This is useful for getting up-to-date player names, especially when players
     * have changed their usernames since being added to the tracking list.
     *
     * The method uses the legacy Mojang API which provides profile information
     * including the current username for a UUID.
     *
     * Response handling:
     *   HTTP 200: Returns the current username
     *   HTTP 404: UUID not found or invalid
     *   Other HTTP codes: Logs error and returns null
     *   Network errors: Returns null
     *
     * @param uuid the UUID to convert to a username (should be without hyphens)
     * @return the current username for the UUID, or null if not found or network error occurred
     */
    public String getUsernameFromUuid(String uuid){
        try {
            String apiUrl = MOJANG_API_URL_OLD + "/" + uuid;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = getResponseBody(connection);
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                return jsonObject.get("name").getAsString();
            } else {
                System.out.println("Mojang API Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}