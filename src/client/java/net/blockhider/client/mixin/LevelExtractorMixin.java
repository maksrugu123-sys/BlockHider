package net.blockhider.client.mixin;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps mobs, players and dropped item entities in the world render list.
 * First-person hands are extracted separately by vanilla and are intentionally untouched.
 * Particle extraction is also left untouched.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
    @ModifyExpressionValue(
        method = "extractVisibleEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"
        ),
        require = 1
    )
    private Iterable<Entity> blockhider$keepMobsPlayersAndDroppedItems(Iterable<Entity> entities) {
        // Filter lazily: this hook runs during world extraction every frame.
        // Avoid copying all visible entities into a fresh ArrayList each frame.
        return () -> new Iterator<>() {
            private final Iterator<Entity> source = entities.iterator();
            private Entity next;
            private boolean prepared;

            private void prepareNext() {
                if (prepared) return;
                while (source.hasNext()) {
                    Entity candidate = source.next();
                    if (candidate instanceof Mob || candidate instanceof Player || candidate instanceof ItemEntity) {
                        next = candidate;
                        prepared = true;
                        return;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                prepareNext();
                return prepared;
            }

            @Override
            public Entity next() {
                if (!hasNext()) throw new NoSuchElementException();
                Entity result = next;
                next = null;
                prepared = false;
                return result;
            }
        };
    }
}
