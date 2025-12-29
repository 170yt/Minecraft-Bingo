package x170.bingo.goal;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import x170.bingo.icon.IconManager;

public class EntityGoal implements Goal {
    public static final String DISPLAY_NAME = "Mobs";
    public static final String DISPLAY_DESCRIPTION = "Kill the given mob to complete the goal";
    public static final Item DISPLAY_ITEM = Items.CREEPER_SPAWN_EGG;
    private final String name;
    private final ItemStack displayItem;
    private final Text displayText;
    private final EntityType<?> entityType;

    public EntityGoal(EntityType<?> entityType) {
        Text name = entityType.getName();
        Item displayItem = getSpawnEgg(entityType);

        this.name = name.getString();
        this.displayItem = Goal.generateDisplayItem(
                displayItem,
                name,
                Text.literal("Kill " + this.name)
        );
        this.displayText = IconManager.getItemIcon(displayItem).append(Text.literal(this.name));
        this.entityType = entityType;
    }

    private static Item getSpawnEgg(EntityType<?> entityType) {
        String spawnEggPath = entityType.getUntranslatedName() + "_spawn_egg";
        if (Identifier.isPathValid(spawnEggPath))
            return Registries.ITEM.get(Identifier.of("minecraft", spawnEggPath));
        else
            return Items.SPAWNER;
    }

    public String getName() {
        return name;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public Text getDisplayText() {
        return displayText;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof EntityGoal entityGoal)) return false;
        return entityGoal.getEntityType().equals(entityType);
    }

    @Override
    public int hashCode() {
        return entityType.hashCode();
    }
}
