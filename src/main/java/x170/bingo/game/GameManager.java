package x170.bingo.game;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.rule.GameRules;
import x170.bingo.Bingo;
import x170.bingo.goal.Goal;
import x170.bingo.pool.PoolManager;
import x170.bingo.setting.Settings;
import x170.bingo.setting.SettingsManager;
import x170.bingo.team.BingoTeam;
import x170.bingo.team.TeamGoalManager;
import x170.bingo.team.TeamGoalsGUI;
import x170.bingo.team.TeamManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class GameManager {
    public static GameStatus status = GameStatus.IDLE;
    public static boolean resetWorldOnStop = false;

    public static void onServerTick(MinecraftServer server) {
        boolean onceASecond = Bingo.SERVER.getTicks() % 20 == 0;

        switch (status) {
            case IDLE:
                break;

            case PLAYING:
                if (onceASecond) Timer.tick();
                break;

            case PAUSED:
                break;
        }

        // Show the timer every second
        if (onceASecond) showTimer();
    }

    public static void startGame() throws CommandSyntaxException {
        int goalAmount = (int) Settings.GOAL_AMOUNT.getDouble();
        ArrayList<Goal> goals = PoolManager.generateGoals(goalAmount);

        TeamManager.initRoundForTeams(new TeamGoalManager(goals), false);

        ServerWorld overworld = Bingo.SERVER.getOverworld();
        TeleportTarget worldSpawn = new TeleportTarget(overworld, overworld.getSpawnPoint().getPos().toBottomCenterPos().add(0, 1, 0), new Vec3d(0, 0, 0), 0, 0, TeleportTarget.NO_OP);

        // Prepare the world for the game
        Bingo.SERVER.getSpawnWorld().getGameRules().setValue(GameRules.ANNOUNCE_ADVANCEMENTS, false, Bingo.SERVER);
        overworld.setTimeOfDay(0);
        overworld.setWeather(ServerWorld.CLEAR_WEATHER_DURATION_PROVIDER.get(overworld.getRandom()), 0, false, false);

        Timer.start();
        status = GameStatus.PLAYING;

        Collection<AdvancementEntry> advancements = Bingo.SERVER.getAdvancementLoader().getAdvancements();

        // Prepare players for the game
        for (ServerPlayerEntity player : Bingo.SERVER.getPlayerManager().getPlayerList()) {
            BingoTeam bingoTeam = TeamManager.getBingoTeam(player);

            // Set GameMode
            player.changeGameMode(bingoTeam != null ? GameMode.SURVIVAL : GameMode.SPECTATOR);

            // Revoke all advancements
            for (AdvancementEntry advancement : advancements) {
                AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
                if (advancementProgress.isAnyObtained()) {
                    for (String string : advancementProgress.getObtainedCriteria()) {
                        player.getAdvancementTracker().revokeCriterion(advancement, string);
                    }
                }
            }

            // Unlock all recipes
            player.unlockRecipes(Bingo.SERVER.getRecipeManager().values());

            resetPlayer(player);

            // Apply settings to player
            SettingsManager.applySettingsToPlayer(player);

            // Play start sound
            GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_END_PORTAL_SPAWN);

            if (bingoTeam == null) continue;

            // Teleport player to the world spawn
            player.teleportTo(worldSpawn);

            // Open the bingo GUI
            TeamGoalsGUI.open(player);
        }

        MutableText startMessage = Text.literal("§6§lBingo Game Started!§r\n")
                .append(Text.literal("§7Clear all " + goalAmount + " goals!§r\n"))
                .append(getCommandMessage("/bingo", "see all goals your team needs to clear", true));
        if (Settings.BACKPACK.getBool())
            startMessage.append(Text.literal("\n")).append(getCommandMessage("/backpack", "access your team's backpack", true));
        if (Settings.COMMAND_TOP.getBool())
            startMessage.append(Text.literal("\n")).append(getCommandMessage("/top", "teleport to the surface (or to the world spawn if you are in the nether or the end)", false));
        Bingo.SERVER.getPlayerManager().broadcast(startMessage, false);
    }

    public static void stopGame() {
        String timeString = Timer.stop();
        status = GameStatus.IDLE;

        for (ServerPlayerEntity player : Bingo.SERVER.getPlayerManager().getPlayerList()) {
            player.changeGameMode(GameMode.SPECTATOR);

            // Apply settings to player
            SettingsManager.applySettingsToPlayer(player);

            // Play sound to all players
            GameManager.playSoundToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_DEATH, 0.5F);
        }

        List<String> leaderboard = TeamManager.getLeaderboard();
        MutableText stopMessage = Text.literal("§6§lBingo Game Over!§r\n")
                .append(Text.literal("§7Time: " + timeString + "§r"));
        if (!leaderboard.isEmpty())
            stopMessage.append(Text.literal("\n")).append(Text.literal(String.join("\n", leaderboard)));

        Bingo.SERVER.getPlayerManager().broadcast(stopMessage, false);
    }

    public static void pauseGame() {
        Timer.pause();
        status = GameStatus.PAUSED;
        SettingsManager.applySettingsToAllPlayers();
    }

    public static void resumeGame() {
        Timer.resume();
        status = GameStatus.PLAYING;
        SettingsManager.applySettingsToAllPlayers();
    }

    private static void showTimer() {
        // Show the timer in every player's actionbar
        String text = Timer.getTime() + " | ";  // List.of("⏹", "⏵", "⏸").get(status.ordinal())
        for (ServerPlayerEntity player : Bingo.SERVER.getPlayerManager().getPlayerList()) {
            BingoTeam team = TeamManager.getBingoTeam(player);
            String playerText = text;
            if (team == null) {
                playerText += "No Team";
            } else {
                TeamGoalManager goalManager = team.getGoalManager();
                if (goalManager == null) {
                    playerText += team.getColor() + team.getName();
                } else {
                    playerText += goalManager.getScorePrintable();
                }
            }
            player.sendMessage(Text.literal(playerText).formatted(Formatting.GOLD, Formatting.BOLD), true);
        }
    }

    public static Text getWelcomeMessage(String playerName, boolean isOp) {
        MutableText message = Text.literal("§6§lHey " + playerName + ", welcome to Bingo!§r\n")
                .append(getCommandMessage("/bingo teams", "join a team", true));
        if (isOp) {
            message.append(Text.literal("\n").append(getCommandMessage("/bingo settings", "change game settings", true)))
                    .append(Text.literal("\n").append(getCommandMessage("/bingo start", "start the game", false)))
                    .append(Text.literal("\n").append(getCommandMessage("/bingo reset", "reset the world", false)))
                    .append(Text.literal("\n§7For all commands, take a look at the suggestions for §e/bingo§r"));
        }
        return message;
    }

    private static Text getCommandMessage(String command, String description, boolean runCommandOnClick) {
        return Text.literal("§7Use ")
                .append(Text.literal("§e" + command + "§7").setStyle(Style.EMPTY
                        .withClickEvent(runCommandOnClick ? new ClickEvent.RunCommand(command) : new ClickEvent.SuggestCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to " + description)))
                ))
                .append(Text.literal("§7 to " + description + "."));
    }

    public static void resetPlayer(ServerPlayerEntity player) {
        // Reset player's inventories, xp, health, hunger, saturation and effects
        player.getInventory().clear();
        player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.getCraftingInput().clear();
        player.playerScreenHandler.onContentChanged(player.getInventory());
        player.getEnderChestInventory().clear();

        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
        player.setHealth(player.getMaxHealth());
        HungerManager hungerManager = player.getHungerManager();
        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5.0F);
        player.clearStatusEffects();
    }

    public static void playSoundToPlayer(ServerPlayerEntity player, SoundEvent soundEvent) {
        playSoundToPlayer(player, soundEvent, 1.0F);
    }

    public static void playSoundToPlayer(ServerPlayerEntity player, SoundEvent soundEvent, float volume) {
        player.networkHandler.sendPacket(
                new PlaySoundS2CPacket(
                        RegistryEntry.of(soundEvent),
                        SoundCategory.MASTER,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        volume,
                        1.0F,
                        player.getEntityWorld().random.nextLong()
                )
        );
    }
}
