package x170.bingo.goal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import x170.bingo.icon.IconManager;

public class ItemGoal implements Goal {
    public static final String DISPLAY_NAME = "Items";
    public static final String DISPLAY_DESCRIPTION = "Collect the given item to complete the goal";
    public static final Item DISPLAY_ITEM = Items.STICK;
    private final String name;
    private final ItemStack displayItem;
    private final Item item;
    private final Text displayText;

    public ItemGoal(Item item) {
        this.name = item.getName().getString();
        this.displayItem = Goal.generateDisplayItem(
                item,
                null,
                Text.literal("Collect " + this.name)
        );
        this.item = item;
        this.displayText = IconManager.getItemIcon(item).append(Text.literal(this.name));
    }

    public String getName() {
        return name;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public Item getItem() {
        return item;
    }

    public Text getDisplayText() {
        return displayText;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ItemGoal itemGoal)) return false;
        return itemGoal.getItem().equals(item);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }
}
