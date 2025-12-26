package x170.bingo.pool;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import x170.bingo.Bingo;

import java.io.IOException;

public class ItemTypeAdapter extends TypeAdapter<Item> {
    @Override
    public Item read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String str = reader.nextString();
        Item item = Registries.ITEM.get(Identifier.of(str));
        if (item.equals(Items.AIR)) {
            Bingo.LOGGER.warn("Unknown item in pool config (skipping): \"{}\"", str);
            return null;
        }
        return item;
    }

    @Override
    public void write(JsonWriter writer, Item value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value.toString());
    }
}
