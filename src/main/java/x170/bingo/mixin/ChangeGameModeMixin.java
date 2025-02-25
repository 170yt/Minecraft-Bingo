package x170.bingo.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import x170.bingo.setting.SettingsManager;

@Mixin(ServerPlayerEntity.class)
public class ChangeGameModeMixin {
    @Inject(method = "changeGameMode(Lnet/minecraft/world/GameMode;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;markEffectsDirty()V", shift = At.Shift.AFTER))
    private void afterGameModeChange(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        SettingsManager.applySettingsToPlayer((ServerPlayerEntity) (Object) this);
    }
}
