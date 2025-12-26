package x170.bingo.pool;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.EntityType;
import x170.bingo.Bingo;

import java.io.IOException;
import java.util.Optional;

public class EntityTypeTypeAdapter extends TypeAdapter<EntityType<?>> {
    @Override
    public EntityType<?> read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String entityTypeId = reader.nextString();
        Optional<EntityType<?>> entityType = EntityType.get(entityTypeId);
        if (entityType.isEmpty()) {
            Bingo.LOGGER.warn("Unknown entity in pool config (skipping): \"{}\"", entityTypeId);
            return null;
        }
        return entityType.get();
    }

    @Override
    public void write(JsonWriter writer, EntityType<?> value) throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(EntityType.getId(value).toString());
    }
}
