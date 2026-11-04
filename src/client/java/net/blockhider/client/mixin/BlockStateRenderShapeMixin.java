package net.blockhider.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hides every block model except the End Portal.
 * This only changes client rendering; block states, collision and interaction remain intact.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateRenderShapeMixin {
    @ModifyReturnValue(method = "getRenderShape", at = @At("RETURN"))
    private RenderShape blockhider$hideWorldBlocksExceptEndPortal(RenderShape original) {
        BlockState state = (BlockState) (Object) this;
        return state.is(Blocks.END_PORTAL) ? original : RenderShape.INVISIBLE;
    }
}
