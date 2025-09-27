package org.example;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class providing helper methods for various common operations.
 *
 * This class contains static utility methods for:
 *   Converting between Minecraft usernames and UUIDs
 *   Reading player lists from configuration files
 *   Determining player rank information
 *   Extracting cosmetic information for MVP++ players
 *
 * All methods respect Mojang API rate limits by including appropriate delays
 * between requests (1.2 seconds per request to stay within the 600 requests per
 * 10 minutes limit).
 *
 */
public class MiscHelper {

    /**
     * Converts an array of Minecraft usernames to their corresponding UUIDs.
     *
     * This method processes each username sequentially, converting them to UUIDs
     * via the Mojang API. It includes built-in rate limiting (1.2 second delays)
     * and retry logic for failed requests. If the primary API fails, it attempts
     * the secondary API endpoint before giving up.
     *
     * Rate limiting: Respects Mojang's 600 requests per 10 minutes limit by
     * waiting 1.2 seconds between each request.
     *
     * @param igns array of Minecraft usernames to convert
     * @param uuids the UsernameAndUUIDConverter instance to use for API calls
     * @return array of UUIDs corresponding to the input usernames, or null if API connection failed
     * @throws InterruptedException if the thread is interrupted during rate limiting delays
     */
    public static String[] listUsernamesToListUUIDs(String[] igns, UsernameAndUUIDConverter uuids) throws InterruptedException {
        String[] output = new String[igns.length];
        for (int i = 0; i < output.length; i++) {
            String uuid = "";
            while (uuid.isEmpty()) {
                uuid = uuids.getUuidFromUsername(igns[i]);
                if (uuid == null) {
                    return null;
                }
                if (uuid.isEmpty()) {
                    uuid = uuids.getUuidFromUsername2(igns[i]);
                    if (uuid == null) {
                        return null;
                    }
                    if (uuid.isEmpty()) {
                        Thread.sleep(9000);
                    }
                    Thread.sleep(1200);
                }
            }
            output[i] = uuid;
            // Rate limit: 600 requests per 10 minutes (1 request every 1 second)
            Thread.sleep(1200);
        }
        return output;
    }

    /**
     * Converts an array of UUIDs to their corresponding Minecraft usernames.
     *
     * This method processes each UUID sequentially, converting them to current
     * usernames via the Mojang API. It includes the same rate limiting and retry
     * logic as the username-to-UUID conversion method.
     *
     * Rate limiting: Respects Mojang's 600 requests per 10 minutes limit by
     * waiting 1.2 seconds between each request.
     *
     * @param igns array of UUIDs to convert to usernames
     * @param uuids the UsernameAndUUIDConverter instance to use for API calls
     * @return array of current usernames corresponding to the input UUIDs, or null if API connection failed
     * @throws InterruptedException if the thread is interrupted during rate limiting delays
     */
    public static String[] listUUIDsToListUsernames(String[] igns, UsernameAndUUIDConverter uuids) throws InterruptedException {
        String[] output = new String[igns.length];
        for (int i = 0; i < output.length; i++) {
            String uuid = "";
            while (uuid.isEmpty()) {
                uuid = uuids.getUsernameFromUuid(igns[i]);
                if (uuid == null) {
                    return null;
                }
                if (uuid.isEmpty()) {
                    uuid = uuids.getUsernameFromUuid(igns[i]);
                    if (uuid == null) {
                        return null;
                    }
                    if (uuid.isEmpty()) {
                        Thread.sleep(9000);
                    }
                    Thread.sleep(1200);
                }
            }
            output[i] = uuid;
            // Rate limit: 600 requests per 10 minutes (1 request every 1 second)
            Thread.sleep(1200);
        }
        return output;
    }

    /**
     * Reads a list of player identifiers from a text file.
     *
     * This method reads a text file containing player identifiers (either usernames
     * or UUIDs) with one identifier per line. It automatically trims whitespace and
     * handles empty lines gracefully. This is typically used to load the initial
     * list of players to track from the configuration file.
     *
     * @param fileName the path to the file containing player identifiers
     * @return array of player identifiers read from the file, empty array if file not found or empty
     */
    public static String[] readPlayerUsernames(String fileName) {
        ArrayList<String> usernames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                usernames.add(line.trim()); // Add each line to the list after trimming whitespace
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the ArrayList to a String array
        return usernames.toArray(new String[0]);
    }

    /**
     * Determines if a player has the MVP++ (SUPERSTAR) rank on Hypixel.
     *
     * This method checks the player's current monthly package rank to determine
     * if they have MVP++ status. MVP++ players have access to additional features
     * like nickname functionality and exclusive cosmetics that are relevant for
     * tracking purposes.
     *
     * The method specifically looks for the "SUPERSTAR" value in the
     * "monthlyPackageRank" field, which corresponds to MVP++ rank on Hypixel.
     *
     * @param playerStats JsonObject containing the player's complete Hypixel statistics
     * @return true if the player has MVP++ rank, false otherwise
     */
    public static boolean hasMvpPlusPlus(JsonObject playerStats) {
        // Check if the JsonObject contains a "rank" field
        if (playerStats.getAsJsonObject("player").has("monthlyPackageRank")) {
            // Retrieve the value of the "rank" field
            String rank = playerStats.getAsJsonObject("player").get("monthlyPackageRank").getAsString();

            // Check if the rank is "MVP++"
            return rank.equals("SUPERSTAR");
        }

        // If the "rank" field is not present, return false
        return false;
    }

    /**
     * Extracts and formats SkyWars cosmetic information for MVP++ players.
     *
     * This method retrieves all active SkyWars cosmetics for a player and formats
     * them into a space-separated string. This information is useful for identifying
     * players in-game when they are using nicknames, as cosmetics remain visible
     * and can serve as a unique identifier.
     *
     * Extracted cosmetics include:
     *   Active cage skin
     *   Active balloon
     *   Active projectile trail
     *   Active kill effect
     *   Active kill messages
     *   Active victory dance
     *   Active death cry
     *
     * This method should only be called for players confirmed to have MVP++ rank,
     * as cosmetic information is only meaningful for players who can use nicknames.
     *
     * @param playerStats JsonObject containing the player's complete Hypixel statistics
     * @return formatted string of active cosmetics, empty string if no cosmetics found
     */
    public static String generateSwCosmetics(JsonObject playerStats) {
        //gets player.stats.SkyWars.active_cage, player.stats.SkyWars.active_balloon, etc and
        //appends them all into one string. The output isn't pretty, and can be improved by
        //processing all the cosmetics by their unique names, but there are ~300 of them.
        JsonObject player = playerStats.getAsJsonObject("player");
        if (player == null) {
            return "";
        }
        JsonObject stats = player.getAsJsonObject("stats");
        if (stats == null) {
            return "";
        }
        JsonObject skyWars = stats.getAsJsonObject("SkyWars");
        if (skyWars == null) {
            return "";
        }

        String[] cosmeticFields = {
                "active_cage",
                "active_balloon",
                "active_projectiletrail",
                "active_killeffect",
                "active_killmessages",
                "active_victorydance",
                "active_deathcry"
        };

        StringBuilder sb = new StringBuilder();
        for (String field : cosmeticFields) {
            if (skyWars.has(field)) {
                sb.append(skyWars.get(field).getAsString()).append(" ");
            }
        }
        return sb.toString().trim();
    }
}