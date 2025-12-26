package x170.bingo.pool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import x170.bingo.Bingo;
import x170.bingo.goal.Goal;
import x170.bingo.poolDefaults.*;
import x170.bingo.setting.Settings;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public abstract class PoolManager {
    private static final Pool[] POOLS = loadPools();

    public static void init() {
        // Intentionally left blank to trigger static initialization
    }

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

    public static Pool getPool(String id) {
        for (Pool pool : POOLS) {
            if (pool.getId().equals(id)) {
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

    private static Pool[] loadPools() {
        Path poolsDir = FabricLoader.getInstance().getConfigDir().resolve(Bingo.MOD_ID + "/pools");

        if (!Files.exists(poolsDir)) {
            try {
                Files.createDirectories(poolsDir);
            } catch (Exception e) {
                Bingo.LOGGER.error("Failed to create pools directory: {}", e.getMessage());
            }
        }

        try (Stream<Path> stream = Files.list(poolsDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
        ) {
            if (stream.findAny().isEmpty()) {
                Bingo.LOGGER.info("No pools found. Saving default pools...");
                saveDefaultPools(poolsDir);
            }
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to check files in pool directory: {}", e.getMessage());
        }

        ArrayList<Pool> pools = new ArrayList<>();
        try (Stream<Path> stream = Files.list(poolsDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
        ) {
            stream.forEach(filePath -> {
                if (!filePath.toString().endsWith(".json")) return;
                Pool pool = loadPool(filePath);
                if (pool != null) {
                    pools.add(pool);
                }
            });
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to load pools from directory: {}", e.getMessage());
        }

        pools.sort(Comparator.comparing(Pool::getId));

        // Load at most 18 pools
        if (pools.size() > 18) {
            Bingo.LOGGER.warn("Loaded more than 18 pools ({}). Only the first 18 will be used. Loaded pools: {}", pools.size(), Arrays.toString(pools.stream().map(Pool::getId).toArray()));
            return pools.subList(0, 18).toArray(new Pool[0]);
        }

        Bingo.LOGGER.info("Loaded {} pools: {}", pools.size(), Arrays.toString(pools.stream().map(Pool::getId).toArray()));
        return pools.toArray(new Pool[0]);
    }

    private static Pool loadPool(Path filePath) {
        // Load the pools config file
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Item.class, new ItemTypeAdapter())
                .registerTypeAdapter(EntityType.class, new EntityTypeTypeAdapter())
                .create();
        try {
            PoolConfig poolConfig = gson.fromJson(Files.readString(filePath), PoolConfig.class);
            return new Pool(filePath.getFileName().toString().replace(".json", ""), poolConfig);
        } catch (Exception e) {
            Bingo.LOGGER.error("Failed to load pool \"{}\": {}", filePath.getFileName(), e.getMessage());
        }
        return null;
    }

    private static void saveDefaultPools(Path poolsDir) {
        HashMap<String, PoolConfig> defaultPools = new HashMap<>();
        defaultPools.put("01_overworld_easy.json", new OverworldEasyPool());
        defaultPools.put("02_overworld_medium.json", new OverworldMediumPool());
        defaultPools.put("03_overworld_hard.json", new OverworldHardPool());
        defaultPools.put("04_nether.json", new NetherPool());
        defaultPools.put("05_end.json", new EndPool());
        defaultPools.put("06_extreme.json", new ExtremePool());

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Item.class, new ItemTypeAdapter())
                .registerTypeAdapter(EntityType.class, new EntityTypeTypeAdapter())
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
        for (Map.Entry<String, PoolConfig> entry : defaultPools.entrySet()) {
            try {
                String json = gson.toJson(entry.getValue());
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(poolsDir.resolve(entry.getKey())))) {
                    writer.println(json);
                    writer.flush();
                }
            } catch (Exception e) {
                Bingo.LOGGER.error("Failed to save default pool {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }
}
