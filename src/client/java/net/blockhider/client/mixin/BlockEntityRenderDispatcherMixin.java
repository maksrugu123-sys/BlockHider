package net.blockhider.client.mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the End Portal block entity renderer and suppresses all other block entities. */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true, require = 1)
    private void blockhider$keepOnlyEndPortal(
        BlockEntity blockEntity,
        float partialTicks,
        ModelFeatureRenderer.CrumblingOverlay breakProgress,
        boolean isGloballyRendered,
        CallbackInfoReturnable<BlockEntityRenderState> cir
    ) {
        boolean isEndPortal = blockEntity instanceof TheEndPortalBlockEntity
            && !(blockEntity instanceof TheEndGatewayBlockEntity);
        if (!isEndPortal) {
            cir.setReturnValue(null);
        }
    }
}
