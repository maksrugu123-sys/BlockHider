package net.blockhider.client.mixin;

import net.blockhider.client.BlockHiderConfig;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Independently controls rendering of water, lava and modded fluid surfaces. */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$hideFluidsWhenDisabled(
        BlockAndTintGetter level,
        BlockPos pos,
        FluidRenderer.Output output,
        BlockState state,
        FluidState fluidState,
        CallbackInfo ci
    ) {
        if (!BlockHiderConfig.showLiquids()) {
            ci.cancel();
        }
    }
}
