package net.blockhider.client.mixin;

import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Water is emitted by the dedicated fluid renderer, not by a block model.
 * Cancel only water tessellation; lava and other fluids are left untouched.
 */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$hideWater(
        BlockAndTintGetter level,
        BlockPos pos,
        FluidRenderer.Output output,
        BlockState state,
        FluidState fluidState,
        CallbackInfo ci
    ) {
        if (fluidState.is(FluidTags.WATER)) {
            ci.cancel();
        }
    }
}
