package x170.bingo.pool;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;

public class EndPool extends Pool {
    public EndPool() {
        super (
                "End Pool",
                new String[] {
                        "Playable fully in the End",
                        "(starting gear is recommended for end only)"
                },
                Items.END_STONE,
                new String[] {
                        "minecraft:brewing_stand",
                        "minecraft:chorus_flower",
                        "minecraft:chorus_fruit",
                        "minecraft:chorus_plant",
                        "minecraft:dragon_head",
                        "minecraft:elytra",
                        "minecraft:end_rod",
                        "minecraft:end_stone",
                        "minecraft:end_stone_brick_slab",
                        "minecraft:end_stone_brick_stairs",
                        "minecraft:end_stone_brick_wall",
                        "minecraft:end_stone_bricks",
                        "minecraft:obsidian",
                        "minecraft:potion",
                        "minecraft:purpur_block",
                        "minecraft:purpur_pillar",
                        "minecraft:purpur_slab",
                        "minecraft:purpur_stairs",
                        "minecraft:saddle",
                        "minecraft:shulker_box",
                        "minecraft:shulker_shell",
                },
                new EntityType<?>[] {
                        EntityType.ENDER_DRAGON,
                        EntityType.ENDERMAN,
                        EntityType.ENDERMITE,
                        EntityType.SHULKER,
                },
                new String[] {
                        "minecraft:end/elytra",
                        "minecraft:end/enter_end_gateway",
                        "minecraft:end/find_end_city",
                        "minecraft:end/kill_dragon",
                        "minecraft:end/levitate",
                        "minecraft:end/respawn_dragon",
                        "minecraft:nether/brew_potion",
                }
        );
    }
}
