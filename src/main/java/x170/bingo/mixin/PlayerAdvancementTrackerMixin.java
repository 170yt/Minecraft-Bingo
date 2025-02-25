package x170.bingo.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import x170.bingo.event.PlayerAdvancementAchievedCallback;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;onStatusUpdate(Lnet/minecraft/advancement/AdvancementEntry;)V"), method = "grantCriterion")
    private void grantCriterion(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        // Ignore recipe unlocking advancements (no display)
        if (advancement.value().display().isEmpty()) return;
        PlayerAdvancementAchievedCallback.EVENT.invoker().advancementAchieved(owner, advancement);
    }
}
