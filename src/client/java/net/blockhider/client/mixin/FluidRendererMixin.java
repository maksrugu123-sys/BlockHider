package net.blockhider.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides every fluid surface (water, lava and other fluids) on the client. */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$hideAllFluids(
        BlockAndTintGetter level,
        BlockPos pos,
        FluidRenderer.Output output,
        BlockState state,
        FluidState fluidState,
        CallbackInfo ci
    ) {
        // BlockShapeVisualizer restores fluids with static (non-animated) textures.
        // Keep the original Block Hider behavior when that companion mod is absent.
        if (!FabricLoader.getInstance().isModLoaded("blockshape")) {
            ci.cancel();
        }
    }
}
