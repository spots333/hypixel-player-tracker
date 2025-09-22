package org.example;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

/**
 * Manages all Discord bot interactions and channel operations.
 *
 * This class handles:
 *   Setting up and managing player-specific Discord channels
 *   Posting player activity updates as embedded messages
 *   Managing channel permissions for different user roles
 *   Creating formatted embed messages with appropriate thumbnails
 *
 * The class creates individual channels for each tracked player and configures
 * them to be read-only for most users, with only administrators able to post messages.
 * This allows users to subscribe to specific players by muting unwanted channels.
 */
public class DiscordManager {

    /** URL for SkyWars game mode thumbnail image */
    private static String skywars_image = "https://hypixel.net/styles/hypixel-v2/images/game-icons/Skywars-64.png";

    /** URL for Duels game mode thumbnail image */
    private static String duels_image = "https://hypixel.net/styles/hypixel-v2/images/game-icons/Duels-64.png";

    /** URL for Blitz Survival Games thumbnail image */
    private static String blitz_image = "https://hypixel.net/styles/hypixel-v2/images/game-icons/SG-64.png";

    /** URL for general Hypixel thumbnail image */
    private static String hypixel_image = "https://scontent-sea1-1.xx.fbcdn.net/v/t39.30808-6/326277384_903491107675041_3342519570671313382_n.png?_nc_cat=109&ccb=1-7&_nc_sid=5f2048&_nc_ohc=mkiVP01DnhIQ7kNvgHO9Tvu&_nc_ht=scontent-sea1-1.xx&oh=00_AYAG3mlr1DznDRxoP82L7iikfSPUTjHch8LQK8a2ydNGnQ&oe=664B0758";

    /** URL for bot profile avatar image */
    private static String spots_face = "https://s.namemc.com/2d/skin/face.png?id=b5fe63448080d472&scale=4";

    /**
     * Sets up Discord channels for all tracked players across all guilds.
     *
     * This is a convenience method that calls the single-guild setup method
     * for each guild the bot is connected to. Used primarily for slash command
     * contexts where guild information is available through the event.
     *
     * @param event the slash command interaction event providing guild context
     * @param trackedUUIDs array of player UUIDs being tracked
     * @param trackedPlayers array of player usernames corresponding to the UUIDs
     */
    public static void setupChannels(SlashCommandInteractionEvent event, String[] trackedUUIDs, String[] trackedPlayers) {
        for (Guild guild : event.getJDA().getGuilds()) {
            setupChannels(guild, trackedUUIDs, trackedPlayers);
        }
    }

    /**
     * Sets up Discord channels for all tracked players in a specific guild.
     *
     * This method:
     *   Creates a channel for each tracked player if it doesn't exist
     *   Updates existing channel names if the player has changed their username
     *   Sets the channel topic to the player's UUID for persistent identification
     *   Configures appropriate permissions for different user roles
     *
     * Channels are identified by their topic field containing the player's UUID,
     * allowing the system to handle username changes gracefully while maintaining
     * channel continuity.
     *
     * @param guild the Discord guild where channels should be set up
     * @param trackedUUIDs array of player UUIDs being tracked
     * @param trackedPlayers array of current usernames for the tracked players
     */
    public static void setupChannels(Guild guild, String[] trackedUUIDs, String[] trackedPlayers) {
        Role everyoneRole = guild.getPublicRole();

        for (int i = 0; i < trackedUUIDs.length; i++) {
            String uuid = trackedUUIDs[i];
            String username = trackedPlayers[i];
            boolean channelExists = false;

            for (TextChannel channel : guild.getTextChannels()) {
                if (channel.getTopic() != null && channel.getTopic().equals(uuid)) {
                    channelExists = true;
                    if (!channel.getName().equals(username)) {
                        // Update channel name if it doesn't match the tracked username
                        channel.getManager().setName(username).queue();
                    }
                    // Update permissions
                    setChannelPermissions(channel, everyoneRole);
                    break;
                }
            }

            if (!channelExists) {
                // Create a new channel if it doesn't exist
                guild.createTextChannel(username)
                        .setTopic(uuid)
                        .queue(channel -> {
                            // Set permissions
                            setChannelPermissions(channel, everyoneRole);
                        });
            }
        }
    }

    /**
     * Posts a player activity update to the appropriate Discord channel using event context.
     *
     * This method finds the channel corresponding to the player in the differences
     * object and posts the activity update as a formatted embed message. It processes
     * all guilds that the bot is connected to.
     *
     * @param event the Discord event providing guild context
     * @param diff the Differences object containing player activity changes to display
     */
    public static void updateChannel(Event event, Differences diff) {
        for (Guild guild : event.getJDA().getGuilds()) {
            // Search for the channel by name
            TextChannel userChannel = guild.getTextChannelsByName(diff.username, true).stream().findFirst().orElse(null);

            if (userChannel != null) {
                // Send the message if the channel is found
                //userChannel.sendMessage(message).queue();
                EmbedBuilder em = buildEmbed(diff);
                userChannel.sendMessageEmbeds(em.build()).queue();
            } else {
                System.out.println("Channel for user " + diff.username + " not found.");
            }
        }
    }

    /**
     * Posts a player activity update to the appropriate Discord channel in a specific guild.
     *
     * This method finds the channel with the same name as the player's username
     * and posts the activity update as a formatted embed message. If no matching
     * channel is found, an error message is logged to the console.
     *
     * @param g the specific Discord guild to update
     * @param diff the Differences object containing player activity changes to display
     */
    public static void updateChannel(Guild g, Differences diff) {
        // Search for the channel by name
        TextChannel userChannel = g.getTextChannelsByName(diff.username, true).stream().findFirst().orElse(null);

        if (userChannel != null) {
            // Send the message if the channel is found
            //userChannel.sendMessage(message).queue();
            EmbedBuilder em = buildEmbed(diff);
            userChannel.sendMessageEmbeds(em.build()).queue();
        } else {
            System.out.println("Channel for user " + diff.username + " not found.");
        }
    }

    /**
     * Configures the permission overrides for a player-specific channel.
     *
     * <p>Sets up permissions to make channels read-only for most users while allowing
     * administrators to post messages and specific roles to view channels. The permission
     * structure is:
     *   @everyone role: Cannot send messages or view channels
     *   "ad" role: Can send messages (administrator override)
     *   "tracker perms" role: Can view channels
     *
     * @param channel the text channel to configure permissions for
     * @param everyoneRole the @everyone role to apply base restrictions to
     */
    private static void setChannelPermissions(TextChannel channel, Role everyoneRole) {
        channel.upsertPermissionOverride(everyoneRole)
                .deny(Permission.MESSAGE_SEND)
                .queue();
        channel.upsertPermissionOverride(everyoneRole)
                .deny(Permission.VIEW_CHANNEL)
                .queue();

        // Allow administrators to write messages
        channel.upsertPermissionOverride(channel.getGuild().getRolesByName("ad", true).get(0))
                .grant(Permission.MESSAGE_SEND)
                .queue();

        channel.upsertPermissionOverride(channel.getGuild().getRolesByName("tracker perms", true).get(0))
                .grant(Permission.VIEW_CHANNEL)
                .queue();
    }

    /**
     * Creates a formatted Discord embed message for player activity updates.
     *
     * This method constructs a rich embed message containing:
     *   Activity information formatted as the description
     *   Orange sidebar color for visual consistency
     *   Game-specific thumbnail based on detected activity
     *   Footer indicating update frequency and bot avatar
     *   Static title explaining the tracking scope
     *
     * Thumbnail selection priority:
     *   SkyWars image if SkyWars activity detected
     *   Duels image if Duels activity detected
     *   Blitz image if Hunger Games activity detected
     *   General Hypixel image as fallback
     *
     * @param diff the Differences object containing player activity to format
     * @return EmbedBuilder configured with the player's activity information
     */
    private static EmbedBuilder buildEmbed(Differences diff) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Only tracks Games, not activity.", null); // Title with URL (URL can be null)
        embedBuilder.setColor(Color.ORANGE); // Sidebar color

        String content = diff.toString();
        embedBuilder.setDescription(content); // Main message
        embedBuilder.setFooter("Updated every 5 minutes", spots_face); // Footer text with icon URL
        String thumbnailURL = hypixel_image;
        int[] games = diff.gamesPlayed();
        if (games[12] == 1) {
            thumbnailURL = blitz_image;
        }
        if (games[18] == 1) {
            thumbnailURL = duels_image;
        }
        if (games[0] == 1) {
            thumbnailURL = skywars_image;
        }
        embedBuilder.setThumbnail(thumbnailURL);

        return embedBuilder;
    }
}