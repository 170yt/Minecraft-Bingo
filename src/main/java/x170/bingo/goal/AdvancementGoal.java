package x170.bingo.goal;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import x170.bingo.game.GameManager;

public class AdvancementGoal implements Goal {
    public static final String DISPLAY_NAME = "Advancements";
    public static final String DISPLAY_DESCRIPTION = "Obtain the given advancement to complete the goal";
    public static final Item DISPLAY_ITEM = Items.GRASS_BLOCK;
    private final String name;
    private final ItemStack displayItem;
    private final Text icon;
    private final AdvancementEntry advancement;

    public AdvancementGoal(AdvancementEntry advancement) {
        AdvancementDisplay advancementDisplay = advancement.value().display().get();
        Text name = advancementDisplay.getTitle();
        Item displayItem = advancementDisplay.getIcon().getItem();

        this.name = name.getString();
        this.displayItem = Goal.generateDisplayItem(
                displayItem,
                name,
                advancementDisplay.getDescription(),
                Identifier.of("bingo", displayItem.toString().split(":")[1] + "_advancement")
        );
        this.icon = GameManager.getAdvancementIcon(displayItem);
        this.advancement = advancement;
    }

    public String getName() {
        return name;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public AdvancementEntry getAdvancement() {
        return advancement;
    }

    public Text getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AdvancementGoal advancementGoal)) return false;
        return advancementGoal.getAdvancement().equals(advancement);
    }

    @Override
    public int hashCode() {
        return advancement.hashCode();
    }
}
