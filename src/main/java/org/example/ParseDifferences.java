package org.example;

import java.util.List;

/**
 * Processes and categorizes differences in player statistics from Hypixel API data.
 * This class takes raw difference data and converts it into structured information
 * about specific game modes and activities that a player has engaged in.
 *
 * The parser recognizes different types of Hypixel game modes including:
 * - Bedwars (solo, doubles, 3v3, 4v4, 4v4v4v4)
 * - SkyWars (solo/teams, normal/insane)
 * - Duels (various 1v1 and team modes)
 * - Other miscellaneous games and activities
 *
 */
public class ParseDifferences {

    /**
     * Default constructor for ParseDifferences.
     * Initializes a new instance of the difference parser.
     */
    public ParseDifferences() {

    }

    /**
     * Parses a list of raw difference strings and converts them into a structured Differences object.
     * This is the main processing method that categorizes player activity changes into specific
     * game modes and tracks wins/losses where applicable.
     *
     * The method processes difference strings with prefixed codes:
     * - "1": New fields added (typically new activity)
     * - "2": Changed fields (game statistics updates)
     * - "3": Removed fields (rare, typically profile changes)
     *
     * @param differences List of raw difference strings from JSON comparison
     * @param username The player's username for logging and identification
     * @param bonus Additional bonus information (typically cosmetic data for MVP++ players)
     * @return A Differences object containing structured information about player activity,
     *         or an empty Differences object if no relevant changes were found
     */
    public Differences ParseDifferences(List<String> differences, String username, String bonus) {
        Differences diff = new Differences();
        boolean otherActivities = false;
        if (!differences.isEmpty()) {
            // Bedwars game tracking array
            int[] bedwarsGames = new int[11];
            //Key:
            //[0]: if 1, then there have been games played.
            //[1]: number of 1s wins
            //[2]: number of 1s losses
            //[3]: number of 2s wins
            //[4]: number of 2s losses
            //[5]: number of 3s wins
            //[6]: number of 3s losses
            //[7]: number of 4s wins
            //[8]: number of 4s losses
            //[9]: number of 4v4s wins
            //[10]: number of 4v4s losses

            // SkyWars game tracking array
            int[] skywarsGames = new int[9];
            //Key:
            //[0]: if 1, then there have been games played.
            //[1]: number of solo insane wins
            //[2]: number of solo insane losses
            //[3]: number of solo normal wins
            //[4]: number of solo normal losses
            //[5]: number of teams insane wins
            //[6]: number of teams insane losses
            //[7]: number of teams normal wins
            //[8]: number of teams normal losses

            // Miscellaneous games tracking array
            boolean[] randomGames = new boolean[19];
            //[0] = SuperSmash / Smash Heroes
            //[1] = Arcade
            //[2] = Tnt games
            //[3] = Battleground
            //[4] = Arena / Arena Brawl
            //[5] = Paintball
            //[6] = Quake
            //[7] = VampireZ
            //[8] = Walls
            //[9] = Hunger Games
            //[10] = MCGO
            //[11] = Walls3
            //[12] = MurderMystery
            //[13] = SkyBlock
            //[14] = BuildBattle
            //[15] = Housing
            //[16] = WoolGames / Wool Wars
            //[17] = Pit
            //[18] = Main Lobby

            // Duels game tracking array
            int[] duelsGames = new int[13];
            //Key:
            //[0]: if 1, then there have been games played.
            //[1]: played skywars duels
            //[2]: played skywars 2v2s
            //[3]: played nodebuff
            //[4]: played op
            //[5]: played op 2v2s
            //[6]: played classic
            //[7]: played bridge
            //[8]: played uhc duels
            //[9]: played uhc duels 2v2s
            //[10]: played uhc duels 4v4s
            //[11]: played uhc duels meetup
            //[12]: played blitz

            for (String line : differences) {
                if (line.contains("coins")) {
                    continue;
                }
                String firstKey = line.substring(0, 1);
                if (firstKey.equals("1")) {
                    if (!line.contains("player.stats.Pit.profile")) {
                        //otherActivities = true;
                        System.out.println(username + " Act.: " + line);
                    }

                }
                else if (firstKey.equals("3")) {
                    if (!line.contains("player.stats.Pit.profile")) {
                        //otherActivities = true;
                        System.out.println(username + " Act.: " + line);
                    }
                }
                else if (firstKey.equals("2")) {
                    String dataChangeLocation = whereAreWeAt(line.substring(1, line.length()));
                    if (dataChangeLocation.equals("player.stats.Duels")) {
                        String desc = substringAfterLastDot(line);
                        if (desc.equals("sw_duel_rounds_played")) duelsGames[1]++; duelsGames[0] = 1;
                        if (desc.equals("sw_doubles_rounds_played")) duelsGames[2]++; duelsGames[0] = 1;
                        if (desc.equals("potion_duel_rounds_played")) duelsGames[3]++; duelsGames[0] = 1;
                        if (desc.equals("op_duel_rounds_played")) duelsGames[4]++; duelsGames[0] = 1;
                        if (desc.equals("op_doubles_rounds_played")) duelsGames[5]++; duelsGames[0] = 1;
                        if (desc.equals("classic_duel_rounds_played")) duelsGames[6]++; duelsGames[0] = 1;
                        if (desc.equals("bridge_duel_rounds_played")) duelsGames[7]++; duelsGames[0] = 1;
                        if (desc.equals("uhc_duel_rounds_played")) duelsGames[8]++; duelsGames[0] = 1;
                        if (desc.equals("uhc_doubles_rounds_played")) duelsGames[9]++; duelsGames[0] = 1;
                        if (desc.equals("uhc_four_rounds_played")) duelsGames[10]++; duelsGames[0] = 1;
                        if (desc.equals("uhc_meetup_rounds_played")) duelsGames[11]++; duelsGames[0] = 1;
                        if (desc.equals("blitz_duel_rounds_played")) duelsGames[12]++; duelsGames[0] = 1;
                    }
                    else if (dataChangeLocation.equals("player.stats.SkyWars")) {
                        String desc = substringAfterLastDot(line);
                        if (line.equals("2player.stats.SkyWars.levelFormatted") || line.equals("2player.stats.SkyWars.levelFormattedWithBrackets")) continue;
                        System.out.println(username + " Sw: " + line);
                        if (desc.equals("wins_solo_insane")) skywarsGames[1]++; skywarsGames[0] = 1;
                        if (desc.equals("losses_solo_insane")) skywarsGames[2]++; skywarsGames[0] = 1;
                        if (desc.equals("wins_solo_normal")) skywarsGames[3]++; skywarsGames[0] = 1;
                        if (desc.equals("losses_solo_normal")) skywarsGames[4]++; skywarsGames[0] = 1;
                        if (desc.equals("wins_team_insane")) skywarsGames[5]++; skywarsGames[0] = 1;
                        if (desc.equals("losses_team_insane")) skywarsGames[6]++; skywarsGames[0] = 1;
                        if (desc.equals("wins_team_normal")) skywarsGames[7]++; skywarsGames[0] = 1;
                        if (desc.equals("losses_team_normal")) skywarsGames[8]++; skywarsGames[0] = 1;
                    }
                    else if (dataChangeLocation.equals("player.stats.SuperSmash")) { randomGames[0] = true;}
                    else if (dataChangeLocation.equals("player.stats.Arcade")) { randomGames[1] = true;}
                    else if (dataChangeLocation.equals("player.stats.TNTGames")) { randomGames[2] = true;}
                    else if (dataChangeLocation.equals("player.stats.Battleground")) { randomGames[3] = true;}
                    else if (dataChangeLocation.equals("player.stats.UHC")) {
                        //also only coins here, cant do anything.
                    }
                    else if (dataChangeLocation.equals("player.stats.Arena")) { randomGames[4] = true;}
                    else if (dataChangeLocation.equals("player.stats.GingerBread")) {}
                    else if (dataChangeLocation.equals("player.stats.Paintball")) { randomGames[5] = true;}
                    else if (dataChangeLocation.equals("player.stats.Quake")) { randomGames[6] = true;}
                    else if (dataChangeLocation.equals("player.stats.VampireZ")) { randomGames[7] = true;}
                    else if (dataChangeLocation.equals("player.stats.Walls")) { randomGames[8] = true;}
                    else if (dataChangeLocation.equals("player.stats.HungerGames")) { randomGames[9] = true;}
                    else if (dataChangeLocation.equals("player.stats.MCGO")) { randomGames[10] = true;}
                    else if (dataChangeLocation.equals("player.stats.Walls3")) { randomGames[11] = true;}
                    else if (dataChangeLocation.equals("player.stats.SpeedUHC")) {
                        //only coins here, cant tell anything from it.
                    }
                    else if (dataChangeLocation.equals("player.stats.MurderMystery")) { randomGames[12] = true;}
                    else if (dataChangeLocation.equals("player.stats.Bedwars")) {
                        String desc = substringAfterLastDot(line);
                        if (line.contains("2player.stats.Bedwars.slumber")) continue;
                        if (desc.equals("eight_one_games_played_bedwars")) bedwarsGames[1]++; bedwarsGames[0] = 1;
                        if (desc.equals("eight_one_losses_bedwars")) bedwarsGames[2]++; bedwarsGames[0] = 1;
                        if (desc.equals("eight_two_wins_bedwars")) bedwarsGames[3]++; bedwarsGames[0] = 1;
                        if (desc.equals("eight_two_losses_bedwars")) bedwarsGames[4]++; bedwarsGames[0] = 1;
                        if (desc.equals("four_three_wins_bedwars")) bedwarsGames[5]++; bedwarsGames[0] = 1;
                        if (desc.equals("classic_duel_rounds_played")) bedwarsGames[6]++; bedwarsGames[0] = 1;
                        if (desc.equals("four_four_wins_bedwars")) bedwarsGames[7]++; bedwarsGames[0] = 1;
                        if (desc.equals("uhc_duel_rounds_played")) bedwarsGames[8]++; bedwarsGames[0] = 1;
                        if (desc.equals("two_four_wins_bedwars")) bedwarsGames[9]++; bedwarsGames[0] = 1;
                        if (desc.equals("uhc_four_rounds_played")) bedwarsGames[10]++; bedwarsGames[0] = 1;
                    }
                    else if (dataChangeLocation.equals("player.stats.Skyblock")) { randomGames[13] = true;}
                    else if (dataChangeLocation.equals("player.stats.BuildBattle")) { randomGames[14] = true;}
                    else if (dataChangeLocation.equals("player.stats.Housing")) { randomGames[15] = true;}
                    else if (dataChangeLocation.equals("player.stats.Legacy")) {}
                    else if (dataChangeLocation.equals("player.stats.WoolGames")) { randomGames[16] = true;}
                    else if (dataChangeLocation.equals("player.stats.Pit")) {
                        if (!dataChangeLocation.equals("player.stats.Pit.profile")) {
                            randomGames[17] = true;
                        }
                    }
                    else if (dataChangeLocation.equals("player.stats.MainLobby")) { randomGames[18] = true;}
                    else {
                        System.out.println(username + " Act.: " + line);
                        //otherActivities = true;
                    }
                }
                else {
                    System.out.println("Error: ParseDifferences unknown code? Code: " + line);
                }
                if (username.toLowerCase().equals("echens") || username.toLowerCase().equals("5caffold") || username.toLowerCase().equals("flaashx")) {
                    //System.out.println(line); //For debugging
                }
            }
            Differences diff2 = new Differences(username, randomGames, skywarsGames, duelsGames, bedwarsGames, bonus, otherActivities);
            return diff2;
        }
        return diff;
    }

    /**
     * Extracts the parent path from a full JSON field key by removing the last segment.
     * Used to determine which game mode category a changed field belongs to.
     *
     * For example: "player.stats.SkyWars.wins_solo_insane" → "player.stats.SkyWars"
     *
     * @param fullKey The complete JSON field path (e.g., "player.stats.SkyWars.wins")
     * @return The parent path without the final segment, or empty string if no dot found
     */
    private static String whereAreWeAt(String fullKey) {
        int lastDotIndex = fullKey.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; // No dot found, return empty string
        }
        return fullKey.substring(0, lastDotIndex);
    }

    /**
     * Extracts the final segment from a JSON field path after the last dot.
     * Used to get the specific statistic name from a full field path.
     *
     * For example: "player.stats.SkyWars.wins_solo_insane" → "wins_solo_insane"
     *
     * @param input The complete JSON field path
     * @return The final segment after the last dot, or the entire input if no dot found
     */
    private String substringAfterLastDot(String input) {
        int lastDotIndex = input.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == input.length() - 1) {
            return input; // If no dot found or dot is the last character, return the input string
        }
        return input.substring(lastDotIndex + 1);
    }
}