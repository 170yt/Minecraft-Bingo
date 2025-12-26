package x170.bingo.pool;

import com.google.gson.annotations.Expose;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;

import java.util.ArrayList;

public class PoolConfig {
    @Expose
    public String name;
    @Expose
    public String description;
    @Expose
    public Item displayItem;
    @Expose
    public ArrayList<Item> items;
    @Expose
    public ArrayList<EntityType> entities;
    @Expose
    public ArrayList<String> advancements;
}
