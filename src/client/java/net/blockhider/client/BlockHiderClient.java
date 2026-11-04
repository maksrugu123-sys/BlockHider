package net.blockhider.client;

import net.blockhider.client.render.DepthMaskManager;
import net.blockhider.client.render.DepthMaskRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BlockHiderClient implements ClientModInitializer {
    public static final String MOD_ID = "blockhider";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        DepthMaskRenderer.initialize();

        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) ->
            DepthMaskManager.removeChunk(chunk.getPos().x(), chunk.getPos().z())
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DepthMaskManager.clear());

        LOGGER.info("Block Hider loaded. Hidden blocks use an invisible depth-only surface.");
    }
}
