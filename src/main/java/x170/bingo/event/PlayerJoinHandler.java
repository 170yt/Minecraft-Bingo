package x170.bingo.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.setting.SettingsManager;
import x170.bingo.team.TeamManager;
import x170.bingo.team.TeamSelectionGUI;

public class PlayerJoinHandler implements ServerPlayConnectionEvents.Join {
    @Override
    public void onPlayReady(@NotNull ServerPlayNetworkHandler handler, @NotNull PacketSender sender, @NotNull MinecraftServer server) {
        // Set GameMode
        if (GameManager.status == GameStatus.IDLE) {
            handler.player.changeGameMode(GameMode.SURVIVAL);
            GameManager.resetPlayer(handler.player);
            handler.player.sendMessage(GameManager.getWelcomeMessage(handler.player.getName().getString(), handler.player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS)));

            if (TeamManager.getBingoTeam(handler.player) == null) new TeamSelectionGUI(handler.player);
        } else
            handler.player.changeGameMode(TeamManager.getBingoTeam(handler.player) != null ? GameMode.SURVIVAL : GameMode.SPECTATOR);

        // Apply current settings to the player
        SettingsManager.applySettingsToPlayer(handler.player);
    }
}
