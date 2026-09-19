package net.onixary.shapeShifterCurseFabric.blocks.block_entity_renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;

import java.util.List;

public class FormAttunerBeamRenderer implements BlockEntityRenderer<FormAttunerBlockEntity> {
    public static final ResourceLocation BEAM_TEXTURE = ResourceLocation.parse("textures/entity/beacon_beam.png");

    public FormAttunerBeamRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(FormAttunerBlockEntity FormAttunerBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
        long l = FormAttunerBlockEntity.getLevel().getGameTime();
        List<FormAttunerBlockEntity.BeamSegment> list = FormAttunerBlockEntity.getBeamSegments();
        int k = 0;
        for (int m = 0; m < list.size(); ++m) {
            FormAttunerBlockEntity.BeamSegment beamSegment = list.get(m);
            renderBeam(matrixStack, vertexConsumerProvider, f, l, k, m == list.size() - 1 ? 1024 : beamSegment.getHeight(), beamSegment.getColor());
            k += beamSegment.getHeight();
        }
    }

    private static void renderBeam(PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, float[] color) {
        renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0f, worldTime, yOffset, maxY, color, 0.2f, 0.25f);
    }

    public static void renderBeam(PoseStack matrices, MultiBufferSource vertexConsumers, ResourceLocation textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, float[] color, float innerRadius, float outerRadius) {
        int i = yOffset + maxY;
        matrices.pushPose();
        matrices.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(worldTime, 40) + tickDelta;
        float g = maxY < 0 ? f : -f;
        float h = Mth.frac(g * 0.2f - (float) Mth.floor(g * 0.1f));
        float j = color[0];
        float k = color[1];
        float l = color[2];
        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(f * 2.25f - 45.0f));
        float m = 0.0f;
        float n = innerRadius;
        float o = innerRadius;
        float p = 0.0f;
        float q = -innerRadius;
        float r = 0.0f;
        float s = 0.0f;
        float t = -innerRadius;
        float u = 0.0f;
        float v = 1.0f;
        float w = -1.0f + h;
        float x = (float)maxY * heightScale * (0.5f / innerRadius) + w;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderType.beaconBeam(textureId, false)), j, k, l, 1.0f, yOffset, i, 0.0f, n, o, 0.0f, q, 0.0f, 0.0f, t, 0.0f, 1.0f, x, w);
        matrices.popPose();
        m = -outerRadius;
        n = -outerRadius;
        o = outerRadius;
        p = -outerRadius;
        q = -outerRadius;
        r = outerRadius;
        s = outerRadius;
        t = outerRadius;
        u = 0.0f;
        v = 1.0f;
        w = -1.0f + h;
        x = (float)maxY * heightScale + w;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderType.beaconBeam(textureId, true)), j, k, l, 0.125f, yOffset, i, m, n, o, p, q, r, s, t, 0.0f, 1.0f, x, w);
        matrices.popPose();
    }

    private static void renderBeamLayer(PoseStack matrices, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        // 1.21.1 Mojmap：Pose.getPositionMatrix()/getNormalMatrix() 改名为 pose()/normal()；
        // 且 VertexConsumer.setNormal 收的是 PoseStack.Pose（不是 Matrix3f），所以整个渲染链传 Pose。
        PoseStack.Pose entry = matrices.last();
        renderBeamFace(entry, vertices, red, green, blue, alpha, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
        renderBeamFace(entry, vertices, red, green, blue, alpha, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
        renderBeamFace(entry, vertices, red, green, blue, alpha, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
        renderBeamFace(entry, vertices, red, green, blue, alpha, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    private static void renderBeamFace(PoseStack.Pose entry, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(entry, vertices, red, green, blue, alpha, height, x1, z1, u2, v1);
        renderBeamVertex(entry, vertices, red, green, blue, alpha, yOffset, x1, z1, u2, v2);
        renderBeamVertex(entry, vertices, red, green, blue, alpha, yOffset, x2, z2, u1, v2);
        renderBeamVertex(entry, vertices, red, green, blue, alpha, height, x2, z2, u1, v1);
    }

    private static void renderBeamVertex(PoseStack.Pose entry, VertexConsumer vertices, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        vertices.addVertex(entry.pose(), x, y, z).setColor(red, green, blue, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(entry, 0.0f, 1.0f, 0.0f);
    }

    // Yarn→Mojmap 改名：rendersOutsideBoundingBox → shouldRenderOffScreen
    @Override
    public boolean shouldRenderOffScreen(FormAttunerBlockEntity blockEntity) {
        return true;
    }

    // Yarn→Mojmap 改名：getRenderDistance → getViewDistance
    @Override
    public int getViewDistance() {
        return 256;
    }

    // Yarn→Mojmap 改名：isInRenderDistance → shouldRender
    // （原版默认实现用的是 getBlockPos()，这里沿用上游的「只算水平距离」写法）
    @Override
    public boolean shouldRender(FormAttunerBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0)
            .closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }
}
