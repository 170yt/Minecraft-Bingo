package x170.bingo.team;

import net.minecraft.item.Item;
import net.minecraft.scoreboard.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import x170.bingo.Bingo;
import x170.bingo.game.BingoInventory;
import x170.bingo.game.GameManager;
import x170.bingo.goal.Goal;

import java.util.ArrayList;

public class BingoTeam {
    private final String name;
    private final Formatting color;
    private final Item guiItem;
    private final int guiSlot;
    private final Team team;
    private TeamGoalManager goalManager;
    private BingoInventory backpack;

    public BingoTeam(String name, Formatting color, Item guiItem, int guiSlot, ScoreboardDisplaySlot displaySlot) {
        this.name = name;
        this.color = color;
        this.guiItem = guiItem;
        this.guiSlot = guiSlot;

        ServerScoreboard scoreboard = Bingo.SERVER.getScoreboard();

        // Create the team
        Team tmpTeam = scoreboard.getTeam(this.name);
        if (tmpTeam == null) {
            tmpTeam = scoreboard.addTeam(this.name);
        }
        team = tmpTeam;

        team.setColor(this.color);
        team.setCollisionRule(AbstractTeam.CollisionRule.PUSH_OWN_TEAM);
        team.setFriendlyFireAllowed(false);
        team.setShowFriendlyInvisibles(true);
        team.setSuffix(null);

        // Create the team's scoreboard
        if (scoreboard.getObjectiveNames().contains(this.name)) {
            scoreboard.removeObjective(scoreboard.getNullableObjective(this.name));
        }
        ScoreboardObjective objective = scoreboard.addObjective(
                this.name,
                ScoreboardCriterion.DUMMY,
                Text.literal("  BINGO - " + this.name + "  ").formatted(this.color, Formatting.BOLD),
                ScoreboardCriterion.RenderType.INTEGER,
                true,
                null
        );
        scoreboard.setObjectiveSlot(displaySlot, objective);
        this.syncScoreboard();
    }

    public String getName() {
        return name;
    }

    public Formatting getColor() {
        return color;
    }

    public Item getGuiItem() {
        return guiItem;
    }

    public int getSlot() {
        return guiSlot;
    }

    public Team getTeam() {
        return team;
    }

    public TeamGoalManager getGoalManager() {
        return goalManager;
    }

    public void initRound(TeamGoalManager goalManager, boolean largeBackpack) {
        this.goalManager = goalManager;
        backpack = new BingoInventory("Backpack - " + name, largeBackpack);

        this.syncScoreboard();
    }

    public void openBackpack(ServerPlayerEntity player) {
        if (backpack == null) return;
        backpack.open(player);
    }

    public void syncScoreboard() {
        team.setSuffix(Text.of(" | " + (goalManager == null ? "0" : goalManager.getScore())));

        Scoreboard scoreboard = team.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);
        if (objective == null) return;

        // Remove all scores from the objective
        for (ScoreHolder scoreHolder : scoreboard.getKnownScoreHolders()) {
            scoreboard.removeScore(scoreHolder, objective);
        }

        if (goalManager == null || goalManager.isComplete()) {
            scoreboard.getOrCreateScore(ScoreHolder.fromName("Team: " + name), objective).setScore(1);
            return;
        }

        ArrayList<Goal> goals = goalManager.getRemainingGoals();

        // If more than 15 items, show the first 14 and "+X more"
        if (goals.size() > 15) {
            for (int i = 0; i < 14; i++) {
                scoreboard.getOrCreateScore(ScoreHolder.fromName(goals.get(i).toString()), objective).setScore(15 - i);
            }
            scoreboard.getOrCreateScore(ScoreHolder.fromName("§7+§r" + (goals.size() - 14) + " more"), objective).setScore(1);
        } else {
            for (int i = 0; i < goals.size(); i++) {
                scoreboard.getOrCreateScore(ScoreHolder.fromName(goals.get(i).toString()), objective).setScore(goals.size() - i);
            }
        }
    }

    public ArrayList<ServerPlayerEntity> getPlayersInTeam() {
        ArrayList<ServerPlayerEntity> players = new ArrayList<>();
//        for (ServerPlayerEntity player : Bingo.SERVER.getPlayerManager().getPlayerList()) {
//            if (player.getScoreboardTeam() == team) {
//                players.add(player);
//            }
//        }
        for (String name : team.getPlayerList()) {
            ServerPlayerEntity player = Bingo.SERVER.getPlayerManager().getPlayer(name);
            if (player != null)
                players.add(player);
        }
        return players;
    }

    public boolean hasPlayer(ServerPlayerEntity player) {
        return team.getPlayerList().contains(player.getName().getString());
    }

    public void sendMessage(Text message, boolean successSound) {
        for (ServerPlayerEntity player : this.getPlayersInTeam()) {
            player.sendMessage(message);
            if (successSound) GameManager.playSoundToPlayer(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }
}
