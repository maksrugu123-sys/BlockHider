package net.blockhider.client.mixin;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps mobs and players in the world render list.
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
    private Iterable<Entity> blockhider$keepMobsAndPlayers(Iterable<Entity> entities) {
        List<Entity> visible = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Mob || entity instanceof Player) {
                visible.add(entity);
            }
        }
        return visible;
    }
}
