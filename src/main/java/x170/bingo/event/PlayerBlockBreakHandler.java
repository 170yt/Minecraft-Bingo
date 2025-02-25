package x170.bingo.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;

public class PlayerBlockBreakHandler implements PlayerBlockBreakEvents.Before {
    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        // Prevent non-creative players from breaking blocks while not playing
        return player.isCreative() || GameManager.status == GameStatus.PLAYING;
    }
}
