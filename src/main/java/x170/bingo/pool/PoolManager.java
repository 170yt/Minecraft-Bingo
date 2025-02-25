package x170.bingo.pool;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import x170.bingo.goal.Goal;
import x170.bingo.setting.Settings;

import java.util.*;

public class PoolManager {
    private static final Pool[] POOLS = {
            new OverworldEasyPool(),
            new OverworldMediumPool(),
            new OverworldHardPool(),
            new NetherPool(),
            new EndPool(),
            new ExtremePool(),
    };

    public static ArrayList<Pool> getPools(boolean includeDisabled) {
        if (includeDisabled) return new ArrayList<>(Arrays.asList(POOLS));

        ArrayList<Pool> activePools = new ArrayList<>();
        for (Pool pool : POOLS) {
            if (pool.isEnabled()) {
                activePools.add(pool);
            }
        }
        return activePools;
    }

    public static Pool getPool(String name) {
        for (Pool pool : POOLS) {
            if (pool.getName().equals(name)) {
                return pool;
            }
        }
        return null;
    }

    public static ArrayList<Goal> generateGoals(int amount) throws CommandSyntaxException {
        // Set is used to ensure unique goals
        Set<Goal> activeGoalsSet = new HashSet<>();
        ArrayList<Pool> pools = getPools(false);

        if (pools.isEmpty())
            throw new SimpleCommandExceptionType(Text.literal("No pools enabled. Enable at least one goal pool")).create();

        if (!Settings.ITEM_GOALS.getBool() && !Settings.ENTITY_GOALS.getBool() && !Settings.ADVANCEMENT_GOALS.getBool())
            throw new SimpleCommandExceptionType(Text.literal("No goal types enabled. Enable at least one goal type")).create();

        // Get goals equally distributed from each pool
        Iterator<Pool> poolIterator = pools.iterator();
        while (activeGoalsSet.size() < amount) {
            if (!poolIterator.hasNext()) {
                poolIterator = pools.iterator();
            }

            Pool pool = poolIterator.next();
            Goal goal = pool.getRandomGoal(activeGoalsSet);

            if (goal == null) pools.remove(pool);  // Remove pool for next iteration
            else activeGoalsSet.add(goal);

            if (pools.isEmpty())
                throw new SimpleCommandExceptionType(
                        Text.literal("Not enough active goals (wanted " + amount + ", got " + activeGoalsSet.size() + "). Reduce the goal-amount or enable more goal-pools or goal-types")
                ).create();
        }
        return new ArrayList<>(activeGoalsSet);
    }
}
