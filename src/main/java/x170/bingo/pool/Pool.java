package x170.bingo.pool;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import x170.bingo.Bingo;
import x170.bingo.goal.AdvancementGoal;
import x170.bingo.goal.EntityGoal;
import x170.bingo.goal.Goal;
import x170.bingo.goal.ItemGoal;
import x170.bingo.setting.Settings;

import java.util.*;

public abstract class Pool {
    private final String name;
    private final String[] description;
    private final Item displayItem;
    private final ArrayList<ItemGoal> itemGoals = new ArrayList<>();
    private final ArrayList<EntityGoal> entityGoals = new ArrayList<>();
    private final ArrayList<AdvancementGoal> advancementGoals = new ArrayList<>();
    private boolean enabled = true;

    public Pool(String name, String[] description, Item displayItem, String[] itemIds, EntityType<?>[] entityTypes, String[] advancementIds) {
        this.name = name;
        this.description = description;
        this.displayItem = displayItem;

        // Sets are used to remove duplicates
        for (String itemId : new HashSet<>(Set.of(itemIds))) {
            itemGoals.add(new ItemGoal(Registries.ITEM.get(Identifier.of(itemId))));
        }

        for (EntityType<?> entityType : new HashSet<>(Set.of(entityTypes))) {
            entityGoals.add(new EntityGoal(entityType));
        }

        for (String advancementId : new HashSet<>(Set.of(advancementIds))) {
            AdvancementEntry advancement = Bingo.SERVER.getAdvancementLoader().get(Identifier.of(advancementId));
            if (advancement == null) continue;
            advancementGoals.add(new AdvancementGoal(advancement));
        }
    }

    public String getName() {
        return name;
    }

    public String[] getDescription() {
        return description;
    }

    public Item getDisplayItem() {
        return displayItem;
    }

    public int getItemGoalCount() {
        return itemGoals.size();
    }

    public int getEntityGoalCount() {
        return entityGoals.size();
    }

    public int getAdvancementGoalCount() {
        return advancementGoals.size();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public Goal getRandomGoal(Collection<Goal> excludedGoals) {
        ArrayList<Goal> goals = new ArrayList<>();
        if(Settings.ITEM_GOALS.getBool()) goals.addAll(itemGoals);
        if(Settings.ENTITY_GOALS.getBool()) goals.addAll(entityGoals);
        if(Settings.ADVANCEMENT_GOALS.getBool()) goals.addAll(advancementGoals);

        goals.removeAll(excludedGoals);
        if(goals.isEmpty()) return null;

        Collections.shuffle(goals);
        return goals.getFirst();
    }
}
