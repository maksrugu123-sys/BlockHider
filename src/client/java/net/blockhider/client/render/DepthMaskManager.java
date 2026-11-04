package net.blockhider.client.render;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

/**
 * Collects only exposed faces of hidden solid blocks while vanilla compiles a chunk section.
 * The map stores immutable compact arrays so the render thread never observes a partial build.
 */
public final class DepthMaskManager {
    private static final ConcurrentHashMap<Long, int[]> SECTION_MASKS = new ConcurrentHashMap<>();
    private static final ThreadLocal<SectionBuilder> CURRENT = new ThreadLocal<>();

    private DepthMaskManager() {}

    public static void begin(SectionPos sectionPos) {
        CURRENT.set(new SectionBuilder(sectionPos.asLong()));
    }

    /** faceMask uses Direction ordinal bits. */
    public static void record(BlockPos pos, int faceMask) {
        SectionBuilder builder = CURRENT.get();
        if (builder == null || faceMask == 0) {
            return;
        }

        int packed = ((pos.getX() & 15) << 14)
            | ((pos.getY() & 15) << 10)
            | ((pos.getZ() & 15) << 6)
            | (faceMask & 0x3F);
        builder.positions.add(packed);
    }

    public static void finish(SectionPos sectionPos) {
        SectionBuilder builder = CURRENT.get();
        CURRENT.remove();

        if (builder == null || builder.sectionKey != sectionPos.asLong()) {
            return;
        }

        if (builder.positions.isEmpty()) {
            SECTION_MASKS.remove(builder.sectionKey);
        } else {
            SECTION_MASKS.put(builder.sectionKey, builder.positions.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public static void abort() {
        CURRENT.remove();
    }

    public static Map<Long, int[]> snapshot() {
        return Map.copyOf(SECTION_MASKS);
    }

    public static void removeChunk(int chunkX, int chunkZ) {
        SECTION_MASKS.keySet().removeIf(sectionKey ->
            (SectionPos.x(sectionKey) >> 1) == chunkX
                && (SectionPos.z(sectionKey) >> 1) == chunkZ
        );
    }

    public static void clear() {
        SECTION_MASKS.clear();
        CURRENT.remove();
    }

    private static final class SectionBuilder {
        private final long sectionKey;
        private final ArrayList<Integer> positions = new ArrayList<>();

        private SectionBuilder(long sectionKey) {
            this.sectionKey = sectionKey;
        }
    }
}
