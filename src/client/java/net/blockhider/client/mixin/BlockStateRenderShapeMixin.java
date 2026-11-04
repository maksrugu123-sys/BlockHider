package net.blockhider.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.blockhider.client.HiddenBlocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skip chunk geometry for selected block models. Collision, drops, mining behavior,
 * light data, and world data are left untouched.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateRenderShapeMixin {
    @ModifyReturnValue(method = "getRenderShape", at = @At("RETURN"))
    private RenderShape blockhider$hideSelectedBlocks(RenderShape original) {
        BlockState state = (BlockState) (Object) this;
        return HiddenBlocks.shouldHide(state.getBlock()) ? RenderShape.INVISIBLE : original;
    }
}
