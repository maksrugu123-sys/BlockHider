package net.blockhider.client.mixin;

import net.blockhider.client.HiddenBlocks;
import net.blockhider.client.render.DepthMaskManager;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remove selected models from the chunk mesh, but collect an invisible depth-only
 * surface for their exposed solid faces. This keeps terrain occlusion without color.
 */
@Mixin(ModelBlockRenderer.class)
public abstract class HiddenBlockModelMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$hideModel(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        BlockStateModel model,
        long seed,
        CallbackInfo ci
    ) {
        if (!HiddenBlocks.shouldHide(state.getBlock())) {
            return;
        }

        // Solid terrain and leaves get a full-block depth mask. Plants and other
        // non-solid models are hidden, but do not create an artificial cube wall.
        if (state.isSolidRender() || state.is(BlockTags.LEAVES)) {
            int faceMask = 0;
            for (Direction direction : Direction.values()) {
                BlockState neighbour = level.getBlockState(pos.relative(direction));
                boolean hiddenSolidNeighbour = HiddenBlocks.shouldHide(neighbour.getBlock())
                    && (neighbour.isSolidRender() || neighbour.is(BlockTags.LEAVES));
                if (!hiddenSolidNeighbour) {
                    faceMask |= 1 << direction.ordinal();
                }
            }
            DepthMaskManager.record(pos, faceMask);
        }

        ci.cancel();
    }
}
