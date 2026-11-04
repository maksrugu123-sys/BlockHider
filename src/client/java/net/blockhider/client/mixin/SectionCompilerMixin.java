package net.blockhider.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.blockhider.client.render.DepthMaskManager;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;

/** Captures the hidden-block mask atomically during vanilla section compilation. */
@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @WrapMethod(method = "compile")
    private SectionCompiler.Results blockhider$captureDepthMask(
        SectionPos sectionPos,
        RenderSectionRegion region,
        VertexSorting vertexSorting,
        SectionBufferBuilderPack builders,
        Operation<SectionCompiler.Results> original
    ) {
        DepthMaskManager.begin(sectionPos);
        boolean completed = false;
        try {
            SectionCompiler.Results result = original.call(sectionPos, region, vertexSorting, builders);
            completed = true;
            return result;
        } finally {
            if (completed) {
                DepthMaskManager.finish(sectionPos);
            } else {
                DepthMaskManager.abort();
            }
        }
    }
}
