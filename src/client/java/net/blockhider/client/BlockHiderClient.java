package net.blockhider.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockHiderClient implements ClientModInitializer {
    public static final String MOD_ID = "blockhider";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        BlockHiderConfig.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("bhider")
                        .executes(context -> showStatus(context.getSource()))
                        .then(ClientCommands.literal("status")
                                .executes(context -> showStatus(context.getSource())))
                        .then(ClientCommands.literal("reset")
                                .executes(context -> {
                                    BlockHiderConfig.reset();
                                    context.getSource().sendFeedback(Component.literal(
                                            "[BlockHider] Категории возвращены к настройкам по умолчанию."
                                    ));
                                    return showStatus(context.getSource());
                                }))
                        .then(ClientCommands.literal("category")
                                .then(categoryCommand("liquids", BlockHiderConfig.Category.LIQUIDS))
                                .then(categoryCommand("portals", BlockHiderConfig.Category.PORTALS))
                                .then(categoryCommand("fire", BlockHiderConfig.Category.FIRE))
                                .then(categoryCommand("plants", BlockHiderConfig.Category.PLANTS))
                                .then(categoryCommand("special", BlockHiderConfig.Category.SPECIAL_MODELS)))
                )
        );

        LOGGER.info("Block Hider loaded. Category visibility controls are ready.");
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> categoryCommand(
            String name,
            BlockHiderConfig.Category category
    ) {
        return ClientCommands.literal(name)
                .then(ClientCommands.literal("on").executes(context ->
                        setCategory(context.getSource(), category, true)))
                .then(ClientCommands.literal("off").executes(context ->
                        setCategory(context.getSource(), category, false)));
    }

    private static int setCategory(
            FabricClientCommandSource source,
            BlockHiderConfig.Category category,
            boolean enabled
    ) {
        BlockHiderConfig.setEnabled(category, enabled);
        source.sendFeedback(Component.literal(
                "[BlockHider] " + categoryName(category) + ": " + (enabled ? "ON" : "OFF")
        ));
        return 1;
    }

    private static int showStatus(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal(
                "[BlockHider] Жидкости: " + state(BlockHiderConfig.Category.LIQUIDS)
                        + " | Порталы: " + state(BlockHiderConfig.Category.PORTALS)
                        + " | Огонь: " + state(BlockHiderConfig.Category.FIRE)
        ));
        source.sendFeedback(Component.literal(
                "[BlockHider] Растения: " + state(BlockHiderConfig.Category.PLANTS)
                        + " | Специальные модели: " + state(BlockHiderConfig.Category.SPECIAL_MODELS)
        ));
        source.sendFeedback(Component.literal(
                "[BlockHider] /bhider category <liquids|portals|fire|plants|special> <on|off> | /bhider reset"
        ));
        return 1;
    }

    private static String state(BlockHiderConfig.Category category) {
        return BlockHiderConfig.isEnabled(category) ? "ON" : "OFF";
    }

    private static String categoryName(BlockHiderConfig.Category category) {
        return switch (category) {
            case LIQUIDS -> "Жидкости";
            case PORTALS -> "Порталы";
            case FIRE -> "Огонь";
            case PLANTS -> "Растения";
            case SPECIAL_MODELS -> "Специальные модели";
        };
    }
}
