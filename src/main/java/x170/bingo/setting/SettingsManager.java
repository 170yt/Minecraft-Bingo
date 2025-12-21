package x170.bingo.setting;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRules;
import x170.bingo.Bingo;
import x170.bingo.game.GameManager;
import x170.bingo.game.GameStatus;
import x170.bingo.pool.Pool;
import x170.bingo.pool.PoolManager;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {
    private static final Path settingsPath = FabricLoader.getInstance().getConfigDir().resolve("bingo/bingo.settings");
    private static final String poolPrefix = "pool:";

    public static void applySettingsToPlayer(ServerPlayerEntity player) {
        boolean playing = GameManager.status == GameStatus.PLAYING;
        boolean creative = player.isCreative();

        player.setInvulnerable(!playing || Settings.NO_DAMAGE.getBool());
        attributeModifier(player, EntityAttributes.BLOCK_BREAK_SPEED, Settings.BLOCK_BREAK_SPEED.getDouble(), true);
        attributeModifier(player, EntityAttributes.MOVEMENT_SPEED, (playing || creative) ? Settings.MOVEMENT_SPEED.getDouble() : 0, true);
        attributeModifier(player, EntityAttributes.JUMP_STRENGTH, (playing || creative) ? 1 : 0, true);
        attributeModifier(player, EntityAttributes.WATER_MOVEMENT_EFFICIENCY, Settings.MOVEMENT_SPEED.getDouble(), true);
        attributeModifier(player, EntityAttributes.STEP_HEIGHT, Settings.STEP_HEIGHT.getDouble() + 1, false);

        player.getAbilities().allowFlying = (creative || (playing && Settings.ALLOW_FLYING.getBool()));
        player.sendAbilitiesUpdate();

        if (Settings.NO_HUNGER.getBool()) {
            HungerManager hungerManager = player.getHungerManager();
            hungerManager.setFoodLevel(20);
            hungerManager.setSaturationLevel(0);
        }
    }

    public static void applySettingsToAllPlayers() {
        for (ServerPlayerEntity player : Bingo.SERVER.getPlayerManager().getPlayerList()) {
            applySettingsToPlayer(player);
        }
    }

    public static void applySettingsToServer() {
        onKeepInventoryChange();
        onPvPChange();
    }

    public static void onKeepInventoryChange() {
        Bingo.SERVER.getSpawnWorld().getGameRules().setValue(GameRules.KEEP_INVENTORY, Settings.KEEP_INVENTORY.getBool(), Bingo.SERVER);
    }

    public static void onPvPChange() {
        Bingo.SERVER.getSpawnWorld().getGameRules().setValue(GameRules.PVP, Settings.PVP.getBool(), Bingo.SERVER);
    }

    private static void attributeModifier(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, double multiplier, boolean isMultiplier) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance == null) return;
        instance.clearModifiers();
        if (multiplier == 1) return;
        String attributeId = attribute.getIdAsString().split(":", 2)[1];
        // ADD_MULTIPLIED_BASE does not work when the base value is 0
        if (instance.getBaseValue() == 0) {
            instance.addTemporaryModifier(new EntityAttributeModifier(
                    Identifier.of("bingo", attributeId + "_base"),
                    0.2,
                    EntityAttributeModifier.Operation.ADD_VALUE)
            );
        }
        instance.addTemporaryModifier(new EntityAttributeModifier(
                Identifier.of("bingo", attributeId),
                multiplier - 1,
                isMultiplier ? EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE : EntityAttributeModifier.Operation.ADD_VALUE)
        );
    }

    public static void loadSettings() {
        // Load settings from file
        // Get the setting by Settings.valueOf("key") and set the value

        // If the file does not exist, create it with default settings
        if (!Files.exists(settingsPath)) {
            saveSettings();
            return;
        }

        try {
            Files.lines(settingsPath).forEach(line -> {
                String[] split = line.split("=");
                if (split[0].startsWith(poolPrefix)) {
                    String itemPoolName = split[0].substring(poolPrefix.length());
                    PoolManager.getPool(itemPoolName).setEnabled(Boolean.parseBoolean(split[1]));
                } else {
                    Settings setting = Settings.valueOf(split[0]);
                    setting.setValue(Double.parseDouble(split[1]));
                }
            });
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to load settings from file. Generating new settings file with default values");
            saveSettings();
        }
    }

    public static void saveSettings() {
        // Save settings to file
        try {
            Files.createDirectories(settingsPath.getParent());
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to create directories for settings file", e);
        }

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(settingsPath))) {
            // Save each enum ("key=value") from Settings to the file
            for (Settings setting : Settings.values())
                writer.println(setting.name() + "=" + setting.getDouble());
            for (Pool pool : PoolManager.getPools(true))
                writer.println(poolPrefix + pool.getName() + "=" + pool.isEnabled());
            writer.flush();
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to save settings to file", e);
        }
    }
}
