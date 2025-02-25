package x170.bingo.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.GameMode;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.team.TeamManager;
import x170.bingo.setting.SettingsManager;
import x170.bingo.team.TeamSelectionGUI;

public class PlayerJoinHandler implements ServerPlayConnectionEvents.Join {
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        // Set GameMode
        if (GameManager.status == GameStatus.IDLE) {
            handler.player.changeGameMode(GameMode.SURVIVAL);
            GameManager.resetPlayer(handler.player);
            handler.player.sendMessage(GameManager.getWelcomeMessage(handler.player.getName().getString(), handler.player.hasPermissionLevel(2)));

            if (TeamManager.getBingoTeam(handler.player) == null) new TeamSelectionGUI(handler.player);
        } else
            handler.player.changeGameMode(TeamManager.getBingoTeam(handler.player) != null ? GameMode.SURVIVAL : GameMode.SPECTATOR);

        // Apply current settings to the player
        SettingsManager.applySettingsToPlayer(handler.player);
    }
}
