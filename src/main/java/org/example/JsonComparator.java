package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles comparison between JSON objects representing player statistics.
 * This class is responsible for detecting changes in player data by comparing
 * new statistics with previously stored data, and tracking those differences.
 *
 */
public class JsonComparator {

    /** Gson instance for JSON serialization and deserialization */
    private final Gson gson = new Gson();

    /**
     * Compares new JSON data with old JSON data from a file and returns differences.
     * Creates the old JSON file if it doesn't exist. If differences are found,
     * returns a list of change descriptions.
     *
     * @param newJson The new JSON object containing current player statistics
     * @param oldJsonFileName The filename containing the old JSON data to compare against
     * @return List of strings describing the differences found, empty list if no differences
     */
    private List<String> compareAndReturnDifferences(JsonObject newJson, String oldJsonFileName) {
        File file = new File(oldJsonFileName);
        if (!file.exists()) {
            try {
                System.out.println("Created new file name");
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try (FileReader fileReader = new FileReader(oldJsonFileName)) {

                JsonParser jsonParse = new JsonParser();
                JsonObject oldJson = jsonParse.parse(fileReader).getAsJsonObject();
                if (oldJson.size() == 0) {
                    //new user added, send some message like it
                    //old json updated automatically from caller function
                    //theoretically should never get called because of the check above this try
                } else if (!newJson.equals(oldJson)) {
                    List<String> diff = getDifferences(oldJson, newJson, "");
                    return diff;
                } else {
                    //no games played, no actions to be done
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<String>(); //empty
    }

    /**
     * Recursively compares two JSON objects and identifies all differences.
     * Handles nested JSON objects by recursively calling itself. Tracks additions,
     * modifications, and removals of fields.
     *
     * @param oldJson The original JSON object for comparison
     * @param newJson The new JSON object to compare against the old one
     * @param parentKey The parent key path for nested objects (used for full field paths)
     * @return List of difference descriptions with prefixed codes:
     *         "1" for new fields, "2" for changed fields, "3" for removed fields
     */
    private List<String> getDifferences(JsonObject oldJson, JsonObject newJson, String parentKey) {
        List<String> differences = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : newJson.entrySet()) {
            String key = entry.getKey();
            JsonElement newValue = entry.getValue();
            JsonElement oldValue = oldJson.get(key);
            String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;

            if (oldValue == null) {
                differences.add("1New field added: " + fullKey + " = " + newValue);
            } else if (!newValue.equals(oldValue)) {
                if (newValue.isJsonObject() && oldValue.isJsonObject()) {
                    differences.addAll(getDifferences(oldValue.getAsJsonObject(), newValue.getAsJsonObject(), fullKey));
                } else {
                    differences.add("2" + fullKey);
                }
            }
        }

        for (Map.Entry<String, JsonElement> entry : oldJson.entrySet()) {
            String key = entry.getKey();
            if (!newJson.has(key)) {
                String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;
                differences.add("3Field removed: " + fullKey);
            }
        }

        return differences;
    }

    /**
     * Debug method for printing differences between two JSON objects to console.
     * Recursively compares JSON objects and prints human-readable difference descriptions.
     * This method is primarily used for development and troubleshooting.
     *
     * @param oldJson The original JSON object for comparison
     * @param newJson The new JSON object to compare against the old one
     * @param parentKey The parent key path for nested objects (used for full field paths)
     */
    private void printDifferences(JsonObject oldJson, JsonObject newJson, String parentKey) {
        for (Map.Entry<String, JsonElement> entry : newJson.entrySet()) {
            String key = entry.getKey();
            JsonElement newValue = entry.getValue();
            JsonElement oldValue = oldJson.get(key);
            String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;

            if (oldValue == null) {
                System.out.println("New field added: " + fullKey + " = " + newValue);
            } else if (!newValue.equals(oldValue)) {
                if (newValue.isJsonObject() && oldValue.isJsonObject()) {
                    printDifferences(oldValue.getAsJsonObject(), newValue.getAsJsonObject(), fullKey);
                } else {
                    System.out.println("Field changed: " + fullKey + " (old: " + oldValue + ", new: " + newValue + ")");
                }
            }
        }

        for (Map.Entry<String, JsonElement> entry : oldJson.entrySet()) {
            String key = entry.getKey();
            if (!newJson.has(key)) {
                String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;
                System.out.println("Field removed: " + fullKey);
            }
        }
    }

    /**
     * Updates the old JSON file with new JSON data.
     * Overwrites the existing file with the new JSON object, effectively
     * updating the baseline for future comparisons.
     *
     * @param oldJsonFileName The filename of the JSON file to update
     * @param newJson The new JSON object to write to the file
     */
    public void updateOldJson(String oldJsonFileName, JsonObject newJson) {
        try (FileWriter writer = new FileWriter(oldJsonFileName)) {
            gson.toJson(newJson, writer);
            System.out.println("Filename: " + oldJsonFileName + " should have been updated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Default constructor for JsonComparator.
     * Initializes a new instance of the JSON comparison utility.
     */
    public JsonComparator() {

    }

    /**
     * Main comparison method that compares new JSON data with stored data for a specific player.
     * This is the primary entry point for detecting changes in player statistics.
     * Automatically updates the stored JSON after comparison.
     *
     * @param newJson The new JSON object containing current player statistics
     * @param playerUUID The unique identifier for the player (used for file naming)
     * @return List of strings describing detected differences in player statistics
     */
    public List<String> compare(JsonObject newJson, String playerUUID) {
        ensureDirectoryExists();

        String oldJsonFileName = "player_jsons\\" + playerUUID + "_jsondata.txt";
        List<String> diff = compareAndReturnDifferences(newJson, oldJsonFileName);
        updateOldJson(oldJsonFileName, newJson);
        return diff;
    }

    /**
     * Ensures the player_jsons directory exists, creating it if necessary.
     * This method should be called before any file operations to prevent
     * FileNotFoundException when the directory doesn't exist.
     */
    private void ensureDirectoryExists() {
        File directory = new File("player_jsons");
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created player_jsons directory");
            } else {
                System.err.println("Failed to create player_jsons directory");
            }
        }
    }

}