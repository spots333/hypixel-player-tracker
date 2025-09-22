package org.example;

/**
 * Represents the statistical differences detected for a Hypixel player between two check cycles.
 *
 * This class encapsulates all tracked changes in a player's Hypixel statistics, including
 * wins/losses in specific game modes, activity in various mini-games, and cosmetic information
 * for MVP++ players. It provides methods to format this information for Discord display and
 * to determine which games were played.
 *
 * The class tracks the following categories:
 *   Duels games with specific mode tracking
 *   SkyWars games with win/loss counts by mode
 *   Bedwars games with team size specific tracking
 *   General activity across 19 other Hypixel game modes
 *   Cosmetic information for MVP++ ranked players
 */
public class Differences {
    /** The display name of the player these differences belong to */
    public final String username;

    /** Array tracking duels game activity. Index 0 indicates if any duels were played, subsequent indices track specific modes */
    private int[] duelsChanges;

    /** Boolean array indicating activity in 19 different Hypixel game modes */
    private boolean[] randomGames;

    /** Array tracking SkyWars game results. Index 0 indicates activity, subsequent indices track wins/losses by mode */
    private int[] skywarsChanges;

    /** Array tracking Bedwars game results. Index 0 indicates activity, subsequent indices track wins/losses by team size */
    private int[] bedwarsChanges;

    /** String containing cosmetic information for MVP++ players, empty for other ranks */
    private String bonus;

    /** Flag indicating if the player was active in untracked or miscellaneous areas */
    private boolean otherActivities;

    /** Flag indicating if this Differences object contains no actual changes */
    private boolean empty;

    /**
     * Constructs a Differences object with detected player activity changes.
     *
     * @param username the display name of the player
     * @param randomGames boolean array indicating activity in various game modes
     * @param sw integer array tracking SkyWars wins/losses by mode
     * @param duel integer array tracking Duels activity by mode
     * @param bw integer array tracking Bedwars wins/losses by team size
     * @param bonus cosmetic information string for MVP++ players
     * @param otherAct flag for miscellaneous activity detection
     */
    public Differences(String username, boolean[] randomGames, int[] sw, int[] duel, int[] bw, String bonus, boolean otherAct) {
        this.username = username;
        this.duelsChanges = duel.clone();
        this.skywarsChanges = sw.clone();
        this.randomGames = randomGames.clone();
        this.bonus = bonus;
        this.otherActivities = otherAct;
        this.bedwarsChanges = bw.clone();
        empty = checkIfNotEmpty();
    }

    /**
     * Constructs an empty Differences object representing no detected changes.
     * Used as a default return value when no player activity is detected.
     */
    public Differences() {
        this.username = "";
        this.duelsChanges = null;
        this.skywarsChanges = null;
        this.randomGames = null;
        this.bonus = "";
        this.otherActivities = false;
        this.bedwarsChanges = null;
        empty = true;
    }

    /**
     * Checks if this Differences object contains any actual changes.
     *
     * @return true if no changes were detected, false if activity was found
     */
    public boolean isEmpty() {
        return this.empty;
    }

    /**
     * Converts the detected differences into a human-readable string format.
     *
     * This method formats all detected activity into a string suitable for Discord
     * message display. It includes game mode names, win/loss counts, and cosmetic
     * information where applicable. The format is optimized for Discord embed messages.
     *
     * @return formatted string describing all detected player activity, empty string if no activity
     */
    @Override
    public String toString() {
        String content = "";
        if (randomGames[0]) {content += "\nSmash Heroes";}
        if (randomGames[1]) {content += "\nArcade";}
        if (randomGames[2]) {content += "\nTnt Games";}
        if (randomGames[3]) {content += "\nBattleground?";}
        if (randomGames[4]) {content += "\nArena Brawl";}
        if (randomGames[5]) {content += "\nPaintball";}
        if (randomGames[6]) {content += "\nQuake";}
        if (randomGames[7]) {content += "\nVampireZ";}
        if (randomGames[8]) {content += "\nWalls";}
        if (randomGames[10]) {content += "\nCops v Crims";}
        if (randomGames[11]) {content += "\nWalls3?";}
        if (randomGames[12]) {content += "\nMurder Mystery";}
        if (randomGames[13]) {content += "\nSkyblock";}
        if (randomGames[14]) {content += "\nBuild Battle";}
        if (randomGames[15]) {content += "\nHousing";}
        if (randomGames[16]) {content += "\nWool Wars";}
        if (randomGames[17]) {content += "\nPit";}
        if (randomGames[18]) {content += "\nMain Lobby Shenanigans";}
        if (randomGames[9]) {
            content += "\nBlitz";
        }
        if (duelsChanges[0] == 1) {
            content += "\nDuels Games: \n";
            if (duelsChanges[1] != 0) content += "Skywars 1v1s ";
            if (duelsChanges[2] != 0) content += "Skywars 2v2s ";
            if (duelsChanges[3] != 0) content += "Nodebuff 1v1s ";
            if (duelsChanges[4] != 0) content += "OP 1v1s ";
            if (duelsChanges[5] != 0) content += "OP 2v2s ";
            if (duelsChanges[6] != 0) content += "Classic 1v1s ";
            if (duelsChanges[7] != 0) content += "Bridge 1v1s ";
            if (duelsChanges[8] != 0) content += "UHC 1v1s ";
            if (duelsChanges[9] != 0) content += "UHC 2v2s ";
            if (duelsChanges[10] != 0) content += "UHC 4v4s ";
            if (duelsChanges[11] != 0) content += "UHC meetup ";
            if (duelsChanges[12] != 0) content += "Blitz 1v1s ";
        }
        if (skywarsChanges[0] == 1) {
            content += "\nSkywars Games: ";
            if (skywarsChanges[1] != 0) content += "\nSolo Insane Wins: " + skywarsChanges[1];
            if (skywarsChanges[2] != 0) content += "\nSolo Insane Losses: " + skywarsChanges[2];
            if (skywarsChanges[3] != 0) content += "\nSolo Normal Wins: " + skywarsChanges[3];
            if (skywarsChanges[4] != 0) content += "\nSolo Normal Losses: " + skywarsChanges[4];
            if (skywarsChanges[5] != 0) content += "\nTeams Insane Wins: " + skywarsChanges[5];
            if (skywarsChanges[6] != 0) content += "\nTeams Insane Losses: " + skywarsChanges[6];
            if (skywarsChanges[7] != 0) content += "\nTeams Normal Wins: " + skywarsChanges[7];
            if (skywarsChanges[8] != 0) content += "\nTeams Normal Losses: " + skywarsChanges[8];
            if (!bonus.isEmpty()) {
                content += "\n||" + bonus + "||";
            }
        }
        if (bedwarsChanges[0] == 1) {
            content += "\nBedwars Games: ";
            if (bedwarsChanges[1] != 0) content += "\nSolo Games Played: " + bedwarsChanges[1];
            if (bedwarsChanges[2] != 0)
                if (bedwarsChanges[3] != 0) content += "\n2s Wins: " + bedwarsChanges[3];
            if (bedwarsChanges[4] != 0) content += "\n2s Losses: " + bedwarsChanges[4];
            if (bedwarsChanges[5] != 0) content += "\n3s Wins: " + bedwarsChanges[5];
            if (bedwarsChanges[6] != 0) content += "\n3s Losses: " + bedwarsChanges[6];
            if (bedwarsChanges[7] != 0) content += "\n4s Wins: " + bedwarsChanges[7];
            if (bedwarsChanges[8] != 0) content += "\n4s Losses: " + bedwarsChanges[8];
            if (bedwarsChanges[9] != 0) content += "\n4v4 Wins: " + bedwarsChanges[9];
            if (bedwarsChanges[10] != 0) content += "\n4v4 Losses: " + bedwarsChanges[10];
        }
        if (otherActivities) {
            content += "\nUser has been active in other areas.";
        }
        return content;
    }

    /**
     * Returns an array indicating which game modes the player was active in.
     *
     * This method provides a standardized way to determine game activity for
     * purposes like selecting appropriate Discord embed thumbnails or categorizing
     * player behavior patterns.
     *
     * @return integer array where 1 indicates activity in that game mode, 0 indicates no activity
     */
    public int[] gamesPlayed() {
        int[] games_played = new int[26];
        //[0] = SkyWars
        //[1] = SuperSmash
        //[2] = Arcade
        //[3] = TNTGames
        //[4] = Battleground
        //[5] = UHC
        //[6] = Arena
        //[7] = GingerBread
        //[8] = Paintball
        //[9] = Quake
        //[10] = VampireZ
        //[11] = Walls
        //[12] = HungerGames
        //[13] = MCGO
        //[14] = Walls3
        //[15] = SpeedUHC
        //[16] = MurderMystery
        //[17] = Bedwars
        //[18] = Duels
        //[19] = Skyblock
        //[20] = BuildBattle
        //[21] = Housing
        //[22] = Legacy
        //[23] = WoolGames
        //[24] = Pit
        //[25] = MainLobby
        if (randomGames[9]) {
            games_played[12] = 1;
        }
        if (skywarsChanges[0] == 1) {
            games_played[0] = 1;
        }
        if (duelsChanges[0] == 1) {
            games_played[18] = 1;
        }
        return games_played;
    }

    /**
     * Internal method to determine if this Differences object contains any meaningful changes.
     *
     * Checks all activity arrays and flags to determine if any actual player activity
     * was detected. Used during object construction to set the empty flag.
     *
     * @return false if any activity was detected, true if no changes were found
     */
    private boolean checkIfNotEmpty() {
        for (int i = 0; i < randomGames.length; i++) {
            if (randomGames[i]) {
                return false;
            }
        }
        if (skywarsChanges[0] == 1) {
            return false;
        }
        if (duelsChanges[0] == 1) {
            return false;
        }
        if (bedwarsChanges[0] == 1) {
            return false;
        }
        return true;
    }
}