package x170.bingo.team;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Blocks;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import x170.bingo.Bingo;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.goal.Goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class TeamManager {
    private static final HashMap<String, BingoTeam> teams = new HashMap<>();

    public static void createTeams() {
        teams.put("Gray", new BingoTeam("Gray", Formatting.GRAY, Blocks.GRAY_CONCRETE.asItem(), 0, ScoreboardDisplaySlot.TEAM_GRAY));
        teams.put("Red", new BingoTeam("Red", Formatting.RED, Blocks.RED_CONCRETE.asItem(), 1, ScoreboardDisplaySlot.TEAM_RED));
        teams.put("Orange", new BingoTeam("Orange", Formatting.GOLD, Blocks.ORANGE_CONCRETE.asItem(), 2, ScoreboardDisplaySlot.TEAM_GOLD));
        teams.put("Yellow", new BingoTeam("Yellow", Formatting.YELLOW, Blocks.YELLOW_CONCRETE.asItem(), 3, ScoreboardDisplaySlot.TEAM_YELLOW));
        teams.put("Green", new BingoTeam("Green", Formatting.GREEN, Blocks.LIME_CONCRETE.asItem(), 4, ScoreboardDisplaySlot.TEAM_GREEN));
        teams.put("Cyan", new BingoTeam("Cyan", Formatting.AQUA, Blocks.CYAN_CONCRETE.asItem(), 5, ScoreboardDisplaySlot.TEAM_AQUA));
        teams.put("Blue", new BingoTeam("Blue", Formatting.BLUE, Blocks.BLUE_CONCRETE.asItem(), 6, ScoreboardDisplaySlot.TEAM_BLUE));
        teams.put("Purple", new BingoTeam("Purple", Formatting.DARK_PURPLE, Blocks.PURPLE_CONCRETE.asItem(), 7, ScoreboardDisplaySlot.TEAM_DARK_PURPLE));
        teams.put("Pink", new BingoTeam("Pink", Formatting.LIGHT_PURPLE, Blocks.PINK_CONCRETE.asItem(), 8, ScoreboardDisplaySlot.TEAM_LIGHT_PURPLE));
    }

    public static void initRoundForTeams(TeamGoalManager goalManager, boolean largeBackpack) {
        for (BingoTeam team : teams.values()) {
            team.initRound(new TeamGoalManager(goalManager), largeBackpack);
        }
    }

    public static void checkGoal(ServerPlayerEntity player, Object goalObject) {
        if (GameManager.status != GameStatus.PLAYING) return;

        BingoTeam team = getBingoTeam(player);
        if (team == null) return;

        TeamGoalManager goalManager = team.getGoalManager();
        Goal goal = goalManager.getGoal(goalObject);
        if (goal == null) return;

        if (goalManager.removeGoal(goal)) {
            Bingo.LOGGER.info("Team {} (Player {}) got {}", team.getName(), player.getName().getString(), goal.getName());

            team.sendMessage(
                    Text.literal("§a+ §r").append(goal.getDisplayText()),
                    true
            );

            if (goalManager.isComplete()) {
                Bingo.LOGGER.info("Team {} cleared all goals!", team.getName());
                GameManager.stopGame();
            }
            team.syncScoreboard();
            TeamGoalsGUI.update(team);
        }
    }

    public static BingoTeam getBingoTeam(String name) {
        return teams.get(name);
    }

    public static BingoTeam getBingoTeam(ServerPlayerEntity player) {
        Team team = player.getScoreboardTeam();
        if (team == null) return null;
        return getBingoTeam(team.getName());
    }

    public static ArrayList<BingoTeam> getBingoTeams() {
        return new ArrayList<>(teams.values());
    }

    public static List<String> getLeaderboard() {
        List<String> leaderboard = new ArrayList<>();
        for (BingoTeam bingoTeam : teams.values()) {
            if (bingoTeam.getGoalManager().getScore() == 0) continue;
            leaderboard.add(
                    bingoTeam.getGoalManager().getScorePrintable() + " Goals - " + bingoTeam.getColor() + "Team " + bingoTeam.getName() + Formatting.RESET
            );
        }
        leaderboard.sort(Collections.reverseOrder());
        return leaderboard;
    }

    public static void announceAdvancementToTeam(ServerPlayerEntity player, AdvancementEntry advancement) {
        BingoTeam team = getBingoTeam(player);
        if (team == null) return;

        advancement.value().display().ifPresent(display -> {
            if (display.shouldAnnounceToChat()) {
                team.sendMessage(display.getFrame().getChatAnnouncementText(advancement, player), false);
            }
        });
    }
}
