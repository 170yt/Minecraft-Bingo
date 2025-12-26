package x170.bingo.pool;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import x170.bingo.Bingo;
import x170.bingo.goal.AdvancementGoal;
import x170.bingo.goal.EntityGoal;
import x170.bingo.goal.Goal;
import x170.bingo.goal.ItemGoal;
import x170.bingo.setting.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Pool {
    private final String id;
    private final String name;
    private final String description;
    private final Item displayItem;
    private final ArrayList<ItemGoal> itemGoals = new ArrayList<>();
    private final ArrayList<EntityGoal> entityGoals = new ArrayList<>();
    private final ArrayList<AdvancementGoal> advancementGoals = new ArrayList<>();
    private boolean enabled = true;

    public Pool(String id, PoolConfig poolConfig) {
        this.id = id;
        this.name = poolConfig.name;
        this.description = poolConfig.description;
        this.displayItem = poolConfig.displayItem;

        // Sets are used to remove duplicates
        for (Item item : new HashSet<>(poolConfig.items)) {
            if (item == null) continue;
            itemGoals.add(new ItemGoal(item));
        }

        for (EntityType<?> entityType : new HashSet<>(poolConfig.entities)) {
            if (entityType == null) continue;
            entityGoals.add(new EntityGoal(entityType));
        }

        for (String advancementId : new HashSet<>(poolConfig.advancements)) {
            AdvancementEntry advancement = Bingo.SERVER.getAdvancementLoader().get(Identifier.of(advancementId));
            if (advancement == null) {
                Bingo.LOGGER.warn("Unknown advancement in pool config (skipping): \"{}\"", advancementId);
                continue;
            }
            advancementGoals.add(new AdvancementGoal(advancement));
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
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

    public Goal getRandomGoal(Collection<Goal> excluded) {
        ArrayList<Goal> goals = new ArrayList<>();
        if (Settings.ITEM_GOALS.getBool()) goals.addAll(itemGoals);
        if (Settings.ENTITY_GOALS.getBool()) goals.addAll(entityGoals);
        if (Settings.ADVANCEMENT_GOALS.getBool()) goals.addAll(advancementGoals);

        goals.removeAll(excluded);
        if (goals.isEmpty()) return null;

        Collections.shuffle(goals);
        return goals.getFirst();
    }

    public EntityGoal getRandomEntityGoal(Collection<EntityGoal> excluded) {
        if (!Settings.ENTITY_GOALS.getBool()) return null;

        ArrayList<EntityGoal> goals = new ArrayList<>(entityGoals);
        goals.removeAll(excluded);
        if (goals.isEmpty()) return null;

        Collections.shuffle(goals);
        return goals.getFirst();
    }

    public AdvancementGoal getRandomAdvancementGoal(Collection<AdvancementGoal> excluded) {
        if (!Settings.ADVANCEMENT_GOALS.getBool()) return null;

        ArrayList<AdvancementGoal> goals = new ArrayList<>(advancementGoals);
        goals.removeAll(excluded);
        if (goals.isEmpty()) return null;

        Collections.shuffle(goals);
        return goals.getFirst();
    }
}
