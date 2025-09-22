package org.example;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

/**
 * Main application class for the Hypixel Activity Tracker.
 *
 * This Discord bot tracks up to 300 Hypixel players and monitors their gaming activity
 * across all 26 Hypixel game modes. The bot creates individual Discord channels for each
 * tracked player and posts activity updates as embedded messages every 5 minutes.
 *
 * Key Features:
 *   Real-time player activity tracking via Hypixel API
 *   Detailed game mode analysis for Duels and SkyWars
 *   Cosmetic information for MVP++ players
 *   Automatic Discord channel management
 *
 */
public class Main extends ListenerAdapter
{
    /** Logger instance for application logging */
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    /** Global flag to control application running state */
    private static volatile boolean running = true;

    /** JDA instance for Discord bot functionality */
    private static JDA jda;

    /** Application configuration instance */
    private static Configuration config;

    /**
     * Main entry point for the Hypixel Activity Tracker application.
     *
     * Initializes the Discord bot, sets up slash commands, and starts the main
     * player tracking loop. Handles graceful shutdown via shutdown hooks.
     *
     * @param args command line arguments (currently unused)
     */
    public static void main(String[] args) {
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered. Initiating graceful shutdown...");
            initiateShutdown();
        }));

        try {
            // Load configuration
            config = new Configuration();
            logger.info("Configuration loaded successfully");

        } catch (Configuration.ConfigurationException e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            // Initialize Discord bot
            jda = JDABuilder.createLight(config.getDiscordBotToken(), EnumSet.noneOf(GatewayIntent.class))
                    .addEventListeners(new Main())
                    .build();

            // Setup commands
            setupDiscordCommands();

            // Initialize player tracking
            initializePlayerTracking();

        } catch (Exception e) {
            logger.error("Failed to initialize application: {}", e.getMessage(), e);
            System.err.println("Initialization failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Sets up Discord slash commands for bot interaction.
     *
     * Registers the following commands:
     *   /say - Makes the bot repeat a message, this is used for testing if the bot is online
     *   /turnoff - Administrator-only command to shutdown the bot
     *
     * @throws InterruptedException if the thread is interrupted during setup
     */
    private static void setupDiscordCommands() throws InterruptedException {
        CommandListUpdateAction commands = jda.updateCommands();

        //Test command to know if the bot is on/running
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(STRING, "content", "What the bot should say", true)
        );

        commands.addCommands(
                Commands.slash("turnoff", "turns off the bot")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        Thread.sleep(1000);
        commands.queue();
    }

    /**
     * Initializes the player tracking system.
     *
     * Loads the player list from configuration, converts usernames to UUIDs if necessary,
     * sets up Discord channels for each player, and starts the main tracking loop.
     *
     * @throws InterruptedException if the initialization process is interrupted
     */
    private static void initializePlayerTracking() throws InterruptedException {
        UsernameAndUUIDConverter uuids = new UsernameAndUUIDConverter();
        String[] trackedUUIDs;
        String[] trackedPlayers;

        if (config.isPlayerListContainsUsernames()) {
            // Track players with usernames
            trackedPlayers = MiscHelper.readPlayerUsernames(config.getPlayerListFilename());

            if (trackedPlayers.length > config.getMaxTrackedPlayers()) {
                logger.warn("Player list contains {} players, but max is {}. Only tracking first {} players.",
                        trackedPlayers.length, config.getMaxTrackedPlayers(), config.getMaxTrackedPlayers());
                String[] trimmedPlayers = new String[config.getMaxTrackedPlayers()];
                System.arraycopy(trackedPlayers, 0, trimmedPlayers, 0, config.getMaxTrackedPlayers());
                trackedPlayers = trimmedPlayers;
            }

            trackedUUIDs = null;
            while (trackedUUIDs == null && running) {
                trackedUUIDs = MiscHelper.listUsernamesToListUUIDs(trackedPlayers, uuids);
                if (trackedUUIDs == null) {
                    logger.warn("Connection to Mojang API failed. Retrying in 2 seconds...");
                    Thread.sleep(2000);
                }
            }
        } else {
            // Track players with UUIDs
            trackedUUIDs = MiscHelper.readPlayerUsernames(config.getPlayerListFilename());

            if (trackedUUIDs.length > config.getMaxTrackedPlayers()) {
                logger.warn("Player list contains {} players, but max is {}. Only tracking first {} players.",
                        trackedUUIDs.length, config.getMaxTrackedPlayers(), config.getMaxTrackedPlayers());
                String[] trimmedUUIDs = new String[config.getMaxTrackedPlayers()];
                System.arraycopy(trackedUUIDs, 0, trimmedUUIDs, 0, config.getMaxTrackedPlayers());
                trackedUUIDs = trimmedUUIDs;
            }

            trackedPlayers = null;
            while (trackedPlayers == null && running) {
                trackedPlayers = MiscHelper.listUUIDsToListUsernames(trackedUUIDs, uuids);
                if (trackedPlayers == null) {
                    logger.warn("Connection to Mojang API failed. Retrying in 2 seconds...");
                    Thread.sleep(2000);
                }
            }
        }

        if (!running) {
            logger.info("Shutdown requested during initialization. Exiting...");
            return;
        }

        Guild guild = jda.getGuilds().get(0);
        DiscordManager.setupChannels(guild, trackedUUIDs, trackedPlayers);
        logfourj(guild, "Setting up Channels Complete");

        // Start main tracking loop
        startTrackingLoop(guild, trackedUUIDs);
    }

    /**
     * Starts the main player tracking loop.
     *
     * Continuously monitors tracked players by fetching their stats from the Hypixel API,
     * comparing with previous stats, and posting updates to Discord channels when changes
     * are detected. Respects API rate limits with 1-second delays between requests.
     *
     * @param guild the Discord guild where channels are managed
     * @param trackedUUIDs array of player UUIDs to track
     */
    private static void startTrackingLoop(Guild guild, String[] trackedUUIDs) {
        logger.info("Starting player tracking loop for {} players", trackedUUIDs.length);

        TextChannel loggingChannel = guild.getTextChannelsByName("logging", true).stream().findFirst().orElse(null);
        JsonComparator comp = new JsonComparator();
        GetStats statsGetter = new GetStats(config.getHypixelApiKey());

        int waitTime = 1001; // milliseconds between player checks (to not get API limited)
        long checkIntervalMs = config.getCheckIntervalMinutes() * 60 * 1000L;

        while (running) {
            long cycleStartTime = System.currentTimeMillis();

            if (loggingChannel != null) {
                loggingChannel.sendMessage("Starting new checking cycle").queue();
            }

            for (int i = 0; i < trackedUUIDs.length && running; i++) {
                String uuid = trackedUUIDs[i];

                try {
                    JsonObject newStats = statsGetter.getPlayerDataJSON(uuid);

                    if (newStats == null) {
                        logfourj(guild, "Could not connect to Hypixel API. Skipping player " + (i + 1));
                        Thread.sleep(waitTime);
                        continue;
                    }

                    if (newStats.size() == 0) {
                        logfourj(guild, "Invalid API Key or API error");
                        Thread.sleep(waitTime);
                        continue;
                    }

                    String displayName = newStats.getAsJsonObject("player").get("displayname").getAsString();
                    boolean mvppp = MiscHelper.hasMvpPlusPlus(newStats);
                    List<String> diff = comp.compare(newStats, uuid);
                    String bonus = "";

                    if (mvppp) {
                        bonus = MiscHelper.generateSwCosmetics(newStats);
                    }

                    ParseDifferences pa = new ParseDifferences();
                    Differences d = pa.ParseDifferences(diff, displayName, bonus);

                    if (!d.isEmpty()) {
                        DiscordManager.updateChannel(guild, d);
                    }

                    Thread.sleep(waitTime);

                } catch (InterruptedException e) {
                    logger.info("Player tracking interrupted");
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    logger.error("Error processing player {}: {}", uuid, e.getMessage());
                }
            }

            if (!running) {
                break;
            }

            if (loggingChannel != null) {
                loggingChannel.sendMessage("Ended checking cycle. Sleeping.").queue();
            }

            // Calculate how long to sleep to maintain the check interval
            long cycleTime = System.currentTimeMillis() - cycleStartTime;
            long sleepTime = checkIntervalMs - cycleTime + 10000; // Add 10 seconds buffer

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    logger.info("Sleep interrupted during cycle break");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        logger.info("Player tracking loop terminated");
    }

    /**
     * Initiates graceful shutdown of the application.
     *
     * Stops the tracking loop, sends shutdown notifications to Discord channels,
     * and properly closes the JDA connection with a timeout.
     */
    public static void initiateShutdown() {
        running = false;
        logger.info("Shutdown initiated...");

        if (jda != null) {
            try {
                // Send shutdown message to logging channels
                for (Guild guild : jda.getGuilds()) {
                    TextChannel loggingChannel = guild.getTextChannelsByName("logging", true)
                            .stream().findFirst().orElse(null);
                    if (loggingChannel != null) {
                        loggingChannel.sendMessage("Bot shutting down gracefully...").complete();
                    }
                }

                logger.info("Shutting down Discord connection...");
                jda.shutdown();

                // Wait for shutdown to complete (max 10 seconds)
                if (!jda.awaitShutdown(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    logger.warn("JDA did not shutdown within 10 seconds, forcing shutdown");
                    jda.shutdownNow();
                }

                logger.info("Discord connection closed successfully");
            } catch (Exception e) {
                logger.error("Error during shutdown: {}", e.getMessage());
            }
        }

        logger.info("Graceful shutdown completed");
    }

    /**
     * Handles slash command interactions from Discord users.
     *
     * @param event the slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        if (event.getGuild() == null)
            return;

        switch (event.getName())
        {
            case "say":
                say(event, event.getOption("content").getAsString());
                break;
            case "turnoff":
                killBotCommand(event);
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    /**
     * Handles button interaction events from Discord users.
     *
     * @param event the button interaction event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event)
    {
        String[] id = event.getComponentId().split(":");
        String authorId = id[0];
        String type = id[1];

        if (!authorId.equals(event.getUser().getId()))
            return;

        event.deferEdit().queue();

        MessageChannel channel = event.getChannel();
        switch (type)
        {
            case "prune":
                int amount = Integer.parseInt(id[2]);
                event.getChannel().getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(amount)
                        .thenAccept(channel::purgeMessages);
            case "delete":
                event.getHook().deleteOriginal().queue();
                break;
            case "killbot":
                killTheBot(event);
                break;
        }
    }

    /**
     * Handles the Discord bot ready event.
     *
     * Creates or finds the logging channel and posts startup messages to indicate
     * the bot is online and configuration was loaded successfully.
     *
     * @param event the ready event from JDA
     */
    @Override
    public void onReady(ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            TextChannel loggingChannel = guild.getTextChannelsByName("logging", true).stream().findFirst().orElse(null);

            if (loggingChannel == null) {
                guild.createTextChannel("logging").queue(channel -> {
                    channel.sendMessage("Booting up; Configuration loaded successfully").queue();
                });
            } else {
                loggingChannel.sendMessage("Booting up; Configuration loaded successfully").queue();
            }
        }
    }

    /**
     * Logs a message to the Discord logging channel.
     *
     * Creates the logging channel if it doesn't exist, then posts the message.
     * This is used for bot status updates and error logging visible in Discord.
     *
     * @param guild the Discord guild where the logging channel should be
     * @param message the message to log
     */
    public static void logfourj(Guild guild, String message) {
        TextChannel loggingChannel = guild.getTextChannelsByName("logging", true).stream().findFirst().orElse(null);

        if (loggingChannel == null) {
            guild.createTextChannel("logging").queue(channel -> {
                channel.sendMessage("Created logging channel").queue();
                channel.sendMessage(message).queue();
            });
        } else {
            loggingChannel.sendMessage(message).queue();
        }
    }

    /**
     * Handles the /turnoff slash command by presenting confirmation buttons.
     *
     * @param event the slash command interaction event
     */
    public void killBotCommand(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();

        event.reply("\n**This WILL TURN OFF THE BOT**\n")
                .addActionRow(
                        Button.secondary(userId + ":del6426ete", "Nevermind!"),
                        Button.danger(userId + ":killbot:", "Yes!"))
                .queue();
    }

    /**
     * Handles the confirmation button for shutting down the bot.
     *
     * @param event the button interaction event
     */
    private void killTheBot(ButtonInteractionEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            TextChannel loggingChannel = guild.getTextChannelsByName("logging", true).stream().findFirst().orElse(null);
            if (loggingChannel != null) {
                loggingChannel.sendMessage("Bot shutdown requested by user.").queue();
            }
        }

        // Initiate graceful shutdown
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Give time for the message to send
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            initiateShutdown();
            System.exit(0);
        }).start();
    }

    /**
     * Handles the /say slash command by echoing the provided content.
     *
     * @param event the slash command interaction event
     * @param content the content to echo back
     */
    public void say(SlashCommandInteractionEvent event, String content)
    {
        event.reply(content).queue();
    }
}