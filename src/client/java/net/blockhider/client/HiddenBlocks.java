package net.blockhider.client;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/** Default allow-list of block IDs whose models should not be rendered. */
public final class HiddenBlocks {
    private HiddenBlocks() {}

    private static final Set<String> HIDDEN_IDS = Set.of(
        "minecraft:stone", "minecraft:cobblestone", "minecraft:deepslate",
        "minecraft:cobbled_deepslate", "minecraft:granite", "minecraft:diorite",
        "minecraft:andesite", "minecraft:tuff", "minecraft:calcite",
        "minecraft:dripstone_block", "minecraft:bedrock",
        "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:rooted_dirt",
        "minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium",
        "minecraft:mud", "minecraft:clay", "minecraft:sand", "minecraft:red_sand",
        "minecraft:gravel", "minecraft:suspicious_sand", "minecraft:suspicious_gravel",
        "minecraft:sandstone", "minecraft:cut_sandstone", "minecraft:chiseled_sandstone",
        "minecraft:red_sandstone", "minecraft:cut_red_sandstone", "minecraft:chiseled_red_sandstone",
        "minecraft:snow", "minecraft:snow_block", "minecraft:powder_snow",
        "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice",
        "minecraft:netherrack", "minecraft:basalt", "minecraft:polished_basalt",
        "minecraft:blackstone", "minecraft:polished_blackstone", "minecraft:end_stone",
        "minecraft:soul_sand", "minecraft:soul_soil",
        "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
        "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
        "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
        "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore",
        "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
        "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
        "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
        "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
        "minecraft:nether_quartz_ore"
    );

    public static boolean shouldHide(Block block) {
        return HIDDEN_IDS.contains(BuiltInRegistries.BLOCK.getKey(block).toString());
    }
}
