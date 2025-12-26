package x170.bingo.setting;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import x170.bingo.Bingo;
import x170.bingo.goal.AdvancementGoal;
import x170.bingo.goal.EntityGoal;
import x170.bingo.goal.ItemGoal;

import java.text.DecimalFormat;

public enum Settings {
    USE_BINGO_RESOURCE_PACK(true),
    PVP(0, Items.DIAMOND_SWORD, "PvP", "Enable player vs player combat",
            false, true, SettingsManager::onPvPChange),
    NO_DAMAGE(1, Items.GOLDEN_APPLE, "No Damage", "Players do not take damage",
            false, true, SettingsManager::applySettingsToAllPlayers),
    NO_HUNGER(2, Items.COOKED_BEEF, "No Hunger", "Players do not lose hunger",
            false, true, SettingsManager::applySettingsToAllPlayers),
    KEEP_INVENTORY(3, Items.TOTEM_OF_UNDYING, "Keep Inventory", "Keep inventory on death",
            false, true, SettingsManager::onKeepInventoryChange),
    ALLOW_FLYING(4, Items.ELYTRA, "Allow Flying", "Enable creative flight for players",
            false, true, SettingsManager::applySettingsToAllPlayers),
    BLOCK_BREAK_SPEED(6, Items.DIAMOND_PICKAXE, "Block Break Speed", "Multiplier for block break speed",
            1, 0.5, 20, 0.5, true, SettingsManager::applySettingsToAllPlayers),
    MOVEMENT_SPEED(7, Items.LEATHER_BOOTS, "Movement Speed", "Multiplier for movement speed",
            1, 0.1, 5, 0.1, true, SettingsManager::applySettingsToAllPlayers),
    STEP_HEIGHT(8, Items.LEATHER_LEGGINGS, "Step Height", "Additional step height in blocks",
            0, 0, 9, 1, true, SettingsManager::applySettingsToAllPlayers),
    COMMAND_TOP(9, Items.ENDER_PEARL, "Command Top", "Enable the /top command",
            true, true, null),
    BACKPACK(10, Items.ENDER_CHEST, "Team Backpack", "Enable the /backpack command",
            true, true, null),
    GOAL_AMOUNT(22, Items.PAPER, "Goal Amount", "The amount of goals each team needs to complete",
            27, 1, 54, 1, false, null),
    ITEM_GOALS(24, ItemGoal.DISPLAY_ITEM, "Goal Type: " + ItemGoal.DISPLAY_NAME, ItemGoal.DISPLAY_DESCRIPTION,
            true, false, null),
    ENTITY_GOALS(25, EntityGoal.DISPLAY_ITEM, "Goal Type: " + EntityGoal.DISPLAY_NAME, EntityGoal.DISPLAY_DESCRIPTION,
            true, false, null),
    ADVANCEMENT_GOALS(26, AdvancementGoal.DISPLAY_ITEM, "Goal Type: " + AdvancementGoal.DISPLAY_NAME, AdvancementGoal.DISPLAY_DESCRIPTION,
            true, false, null);

    public final int slot;
    public final Item item;
    public final String name;
    public final String description;
    public final double defaultValue;
    public final double min;
    public final double max;
    public final double stepSize;
    public final boolean configurableWhilePlaying;
    public final boolean isBoolean;
    @Nullable
    private final Runnable onValueChange;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private double value;

    // Boolean hidden
    Settings(boolean defaultValue) {
        this(-1, null, null, null, defaultValue, false, null);
    }

    // Boolean
    Settings(int slot, Item item, String name, String description, boolean defaultValue, boolean configurableWhilePlaying, @Nullable Runnable onValueChange) {
        this(slot, item, name, description, defaultValue ? 1 : 0, 0, 1, 1, configurableWhilePlaying, onValueChange, true);
    }

    // Double
    Settings(int slot, Item item, String name, String description, double defaultValue, double min, double max, double stepSize, boolean configurableWhilePlaying, @Nullable Runnable onValueChange) {
        this(slot, item, name, description, defaultValue, min, max, stepSize, configurableWhilePlaying, onValueChange, false);
    }

    Settings(int slot, Item item, String name, String description, double defaultValue, double min, double max, double stepSize, boolean configurableWhilePlaying, @Nullable Runnable onValueChange, boolean isBoolean) {
        this.slot = slot;
        this.item = item;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.stepSize = stepSize;
        this.configurableWhilePlaying = configurableWhilePlaying;
        this.onValueChange = onValueChange;
        this.isBoolean = isBoolean;
        this.value = defaultValue;
    }

    public boolean getBool() {
        return value == 1;
    }

    public double getDouble() {
        return value;
    }

    public void setValue(boolean value) {
        setValue(value ? 1 : 0);
    }

    public void setValue(double value) {
        // Round the value to the nearest step size
        value = Math.round(value / stepSize) * stepSize;
        // Clamp the value to the min and max values
        this.value = Math.clamp(value, min, max);
        if (onValueChange != null) onValueChange.run();
        if (slot != -1)
            Bingo.SERVER.getPlayerManager().broadcast(Text.literal("§6§lSettings§r: " + name + " is now " + this), false);
    }

    public void addValue(double amount) {
        if (amount == 0) return;
        if (isBoolean) setValue(!getBool());
        else if (amount > 0 && value == max) setValue(min);
        else if (amount < 0 && value == min) setValue(max);
        else setValue(value + amount);
    }

    public void increment(double multiplier) {
        addValue(stepSize * multiplier);
    }

    public String toString() {
        if (isBoolean) return getBool() ? "Enabled" : "Disabled";
        return df.format(value);
    }

    public String getFormattedDefaultValue() {
        return df.format(defaultValue);
    }

    public String getFormattedMin() {
        return df.format(min);
    }

    public String getFormattedMax() {
        return df.format(max);
    }
}
