package net.blockhider.client.mixin;

import net.blockhider.client.BlockHiderConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Category-aware filtering for block-entity renderers such as chests and campfires. */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$filterBlockEntitiesByCategory(
        BlockEntity blockEntity,
        float partialTicks,
        ModelFeatureRenderer.CrumblingOverlay breakProgress,
        boolean isGloballyRendered,
        CallbackInfoReturnable<BlockEntityRenderState> cir
    ) {
        if (!BlockHiderConfig.shouldShowBlockEntity(blockEntity.getBlockState())) {
            cir.setReturnValue(null);
        }
    }
}
