package x170.bingo.goal;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import x170.bingo.setting.Settings;

public interface Goal {
    String getName();
    ItemStack getDisplayItem();
    String toString();
    @Override
    boolean equals(Object obj);
    @Override
    int hashCode();

    static ItemStack generateDisplayItem(Item item, @Nullable Text name, @Nullable Text description) {
        return generateDisplayItem(item, name, description, null);
    }

    static ItemStack generateDisplayItem(Item item, @Nullable Text name, @Nullable Text description, @Nullable Identifier model) {
        ItemStack displayItem = item.getDefaultStack();

        // Set name, lore and item model
        if (name != null)
            displayItem.set(DataComponentTypes.CUSTOM_NAME, name.copy().styled(style -> style.withItalic(style.isItalic()).withColor(style.getColor() != null ? style.getColor() : TextColor.fromFormatting(Formatting.WHITE))));
        if (description != null)
            displayItem.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, description.copy().styled(style -> style.withItalic(false).withColor(TextColor.fromFormatting(Formatting.GRAY))), LoreComponent::with);
        if (Settings.USE_BINGO_RESOURCE_PACK.getBool() && model != null)
            displayItem.set(DataComponentTypes.ITEM_MODEL, model);
//        displayItem.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(floats, flags, strings, colors));

        // Hide default tooltip
        displayItem.apply(DataComponentTypes.TRIM, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.UNBREAKABLE, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.STORED_ENCHANTMENTS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.DYED_COLOR, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.CAN_BREAK, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.apply(DataComponentTypes.CAN_PLACE_ON, null, comp -> comp != null ? comp.withShowInTooltip(false) : null);
        displayItem.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return displayItem;
    }
}
