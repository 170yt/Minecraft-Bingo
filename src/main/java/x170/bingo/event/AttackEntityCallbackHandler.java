package x170.bingo.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;

public class AttackEntityCallbackHandler implements AttackEntityCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        // Prevent non-creative players from hitting entities while not playing
        boolean allowed = player.isCreative() || GameManager.status == GameStatus.PLAYING;
        return allowed ? ActionResult.PASS : ActionResult.FAIL;
    }
}
