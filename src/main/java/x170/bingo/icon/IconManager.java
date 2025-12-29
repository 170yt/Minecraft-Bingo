package x170.bingo.icon;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.object.AtlasTextObjectContents;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import x170.bingo.Bingo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class IconManager {
    private static final Path itemIconsPath = FabricLoader.getInstance().getConfigDir().resolve("bingo/item_icon_mappings.json");
    private static HashMap<Item, String> itemIcons = null;

    public static void init() {
//        if (!Files.exists(itemIconsPath)) {
//            Bingo.LOGGER.warn("Not using icons. File does not exist: {}", itemIconsPath);
//            return;
//        }

        itemIcons = IconAtlasSprites.sprites;

        // TODO
//        try (BufferedReader reader = Files.newBufferedReader(itemIconsPath)) {
//            itemIcons = new Gson().fromJson(reader, HashMap.class);
//        } catch (Exception e) {
//            Bingo.LOGGER.error("Failed to load icons from file", e);
//        }

        Bingo.LOGGER.info("Loaded {} icons", itemIcons.size());
//        logMissingIcons();
    }

    public static boolean isUsingIcons() {
        return itemIcons != null;
    }

    public static MutableText getItemIcon(Item item) {
        if (!isUsingIcons()) return Text.empty();

        String sprite = itemIcons.get(item);
        if (sprite == null) return Text.empty();

        return Text.object(new AtlasTextObjectContents(Atlases.ITEMS, Identifier.of(sprite))).append(Text.literal(" "));
    }

    public static MutableText getAdvancementIcon(Item item) {
        if (!isUsingIcons()) return Text.empty();
        return Text.empty().append(Text.literal(" "));
    }

    private static void logMissingIcons() {
        // Used to create the Bingo Resource Pack
        ArrayList<Item> items = new ArrayList<>(Registries.ITEM.stream().toList());
        items.removeAll(itemIcons.keySet());
        Bingo.LOGGER.info("Missing icons for {} items: {}", items.size(), items);
    }
}
