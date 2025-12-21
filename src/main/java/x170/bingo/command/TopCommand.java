package x170.bingo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import x170.bingo.Bingo;
import x170.bingo.game.GameManager;
import x170.bingo.setting.Settings;

public class TopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("top")
                .executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) {
        if (!Settings.COMMAND_TOP.getBool()) {
            source.sendError(Text.literal("This command is currently disabled"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        // Send error message if the command was called by a non-player
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command"));
            return 0;
        }

        // Send error message if the player is not on the ground
        if (!player.isOnGround() && !player.isSubmergedInWater()) {
            source.sendError(Text.literal("You must be on the ground to use this command"));
            return 0;
        }

        String dimension = player.getEntityWorld().getRegistryKey().getValue().toString();
        if (dimension.equals("minecraft:the_nether") || dimension.equals("minecraft:the_end")) {
            ServerWorld overworld = Bingo.SERVER.getOverworld();
            player.teleportTo(new TeleportTarget(overworld, overworld.getSpawnPoint().getPos().toBottomCenterPos().add(0, 1, 0), new Vec3d(0, 0, 0), 0, 0, TeleportTarget.NO_OP));
        } else {
            // Get the player's current position and the highest block at that position
            int x = player.getBlockPos().getX();
            int z = player.getBlockPos().getZ();
            int y = player.getEntityWorld().getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);

            // Send error message if the player is already at the highest block
            if (y - player.getBlockPos().getY() <= 1) {
                source.sendError(Text.literal("You are already on the surface"));
                return 0;
            }

            // Teleport the player to the highest block
            Vec3d pos_surface = new Vec3d(x + 0.5, y, z + 0.5);
            player.teleportTo(new TeleportTarget(player.getEntityWorld(), pos_surface, player.getVelocity(), player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
        }

        // Play teleport sound
        GameManager.playSoundToPlayer(player, SoundEvents.ENTITY_ENDERMAN_TELEPORT);

        return 1;
    }
}
