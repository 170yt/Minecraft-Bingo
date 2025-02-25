package x170.bingo.team;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import x170.bingo.goal.AdvancementGoal;
import x170.bingo.goal.EntityGoal;
import x170.bingo.goal.Goal;
import x170.bingo.goal.ItemGoal;

import java.util.ArrayList;

public class TeamGoalManager {
//    TODO: Consider using a GoalType enum
//    public enum GoalType {
//        ItemGoal,
//        EntityGoal,
//        AdvancementGoal
//    }
    private final ArrayList<ItemGoal> itemGoals;
    private final int initialItemGoalCount;
    private final ArrayList<EntityGoal> entityGoals;
    private final int initialEntityGoalCount;
    private final ArrayList<AdvancementGoal> advancementGoals;
    private final int initialAdvancementGoalCount;

    public TeamGoalManager(ArrayList<Goal> goals) {
        this.itemGoals = new ArrayList<>();
        this.entityGoals = new ArrayList<>();
        this.advancementGoals = new ArrayList<>();

        for (Goal goal : goals) {
            switch (goal) {
                case ItemGoal itemGoal -> itemGoals.add(itemGoal);
                case EntityGoal entityGoal -> entityGoals.add(entityGoal);
                case AdvancementGoal advancementGoal -> advancementGoals.add(advancementGoal);
                default -> throw new IllegalStateException("Unexpected value: " + goal);
            }
        }

        this.initialItemGoalCount = this.itemGoals.size();
        this.initialEntityGoalCount = this.entityGoals.size();
        this.initialAdvancementGoalCount = this.advancementGoals.size();
    }

    public TeamGoalManager(TeamGoalManager teamGoalManager) {
        this.itemGoals = new ArrayList<>(teamGoalManager.itemGoals);
        this.initialItemGoalCount = teamGoalManager.initialItemGoalCount;
        this.entityGoals = new ArrayList<>(teamGoalManager.entityGoals);
        this.initialEntityGoalCount = teamGoalManager.initialEntityGoalCount;
        this.advancementGoals = new ArrayList<>(teamGoalManager.advancementGoals);
        this.initialAdvancementGoalCount = teamGoalManager.initialAdvancementGoalCount;
    }

    public boolean removeGoal(Goal goal) {
        return switch (goal) {
            case ItemGoal itemGoal -> itemGoals.remove(itemGoal);
            case EntityGoal entityGoal -> entityGoals.remove(entityGoal);
            case AdvancementGoal advancementGoal -> advancementGoals.remove(advancementGoal);
            default -> false;
        };
    }

    public Goal getGoal(Object goalObject) {
        return switch (goalObject) {
            case ItemStack itemStack -> itemGoals.stream().filter(itemGoal -> itemGoal.getItem().equals(itemStack.getItem())).findFirst().orElse(null);
            case EntityType<?> entityType -> entityGoals.stream().filter(entityGoal -> entityGoal.getEntityType().equals(entityType)).findFirst().orElse(null);
            case AdvancementEntry advancement -> advancementGoals.stream().filter(advancementGoal -> advancementGoal.getAdvancement().equals(advancement)).findFirst().orElse(null);
            default -> null;
        };
    }

    public ArrayList<Goal> getRemainingGoals() {
        ArrayList<Goal> goals = new ArrayList<>();
        goals.addAll(itemGoals);
        goals.addAll(entityGoals);
        goals.addAll(advancementGoals);
        // This orders the goals by their type
        return goals;
    }

    public int getScore() {
        return initialItemGoalCount - itemGoals.size()
                + initialEntityGoalCount - entityGoals.size()
                + initialAdvancementGoalCount - advancementGoals.size();
    }

    public int getScoreMax() {
        return initialItemGoalCount
                + initialEntityGoalCount
                + initialAdvancementGoalCount;
    }

    public String getScorePrintable() {
        return getScore() + " / " + getScoreMax();
    }

    public boolean isComplete() {
        return itemGoals.isEmpty()
                && entityGoals.isEmpty()
                && advancementGoals.isEmpty();
    }
}
