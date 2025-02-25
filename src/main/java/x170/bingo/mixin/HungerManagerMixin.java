package x170.bingo.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.setting.Settings;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @ModifyVariable(at = @At("HEAD"), method = "addExhaustion(F)V", argsOnly = true)
    private float injected(float exhaustion) {
        // Return 0 if the game is not playing or if hunger is disabled
        return (GameManager.status != GameStatus.PLAYING || Settings.NO_HUNGER.getBool()) ? 0.0F : exhaustion;
    }
}
