package x170.bingo.poolDefaults;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import x170.bingo.pool.PoolConfig;

import java.util.ArrayList;
import java.util.Arrays;

public class EndPool extends PoolConfig {
    public EndPool() {
        name = "End Pool";
        description = "Fully playable in the End\n(starting gear is recommended for end only)";
        displayItem = Items.END_STONE;
        items = new ArrayList<>(
                Arrays.asList(
                        Items.BREWING_STAND,
                        Items.CHORUS_FLOWER,
                        Items.CHORUS_FRUIT,
                        Items.CHORUS_PLANT,
                        Items.DRAGON_HEAD,
                        Items.ELYTRA,
                        Items.END_ROD,
                        Items.END_STONE,
                        Items.END_STONE_BRICK_SLAB,
                        Items.END_STONE_BRICK_STAIRS,
                        Items.END_STONE_BRICK_WALL,
                        Items.END_STONE_BRICKS,
                        Items.OBSIDIAN,
                        Items.POTION,
                        Items.PURPUR_BLOCK,
                        Items.PURPUR_PILLAR,
                        Items.PURPUR_SLAB,
                        Items.PURPUR_STAIRS,
                        Items.SADDLE,
                        Items.SHULKER_BOX,
                        Items.SHULKER_SHELL
                )
        );
        entities = new ArrayList<>(
                Arrays.asList(
                        EntityType.ENDER_DRAGON,
                        EntityType.ENDERMAN,
                        EntityType.ENDERMITE,
                        EntityType.SHULKER
                )
        );
        advancements = new ArrayList<>(
                Arrays.asList(
                        "minecraft:end/elytra",
                        "minecraft:end/enter_end_gateway",
                        "minecraft:end/find_end_city",
                        "minecraft:end/kill_dragon",
                        "minecraft:end/levitate",
                        "minecraft:end/respawn_dragon",
                        "minecraft:nether/brew_potion"
                )
        );
    }
}
