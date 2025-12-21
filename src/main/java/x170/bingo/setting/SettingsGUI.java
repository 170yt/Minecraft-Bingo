package x170.bingo.setting;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.goal.AdvancementGoal;
import x170.bingo.goal.EntityGoal;
import x170.bingo.goal.ItemGoal;
import x170.bingo.pool.Pool;
import x170.bingo.pool.PoolManager;

public class SettingsGUI extends SimpleGui {
    public SettingsGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Bingo Settings"));
        this.open();
    }

    @Override
    public void onTick() {
        for (Settings setting : Settings.values()) {
            if (setting.slot == -1) continue;

            GuiElementBuilder builder = new GuiElementBuilder(setting.item)
                    .setName(Text.literal(setting.name).formatted(Formatting.GOLD));

            if (!setting.configurableWhilePlaying && GameManager.status != GameStatus.IDLE)
                builder.addLoreLine(Text.literal("Locked while playing").formatted(Formatting.RED));

            builder.addLoreLine(Text.literal(setting.description).formatted(Formatting.GRAY))
                    .hideDefaultTooltip();

            if (setting.isBoolean) {
//                builder.glow(setting.getBool());
                if (setting.getBool()) {
                    builder.setMaxDamage(100);
                    builder.setDamage(1);
                }
            } else {
//                builder.setMaxCount(64);
//                builder.setCount(Math.max((int) setting.getDouble(), 1));
                if (setting.getDouble() != setting.defaultValue) {
                    int maxDamage = (int) (setting.max / setting.stepSize);
                    builder.setMaxDamage(maxDamage);
                    int damage = (int) (maxDamage - (setting.getDouble() - setting.min) / setting.stepSize);
                    builder.setDamage(damage == 0 ? 1 : damage);
                }
                builder.addLoreLine(Text.literal(
                        "Default: " + setting.getFormattedDefaultValue() + ", Min: " + setting.getFormattedMin() + ", Max: " + setting.getFormattedMax()
                ).formatted(Formatting.GRAY));
            }
            builder.addLoreLine(Text.literal(setting.toString())
                    .formatted(setting.isBoolean && !setting.getBool() ? Formatting.RED : Formatting.GREEN, Formatting.BOLD));

            builder.setCallback((element, clickType, slotActionType) -> {
                int multiplier;
                switch (clickType) {
                    case MOUSE_LEFT:
                        multiplier = 1;
                        break;
                    case MOUSE_RIGHT:
                        multiplier = -1;
                        break;
                    case MOUSE_LEFT_SHIFT:
                        multiplier = 10;
                        break;
                    case MOUSE_RIGHT_SHIFT:
                        multiplier = -10;
                        break;
                    default:
                        return;
                }

                if (!setting.configurableWhilePlaying && GameManager.status != GameStatus.IDLE) {
                    GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_CHEST_LOCKED, 0.5F);
                    return;
                }

                setting.increment(multiplier);

                GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
            });
            this.setSlot(setting.slot, builder);
        }

        int startSlot = 27;  // TODO: Implement dynamic slot calculation
        int currentSlot = startSlot;
        for (Pool pool : PoolManager.getPools(true)) {
             GuiElementBuilder builder = new GuiElementBuilder(pool.getDisplayItem())
                    .setName(Text.literal(pool.getName()).formatted(Formatting.GOLD));

            if (GameManager.status != GameStatus.IDLE)
                builder.addLoreLine(Text.literal("Locked while playing").formatted(Formatting.RED));

            for (String line : pool.getDescription()) {
                builder.addLoreLine(Text.literal(line).formatted(Formatting.GRAY));
            }

            MutableText itemGoalText = Text.literal(ItemGoal.DISPLAY_NAME + ": " + pool.getItemGoalCount()).formatted(Formatting.GRAY);
            MutableText entityGoalText = Text.literal(EntityGoal.DISPLAY_NAME + ": " + pool.getEntityGoalCount()).formatted(Formatting.GRAY);
            MutableText advancementGoalText = Text.literal(AdvancementGoal.DISPLAY_NAME + ": " + pool.getAdvancementGoalCount()).formatted(Formatting.GRAY);

            builder.addLoreLine(Settings.ITEM_GOALS.getBool() ? itemGoalText : itemGoalText.formatted(Formatting.STRIKETHROUGH))
                    .addLoreLine(Settings.ENTITY_GOALS.getBool() ? entityGoalText : entityGoalText.formatted(Formatting.STRIKETHROUGH))
                    .addLoreLine(Settings.ADVANCEMENT_GOALS.getBool() ? advancementGoalText : advancementGoalText.formatted(Formatting.STRIKETHROUGH))
                    .hideDefaultTooltip();

//            builder.glow(itemPool.isEnabled());
            if (pool.isEnabled()) {
                builder.setMaxDamage(100);
                builder.setDamage(1);
            }

            boolean isEnabled = pool.isEnabled();
            builder.addLoreLine(Text.literal(isEnabled ? "Enabled" : "Disabled").formatted(isEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD));

            builder.setCallback((element, clickType, slotActionType) -> {
                if (clickType != ClickType.MOUSE_LEFT &&
                        clickType != ClickType.MOUSE_RIGHT &&
                        clickType != ClickType.MOUSE_LEFT_SHIFT &&
                        clickType != ClickType.MOUSE_RIGHT_SHIFT) {
                    return;
                }

                if (GameManager.status != GameStatus.IDLE) {
                    GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_CHEST_LOCKED, 0.5F);
                    return;
                }

                pool.toggleEnabled();

                GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
            });
            this.setSlot(currentSlot, builder);
            currentSlot++;
        }

        // Stop Game Button
        GuiElementBuilder builder = new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("Stop Game").formatted(Formatting.RED))
                .addLoreLine(Text.literal(GameManager.status != GameStatus.IDLE ? "Click to stop the game" : "(!) Game is not running").formatted(Formatting.GRAY))
                .hideDefaultTooltip();

        builder.setCallback((element, clickType, slotActionType) -> {
            if (clickType != ClickType.MOUSE_LEFT && clickType != ClickType.MOUSE_RIGHT) return;

            if (GameManager.status == GameStatus.IDLE) {
                GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_CHEST_LOCKED, 0.5F);
                return;
            }

            GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
            GameManager.stopGame();
        });
        this.setSlot(48, builder);

        // Pause/Resume Game Button
        builder = new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE)
                .setName(Text.literal(GameManager.status != GameStatus.PAUSED ? "Pause Game" : "Resume Game").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal(GameManager.status == GameStatus.PAUSED ? "Click to resume the game" : GameManager.status == GameStatus.PLAYING ? "Click to pause the game" : "(!) Game is not running").formatted(Formatting.GRAY))
                .hideDefaultTooltip();

        builder.setCallback((element, clickType, slotActionType) -> {
            if (clickType != ClickType.MOUSE_LEFT && clickType != ClickType.MOUSE_RIGHT) return;

            if (GameManager.status != GameStatus.PLAYING && GameManager.status != GameStatus.PAUSED) {
                GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_CHEST_LOCKED, 0.5F);
                return;
            }

            GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
            if (GameManager.status == GameStatus.PLAYING) GameManager.pauseGame();
            else GameManager.resumeGame();
        });
        this.setSlot(49, builder);

        // Start Game Button
        builder = new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("Start Game").formatted(Formatting.GREEN))
                .addLoreLine(Text.literal(GameManager.status == GameStatus.IDLE ? "Click to start the game with the current settings" : "(!) Game is already running").formatted(Formatting.GRAY))
                .hideDefaultTooltip();

        builder.setCallback((element, clickType, slotActionType) -> {
            if (clickType != ClickType.MOUSE_LEFT && clickType != ClickType.MOUSE_RIGHT) return;

            if (GameManager.status != GameStatus.IDLE) {
                GameManager.playSoundToPlayer(player, SoundEvents.BLOCK_CHEST_LOCKED, 0.5F);
                return;
            }

            GameManager.playSoundToPlayer(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5F);
            try {
                GameManager.startGame();
            } catch (CommandSyntaxException e) {
                this.getPlayer().sendMessage(Text.literal(e.getMessage()).formatted(Formatting.RED), false);
            }
        });
        this.setSlot(50, builder);

        super.onTick();
    }
}
