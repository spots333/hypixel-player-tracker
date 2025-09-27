# Hypixel Tracker
This project is a Hypixel activity tracker. The tracker is built to track a max of 300 unique players, using a Hypixel API key. Stats are checked every 5 minutes to assure no rate limiting from the Hypixel API. 

To make this as simple to access as possible, I have given it a discord bot interface in its own server. The bot automatically creates a new channel with a tracked player name, and outputs all their activity into that channel in the form of discord embeds. This allows a user to only be notified about players they care, just by muting all other channels. It also provides a history log of a tracked player's activity, just by scrolling up in the message channel.

The project displays all 26 Hypixel gamemodes at time of creation. I have added additional insights into the Duels and Skywars gamemodes: if a player is active in a Duels game, it also outputs the exact Duels mode they are active in. If a player is active in a Skywars game, it outputs which Skywars mode they are active in, and if they won or lost that game. It also outputs their Skywars cosmetics; this is done to let users easily identify tracked players in game if they are nicked, just from checking their cosmetics.

## Example
This is a screenshot of discord. On the left you can see multiple text channels, all named after Hypixel players that are being tracked. On the right you can see the chat of a channel. You can see several messages from the APP spots tracker. This is the bot I was using for testing. 
![image](https://github.com/user-attachments/assets/8a7f21a8-ec26-4f5a-9ef7-7cd50d0fe550)
In the first message you can see that the user 'prava' played a Skywars Teams Normal game, and won it. Below that text, there are the cosmetics of the player written out, but I used the spoiler Discord tag around them, so they are hidden. 
In the next message, using discord timestamps, you can tell that 'prava' stopped playing for 2 hours. When they came back, they played a UHC Duel, and some Skywars game, with one Skywars Teams Normal win, and one loss. Below that you can see the cosmetics of the player: they have the Magic Box cage, no balloon, the vanilla projectile trail, the head rocket kill effect, etc.


## How it works
The project relies on four dependencies: JDA for the Discord side, logback (a requirement of JDA), json and gson for the stats parsing side. It also has the ShadowJar application as an option, to let the developer build the project into a Jar file.

### Simplified Diagram

<img width="1280" height="720" alt="SpotsTrackerBotDiagram" src="https://github.com/user-attachments/assets/7a449d9c-3539-4084-8c17-e757d787cf98" />

The Main Loop flow as seen in the diagram runs through every player in the tracked_players.txt list, and repeats every 5 minutes. The real code has Main.java connected to every other module, so I have omitted that for simplicity. JsonComparator, ParseDifferences and Differences all are part of the same pipeline of figuring out which stats changed between two versions of stats and making it readable, they have been split up for coding simplicity.

The project is split up into several java files. I will explain the helper java files first. We have UsernameAndUUIDConverter, which has several public methods to convert an inputted uuid to a username and back through the Mojang API. Then we have MiscHelper.java, which has several helper methods, like listUsernamesToListUUIDs() which takes in an array of usernames and returns an array of matching uuids. listUUIDsToListUsernames() does the opposite. readPlayerUsernames() takes in a file name and returns an array of usernames from that file. This is used for getting our initial list of players to track. We also have generateSwCosmetics() which is a helper method for listing out all the Skywars player cosmetics a player has, and returns them as a built string.

GetStats.java is a class whose job it is just to take in a tracked player's uuid and a Hypixel API key, and return the JsonObject API response.

DiscordManager.java is just used for everything discord related. It sets up channels, updates them in case of a player activity change and automatically makes channels view-only.

Differences.java is an object class which contains all the differences that we care about of a player between their current stats and last check's stats. The differences are passed in the form of arrays, where for Duels, Skywars, and Bedwars, where every integer is the number of wins/losses for a gamemode. For all other modes, there is a different array with just a boolean value, if any fields for that mode have been changed. (this means this mode was played by the tracked player). The Differences object then has a .toString() method which compiles all the differences into a readable text form, which later becomes the body of the Discord embed message.

Now, to the main loop of the application. On a basic level, the project works like so: Every five minutes loop through every player, get their stats, compare them with their previous stats five minutes ago, and if there is a difference, output it in the Discord.

The loop goes like this. While the bot is running, loop through every player. For every player, get their Hypixel stats. Then, get a list of differences between their current stats and old stats. This is done through the JsonComparator.java class, which takes in the new stats in the form of a json, and returns a Differences object. This is done through passing all the changes json fields in a list to the ParseDifferences.java class, which goes through all the changed fields, find ones that are relevant, (such as UHC Duel wins), and logs them to a Differences object. Then, if the Differences object is not empty, we just pass it onto the DiscordManager class to update the relevant player channel with the stats update.

After doing that, we go onto the next player in our list. Then once all the players have been updated, we wait until 5 minutes have passed since we have updated the first player in our list. Then we do everything all over again, until the bot is manually turned off with the /turnoff command that can be ran through Discord.



## Upgrades:
(This project was originally created as a proof-of-concept tracker, so I was not really thinking about its architecture much, just figuring it out as I went) 
- Instead of storing player stats data in a text file in a folder, a database should be implemented.
- More should be containerized, especially the Discord API calls from Main. They should all be moved to DiscordManager.
- For stats comparisons, maps should be used instead of arrays.


## Notes:
- The Hypixel API has a rate limit of 300 requests every 5 minutes, so effectively one request every second. This is why the 
- Nicked means having a disguise with a randomly generated name, so other players who are in game with you do not know your real minecraft account username. There have been some workarounds around this, where players could 'de-nick' nicked players, however at the time of the making of this project, there were none. So checking a cosmetics that a nicked player had during a game and matching them cosmetics from this bot was the only way. 
- This project has in-built reliability features, such as tracking players by their uuid rather than their username, so upon a name change the tracked user remains being tracked. The channel name is also automatically updated upon a tracked player's name change. 
- Players to track are put into a list called tracked_players.txt within the project directory.
- The project can also be built into a Jar file with ShadowJar.
- Player cosmetics are only useful to look at if a player has a rank that allows them to nick. The only rank that allows that is the MVP++ rank. Therefore the bot only outputs a player's Skywars cosmetics if they have that rank.
- The current stats are always saved to a txt file with their uuid. This allows the player tracker to function fine even after it is turned off or restarted.

## Build Instructions:
 - This project uses Java 17
 - Executing Main.java in an IDE is enough for this to run.
 - Build it as a shadowJar to get a runnable jar file.

## Run Instructions
 - For this project to function, you must set a Hypixel API key, from here: (https://developer.hypixel.net/). This requires a Hypixel Forums account and a Minecraft account.
 - For this project to function, you must set a Discord Bot Developer token, from here: (https://discord.com/developers/applications). This requires a Discord Account. After doing so, you must invite the bot either to an existing Discord Server or a new one, and give it Admin permissions.
 - For any results, you must be tracking at least one player. This can be done through putting their username in the tracked_players.txt file, or however you named it. For multiple players, just put every new player username on a new line in that file.
 - Before running, make sure you have the following files: The jar file, the filled out config.properties file and the tracked_players.txt file.
 - To run it, you can either execute the Main file, or build it as a shadowJar and run it through command line (open the command prompt to the directory of where the jar file resides and run something like this: "java -jar spotsTrackerBot-1.0-SNAPSHOT-all.jar")
 - Make sure all files are in the same directory.
 
