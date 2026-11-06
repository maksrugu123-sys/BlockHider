package net.blockhider.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persisted client-side visibility filters. A category set to true is passed
 * through to vanilla rendering; all other ordinary world blocks stay hidden.
 */
public final class BlockHiderConfig {
    public enum Category {
        LIQUIDS, PORTALS, FIRE, PLANTS, SPECIAL_MODELS
    }

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("block-hider.properties");

    private static boolean showLiquids = FabricLoader.getInstance().isModLoaded("blockshape");
    private static boolean showPortals;
    // Preserve the previous release behavior until the user changes portal settings.
    private static boolean showEndPortal = true;
    private static boolean showFire;
    private static boolean showPlants;
    private static boolean showSpecialModels;

    private BlockHiderConfig() {}

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) return;

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            properties.load(reader);
            showLiquids = readBoolean(properties, "showLiquids", showLiquids);
            showPortals = readBoolean(properties, "showPortals", showPortals);
            showEndPortal = readBoolean(properties, "showEndPortal", showEndPortal);
            showFire = readBoolean(properties, "showFire", showFire);
            showPlants = readBoolean(properties, "showPlants", showPlants);
            showSpecialModels = readBoolean(properties, "showSpecialModels", showSpecialModels);
        } catch (IOException | IllegalArgumentException exception) {
            BlockHiderClient.LOGGER.warn("Could not read Block Hider settings; using safe defaults", exception);
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("showLiquids", Boolean.toString(showLiquids));
        properties.setProperty("showPortals", Boolean.toString(showPortals));
        properties.setProperty("showEndPortal", Boolean.toString(showEndPortal));
        properties.setProperty("showFire", Boolean.toString(showFire));
        properties.setProperty("showPlants", Boolean.toString(showPlants));
        properties.setProperty("showSpecialModels", Boolean.toString(showSpecialModels));

        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                properties.store(writer, "Block Hider category visibility");
            }
        } catch (IOException exception) {
            BlockHiderClient.LOGGER.warn("Could not save Block Hider settings", exception);
        }
    }

    /**
     * Rebuild visible client chunk geometry after a category toggle. This is
     * event-driven (only on settings changes), not a per-frame operation.
     */
    private static void refreshWorldRendering() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            minecraft.levelExtractor.allChanged();
        }
    }

    public static boolean isEnabled(Category category) {
        return switch (category) {
            case LIQUIDS -> showLiquids;
            case PORTALS -> showPortals || showEndPortal;
            case FIRE -> showFire;
            case PLANTS -> showPlants;
            case SPECIAL_MODELS -> showSpecialModels;
        };
    }

    public static void setEnabled(Category category, boolean enabled) {
        switch (category) {
            case LIQUIDS -> showLiquids = enabled;
            case PORTALS -> {
                showPortals = enabled;
                showEndPortal = enabled;
            }
            case FIRE -> showFire = enabled;
            case PLANTS -> showPlants = enabled;
            case SPECIAL_MODELS -> showSpecialModels = enabled;
        }
        save();
        refreshWorldRendering();
    }

    public static void reset() {
        showLiquids = FabricLoader.getInstance().isModLoaded("blockshape");
        showPortals = false;
        showEndPortal = true;
        showFire = false;
        showPlants = false;
        showSpecialModels = false;
        save();
        refreshWorldRendering();
    }

    /**
     * The End Portal remains visible by default for compatibility with the
     * previous release. Other categories are independently opt-in.
     */
    public static boolean shouldShowBlock(BlockState state, RenderShape original) {
        if (state.is(Blocks.END_PORTAL)) return showEndPortal;
        if (isPortal(state)) return showPortals;
        if (original == RenderShape.INVISIBLE) return true;
        if (isFire(state)) return showFire;
        if (isPlant(state)) return showPlants;
        if (original == RenderShape.ENTITYBLOCK_ANIMATED) return showSpecialModels;
        return false;
    }

    public static boolean shouldShowBlockEntity(BlockState state) {
        if (state.is(Blocks.END_PORTAL)) return showEndPortal;
        if (isPortal(state)) return showPortals;
        if (isFire(state)) return showFire;
        // Potted plants and other plant block entities follow the Plants switch,
        // not the general Special Models switch, to avoid orphaned plant models.
        if (isPlant(state)) return showPlants;
        return showSpecialModels;
    }

    public static String portalStatus() {
        if (showPortals) return "ON";
        return showEndPortal ? "END ONLY" : "OFF";
    }

    public static boolean showLiquids() {
        return showLiquids;
    }

    private static boolean isPortal(BlockState state) {
        return state.is(BlockTags.PORTALS) || state.is(Blocks.END_GATEWAY);
    }

    private static boolean isFire(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES);
    }

    private static boolean isPlant(BlockState state) {
        Block block = state.getBlock();
        return block instanceof BushBlock
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CAVE_VINES)
                || state.is(BlockTags.CORAL_PLANTS)
                || state.is(BlockTags.FLOWER_POTS);
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }
}
