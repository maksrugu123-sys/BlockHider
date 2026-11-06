package net.blockhider.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.blockhider.client.BlockHiderConfig;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps selected categories on the normal vanilla render path while the rest
 * of the world remains hidden. Collision and block interaction are untouched.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateRenderShapeMixin {
    @ModifyReturnValue(method = "getRenderShape", at = @At("RETURN"))
    private RenderShape blockhider$filterWorldBlocksByCategory(RenderShape original) {
        BlockState state = (BlockState) (Object) this;
        return BlockHiderConfig.shouldShowBlock(state, original) ? original : RenderShape.INVISIBLE;
    }
}
