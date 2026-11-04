package net.blockhider.client.render;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Draws hidden terrain faces into the main depth attachment with all color channels disabled.
 * It runs after opaque terrain and before entities/block entities.
 */
public final class DepthMaskRenderer {
    private static final double MAX_DISTANCE = 224.0;
    private static final double MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;
    private static final int MAX_FACES_PER_FRAME = 60_000;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private static final RenderPipeline DEPTH_ONLY_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/blockhider_depth_mask")
            .withColorTargetState(new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_NONE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
            .build()
    );

    private static final StagedVertexBuffer BUFFER =
        new StagedVertexBuffer(() -> "Block Hider depth mask", 1 << 20);

    private DepthMaskRenderer() {}

    public static void initialize() {
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(DepthMaskRenderer::render);
    }

    private static void render(LevelTerrainRenderContext context) {
        Map<Long, int[]> sections = DepthMaskManager.snapshot();
        if (sections.isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack matrices = new PoseStack();
        matrices.mulPose(context.levelState().cameraRenderState.orientation);
        matrices.translate(-camera.x, -camera.y, -camera.z);

        boolean drawStarted = false;
        try {
            var format = DEPTH_ONLY_PIPELINE.getVertexFormatBinding(0);
            if (format == null || DEPTH_ONLY_PIPELINE.getPrimitiveTopology() != PrimitiveTopology.QUADS) {
                return;
            }

            StagedVertexBuffer.Draw draw = BUFFER.appendDraw(format, PrimitiveTopology.QUADS);
            drawStarted = true;
            VertexConsumer vertices = BUFFER.getVertexBuilder(draw);
            Matrix4fc pose = matrices.last().pose();

            int facesWritten = 0;
            outer:
            for (Map.Entry<Long, int[]> entry : sections.entrySet()) {
                SectionPos section = SectionPos.of(entry.getKey());
                double centerX = section.minBlockX() + 8.0 - camera.x;
                double centerY = section.minBlockY() + 8.0 - camera.y;
                double centerZ = section.minBlockZ() + 8.0 - camera.z;
                if (centerX * centerX + centerY * centerY + centerZ * centerZ > MAX_DISTANCE_SQUARED) {
                    continue;
                }

                int baseX = section.minBlockX();
                int baseY = section.minBlockY();
                int baseZ = section.minBlockZ();

                for (int packed : entry.getValue()) {
                    int faceMask = packed & 0x3F;
                    int x = baseX + ((packed >>> 14) & 15);
                    int y = baseY + ((packed >>> 10) & 15);
                    int z = baseZ + ((packed >>> 6) & 15);

                    if ((faceMask & (1 << Direction.SOUTH.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 0);
                    }
                    if ((faceMask & (1 << Direction.NORTH.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 1);
                    }
                    if ((faceMask & (1 << Direction.WEST.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 2);
                    }
                    if ((faceMask & (1 << Direction.EAST.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 3);
                    }
                    if ((faceMask & (1 << Direction.UP.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 4);
                    }
                    if ((faceMask & (1 << Direction.DOWN.ordinal())) != 0) {
                        addFace(vertices, pose, x, y, z, x + 1, y + 1, z + 1, 5);
                    }

                    facesWritten += Integer.bitCount(faceMask);
                    if (facesWritten >= MAX_FACES_PER_FRAME) {
                        break outer;
                    }
                }
            }

            BUFFER.upload();
            StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(draw);
            if (info != null) {
                draw(client, info);
            }
        } finally {
            if (drawStarted) {
                BUFFER.endFrame();
            }
            matrices.popPose();
        }
    }

    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget target = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = target.getColorTextureView();
        GpuTextureView depthTexture = target.getDepthTextureView();
        if (colorTexture == null || depthTexture == null) {
            return;
        }

        try (RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "Block Hider invisible depth pass", colorTexture, Optional.empty(), depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(DEPTH_ONLY_PIPELINE));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, info.vertexBuffer().slice());
            pass.setIndexBuffer(info.indexBuffer(), info.indexType());
            pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    /**
     * Face order: +Z, -Z, -X, +X, +Y, -Y. Culling is disabled in the pipeline.
     */
    private static void addFace(
        VertexConsumer out, Matrix4fc pose,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ,
        int face
    ) {
        switch (face) {
            case 0 -> {
                vertex(out, pose, minX, minY, maxZ);
                vertex(out, pose, maxX, minY, maxZ);
                vertex(out, pose, maxX, maxY, maxZ);
                vertex(out, pose, minX, maxY, maxZ);
            }
            case 1 -> {
                vertex(out, pose, maxX, minY, minZ);
                vertex(out, pose, minX, minY, minZ);
                vertex(out, pose, minX, maxY, minZ);
                vertex(out, pose, maxX, maxY, minZ);
            }
            case 2 -> {
                vertex(out, pose, minX, minY, minZ);
                vertex(out, pose, minX, minY, maxZ);
                vertex(out, pose, minX, maxY, maxZ);
                vertex(out, pose, minX, maxY, minZ);
            }
            case 3 -> {
                vertex(out, pose, maxX, minY, maxZ);
                vertex(out, pose, maxX, minY, minZ);
                vertex(out, pose, maxX, maxY, minZ);
                vertex(out, pose, maxX, maxY, maxZ);
            }
            case 4 -> {
                vertex(out, pose, minX, maxY, maxZ);
                vertex(out, pose, maxX, maxY, maxZ);
                vertex(out, pose, maxX, maxY, minZ);
                vertex(out, pose, minX, maxY, minZ);
            }
            case 5 -> {
                vertex(out, pose, minX, minY, minZ);
                vertex(out, pose, maxX, minY, minZ);
                vertex(out, pose, maxX, minY, maxZ);
                vertex(out, pose, minX, minY, maxZ);
            }
            default -> throw new IllegalArgumentException("Unknown cube face " + face);
        }
    }

    private static void vertex(VertexConsumer out, Matrix4fc pose, float x, float y, float z) {
        out.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void close() {
        BUFFER.close();
    }
}
