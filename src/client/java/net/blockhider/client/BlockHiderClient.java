package net.blockhider.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockHiderClient implements ClientModInitializer {
    public static final String MOD_ID = "blockhider";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Block Hider loaded. Common terrain blocks and vanilla ores are hidden client-side.");
    }
}
