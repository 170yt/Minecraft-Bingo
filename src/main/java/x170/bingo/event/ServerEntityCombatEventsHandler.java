package x170.bingo.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.team.TeamManager;

public class ServerEntityCombatEventsHandler implements ServerEntityCombatEvents.AfterKilledOtherEntity {
    @Override
    public void afterKilledOtherEntity(ServerWorld world, Entity entity, LivingEntity killedEntity, DamageSource damageSource) {
        if (GameManager.status != GameStatus.PLAYING) return;

        if (entity instanceof ServerPlayerEntity player) {
            TeamManager.checkGoal(player, killedEntity.getType());
        }
    }
}
