package org.example;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for the Hypixel Tracker application.
 * Loads and validates configuration from properties file.
 */
public class Configuration {
    private static final String CONFIG_FILE = "config.properties";

    private Properties properties;
    private String hypixelApiKey;
    private String discordBotToken;
    private String playerListFilename;
    private boolean playerListContainsUsernames;
    private int checkIntervalMinutes;
    private int maxTrackedPlayers;

    /**
     * Creates and loads configuration from the default config file.
     * @throws ConfigurationException if configuration cannot be loaded or is invalid
     */
    public Configuration() throws ConfigurationException {
        loadConfiguration();
        validateConfiguration();
    }

    /**
     * Creates and loads configuration from specified file.
     * @param configFile path to configuration file
     * @throws ConfigurationException if configuration cannot be loaded or is invalid
     */
    public Configuration(String configFile) throws ConfigurationException {
        loadConfiguration(configFile);
        validateConfiguration();
    }

    private void loadConfiguration() throws ConfigurationException {
        loadConfiguration(CONFIG_FILE);
    }

    private void loadConfiguration(String configFile) throws ConfigurationException {
        properties = new Properties();

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);

            // Load all configuration values
            hypixelApiKey = properties.getProperty("hypixel.api.key", "").trim();
            discordBotToken = properties.getProperty("discord.bot.token", "").trim();
            playerListFilename = properties.getProperty("player.list.filename", "tracked_players.txt").trim();
            playerListContainsUsernames = Boolean.parseBoolean(
                    properties.getProperty("player.list.contains.usernames", "true")
            );
            checkIntervalMinutes = Integer.parseInt(
                    properties.getProperty("check.interval.minutes", "5")
            );
            maxTrackedPlayers = Integer.parseInt(
                    properties.getProperty("max.tracked.players", "300")
            );

        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Configuration file not found: " + configFile +
                    ". Please create a config.properties file with your API keys and settings.");
        } catch (IOException e) {
            throw new ConfigurationException("Failed to read configuration file: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid number format in configuration: " + e.getMessage());
        }
    }

    private void validateConfiguration() throws ConfigurationException {
        // Validate required fields
        if (hypixelApiKey.isEmpty() || hypixelApiKey.equals("YOUR_HYPIXEL_API_KEY_HERE")) {
            throw new ConfigurationException("Hypixel API key is required. Please set hypixel.api.key in config.properties");
        }

        if (discordBotToken.isEmpty() || discordBotToken.equals("YOUR_DISCORD_BOT_TOKEN_HERE")) {
            throw new ConfigurationException("Discord bot token is required. Please set discord.bot.token in config.properties");
        }

        if (playerListFilename.isEmpty()) {
            throw new ConfigurationException("Player list filename cannot be empty");
        }

        // Validate numerical constraints
        if (checkIntervalMinutes < 5) {
            throw new ConfigurationException("Check interval must be at least 5 minutes to respect Hypixel API limits");
        }

        if (maxTrackedPlayers <= 0 || maxTrackedPlayers > 300) {
            throw new ConfigurationException("Max tracked players must be between 1 and 300");
        }

        System.out.println("Configuration loaded successfully:");
        System.out.println("  Player list file: " + playerListFilename);
        System.out.println("  Contains usernames: " + playerListContainsUsernames);
        System.out.println("  Check interval: " + checkIntervalMinutes + " minutes");
        System.out.println("  Max tracked players: " + maxTrackedPlayers);
    }

    // Getters
    public String getHypixelApiKey() {
        return hypixelApiKey;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getPlayerListFilename() {
        return playerListFilename;
    }

    public boolean isPlayerListContainsUsernames() {
        return playerListContainsUsernames;
    }

    public int getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public int getMaxTrackedPlayers() {
        return maxTrackedPlayers;
    }

    /**
     * Exception thrown when configuration loading or validation fails.
     */
    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}