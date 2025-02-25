package x170.bingo.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import x170.bingo.setting.SettingsManager;

public class PlayerRespawnHandler implements ServerPlayerEvents.AfterRespawn {
    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        SettingsManager.applySettingsToPlayer(newPlayer);
    }
}
