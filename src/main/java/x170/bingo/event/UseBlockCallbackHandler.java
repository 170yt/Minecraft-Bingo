package x170.bingo.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;

public class UseBlockCallbackHandler implements UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        // Prevent non-creative players from placing blocks while not playing
        boolean allowed = player.isCreative() || GameManager.status == GameStatus.PLAYING;
        return allowed ? ActionResult.PASS : ActionResult.FAIL;
    }
}
