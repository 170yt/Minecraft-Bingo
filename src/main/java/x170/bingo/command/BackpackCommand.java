package x170.bingo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.setting.Settings;
import x170.bingo.team.BingoTeam;
import x170.bingo.team.TeamManager;

public class BackpackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("backpack")
                .executes(context -> execute(context.getSource())));
        dispatcher.register(CommandManager.literal("bp")
                .executes(context -> execute(context.getSource())));
        dispatcher.register(CommandManager.literal("ec")
                .executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) {
        if (!Settings.BACKPACK.getBool()) {
            source.sendError(Text.literal("This command is currently disabled"));
            return 0;
        }

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

        bingoTeam.openBackpack(player);

        return 1;
    }
}
