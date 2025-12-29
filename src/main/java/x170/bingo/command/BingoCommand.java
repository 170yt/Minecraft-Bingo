package x170.bingo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import x170.bingo.Bingo;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.setting.SettingsGUI;
import x170.bingo.team.BingoTeam;
import x170.bingo.team.TeamGoalsGUI;
import x170.bingo.team.TeamManager;
import x170.bingo.team.TeamSelectionGUI;

import java.util.UUID;

public class BingoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                CommandManager.literal("bingo")
                        // .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .executes(context -> executeBingo(context.getSource()))
                        .then(
                                CommandManager.literal("teams")
                                        .executes(context -> executeTeams(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("settings")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                        .executes(context -> executeSettings(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("start")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                        .executes(context -> executeStart(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("stop")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                        .executes(context -> executeStop(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("pause")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                        .executes(context -> executePause(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("resume")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                                        .executes(context -> executeResume(context.getSource()))
                        )
                        .then(
                                CommandManager.literal("reset")
                                        .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))
                                        .executes(context -> executeReset(context.getSource(), false))
                                        .then(
                                                // generate random string to prevent accidental resets
                                                CommandManager.literal(UUID.randomUUID().toString().split("-")[0])
                                                        .executes(context -> executeReset(context.getSource(), true))
                                        )
                        )
        );
        dispatcher.register(CommandManager.literal("b")
                .executes(context -> executeBingo(context.getSource()))
                .redirect(literalCommandNode));
    }

    private static int executeBingo(ServerCommandSource source) {
        // Send error message if the game is not currently running
        if (GameManager.status != GameStatus.PLAYING) {
            source.sendError(Text.literal("The game is not currently running"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Send error message if the command was called by a non-player
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        // Send error message if the player is not in a team
        BingoTeam bingoTeam = TeamManager.getBingoTeam(player);
        if (bingoTeam == null) {
            source.sendError(Text.literal("You are not in a team"));
            return 0;
        }

        TeamGoalsGUI.open(player);

        return 1;
    }

    private static int executeTeams(ServerCommandSource source) {
        // Send error message if the game is already running
        if (GameManager.status != GameStatus.IDLE) {
            source.sendError(Text.literal("You cannot change teams while the game is running"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Send error message if the command was called by a non-player
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        new TeamSelectionGUI(player);

        return 1;
    }

    private static int executeSettings(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        // Send error message if the command was called by a non-player
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        new SettingsGUI(player);
//        source.sendFeedback(() -> Text.literal("Item Pools:\n" + ItemPoolManager.getPoolInfos(true)), false);
        return 1;
    }

    private static int executeStart(ServerCommandSource source) throws CommandSyntaxException {
        // Send error message if the game is already running
        if (GameManager.status != GameStatus.IDLE) {
            source.sendError(Text.literal("The game is already running. Use /bingo stop to stop the game"));
            return 0;
        }

        GameManager.startGame();

        playSuccessSound(source);
        return 1;
    }

    private static int executeStop(ServerCommandSource source) {
        // Send error message if the game is not currently running
        if (GameManager.status == GameStatus.IDLE) {
            source.sendError(Text.literal("The game is not currently running. Use /bingo start to start a new game"));
            return 0;
        }

        GameManager.stopGame();

        playSuccessSound(source);
        return 1;
    }

    private static int executePause(ServerCommandSource source) {
        // Send error message if the game is already paused
        if (GameManager.status == GameStatus.PAUSED) {
            source.sendError(Text.literal("The game is already paused. Use /bingo resume to continue"));
            return 0;
        }

        // Send error message if the game is not currently running
        if (GameManager.status != GameStatus.PLAYING) {
            source.sendError(Text.literal("The game is not currently running. Use /bingo start to begin"));
            return 0;
        }

        GameManager.pauseGame();

        playSuccessSound(source);
        return 1;
    }

    private static int executeResume(ServerCommandSource source) {
        // Send error message if the game is not paused
        if (GameManager.status != GameStatus.PAUSED) {
            source.sendError(Text.literal("The game is not currently paused. Use /bingo pause to pause the game"));
            return 0;
        }

        GameManager.resumeGame();

        playSuccessSound(source);
        return 1;
    }

    private static int executeReset(ServerCommandSource source, boolean confirmed) {
        // Send error message if the logical server is not hosted by a physical server
//        if (...) {
//            source.sendError(Text.literal("The world cannot be reset in a singleplayer world. Please use a server instead"));
//            return 0;
//        }

        // Send error message if the reset was not confirmed
        if (!confirmed) {
            source.sendError(Text.literal("To prevent accidental resets, please use /bingo reset <suggested random string>"));
            return 0;
        }

        // Send error message if the game is currently running
        if (GameManager.status != GameStatus.IDLE) {
            source.sendError(Text.literal("Please stop the game before resetting the world. Use /bingo stop to stop the game"));
            return 0;
        }

        GameManager.resetWorldOnStop = true;
        Bingo.SERVER.stop(false);
        return 1;
    }

    private static void playSuccessSound(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return;
        GameManager.playSoundToPlayer(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
    }
}
