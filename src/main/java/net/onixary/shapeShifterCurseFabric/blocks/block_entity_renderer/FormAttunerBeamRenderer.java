package net.onixary.shapeShifterCurseFabric.blocks.block_entity_renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.FormAttunerBlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Form Attuner 的信标光柱渲染器。
 *
 * <p>1.21.11 起方块实体渲染改为「提取渲染状态 + 提交」两段式（与实体渲染一致），
 * 不再是 1.21.1 的 {@code render(T, float, PoseStack, MultiBufferSource, int, int)}：
 * <ul>
 *   <li>{@code createRenderState()} / {@code extractRenderState(...)} 负责把方块实体数据抄进渲染状态；</li>
 *   <li>{@code submit(...)} 在提交阶段用 {@link SubmitNodeCollector} 画几何体。</li>
 * </ul>
 * 渲染状态直接复用原版的 {@link BeaconRenderState}（本类本就是原版 BeaconRenderer 的翻版），
 * 其 {@code Section} 的颜色已从 float[] 分量改为 ARGB int。
 * 几何体部分逐行对照 1.21.11 的 {@code BeaconRenderer} 移植。
 */
public class FormAttunerBeamRenderer implements BlockEntityRenderer<FormAttunerBlockEntity, BeaconRenderState> {
    public static final Identifier BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");

    public FormAttunerBeamRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public BeaconRenderState createRenderState() {
        return new BeaconRenderState();
    }

    @Override
    public void extractRenderState(FormAttunerBlockEntity blockEntity, BeaconRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);
        state.animationTime = blockEntity.getLevel() != null
                ? (float) Math.floorMod(blockEntity.getLevel().getGameTime(), 40L) + partialTick
                : 0.0F;
        state.sections = blockEntity.getBeamSegments().stream()
                .map(segment -> new BeaconRenderState.Section(toArgb(segment.getColor()), segment.getHeight()))
                .toList();
        // 上游写法不做信标那种「离远了放大光柱」的处理，保持恒定半径
        state.beamRadiusScale = 1.0F;
    }

    /** 方块实体里的光柱颜色是 0..1 的 float 分量（源自 DyeColor），这里转成渲染状态要的 ARGB int。 */
    private static int toArgb(float[] color) {
        return ARGB.color(
                Mth.clamp((int) (color[0] * 255.0F), 0, 255),
                Mth.clamp((int) (color[1] * 255.0F), 0, 255),
                Mth.clamp((int) (color[2] * 255.0F), 0, 255));
    }

    @Override
    public void submit(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        int yOffset = 0;
        for (int i = 0; i < state.sections.size(); ++i) {
            BeaconRenderState.Section section = state.sections.get(i);
            // 上游：最后一段光柱高度写死 1024（一路顶到天上），其余用实际段高
            submitBeam(poseStack, submitNodeCollector, state.beamRadiusScale, state.animationTime, yOffset,
                    i == state.sections.size() - 1 ? 1024 : section.height(), section.color());
            yOffset += section.height();
        }
    }

    private static void submitBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float radiusScale, float animationTime, int yOffset, int maxY, int color) {
        submitBeam(poseStack, submitNodeCollector, BEAM_TEXTURE, radiusScale, animationTime, yOffset, maxY, color, 0.2F, 0.25F);
    }

    public static void submitBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier texture, float radiusScale, float animationTime, int yOffset, int maxY, int color, float innerRadius, float outerRadius) {
        int endY = yOffset + maxY;
        float inner = innerRadius * radiusScale;
        float outer = outerRadius * radiusScale;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float scroll = maxY < 0 ? animationTime : -animationTime;
        float vOffset = Mth.frac(scroll * 0.2F - Mth.floor(scroll * 0.1F));
        float z = -1.0F + vOffset;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime * 2.25F - 45.0F));
        // 内层光柱（不透明）
        submitPart(poseStack, submitNodeCollector, RenderTypes.beaconBeam(texture, false), color, yOffset, endY,
                0.0F, inner, inner, 0.0F, -inner, 0.0F, 0.0F, -inner,
                maxY * radiusScale * (0.5F / inner) + z, z);
        poseStack.popPose();
        // 外层辉光（半透明）
        submitPart(poseStack, submitNodeCollector, RenderTypes.beaconBeam(texture, true), ARGB.color(32, color), yOffset, endY,
                -outer, -outer, outer, -outer, -outer, outer, outer, outer,
                maxY * radiusScale + z, z);
        poseStack.popPose();
    }

    /**
     * 一次 {@code submitCustomGeometry} 的包装。
     *
     * <p>单独抽出来是因为几何参数必须先落成「形参」—— 直接在 {@code submitBeam} 里写 lambda 会捕获
     * 那些需要二次赋值的局部变量（外层辉光的 8 个顶点参数与内层不同），Java 会报「必须是最终变量」。
     */
    private static void submitPart(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType,
                                   int color, int yOffset, int endY,
                                   float f, float g, float h, float l, float m, float n, float o, float p,
                                   float v1, float v2) {
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, vertexConsumer) -> renderPart(pose, vertexConsumer, color, yOffset, endY, f, g, h, l, m, n, o, p, 0.0F, 1.0F, v1, v2)
        );
    }

    private static void renderPart(PoseStack.Pose pose, VertexConsumer vertexConsumer, int color, int yOffset, int endY,
                                   float f, float g, float h, float l, float m, float n, float o, float p,
                                   float u1, float u2, float v1, float v2) {
        renderQuad(pose, vertexConsumer, color, yOffset, endY, f, g, h, l, u1, u2, v1, v2);
        renderQuad(pose, vertexConsumer, color, yOffset, endY, o, p, m, n, u1, u2, v1, v2);
        renderQuad(pose, vertexConsumer, color, yOffset, endY, h, l, o, p, u1, u2, v1, v2);
        renderQuad(pose, vertexConsumer, color, yOffset, endY, m, n, f, g, u1, u2, v1, v2);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer, int color, int yOffset, int endY,
                                   float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        addVertex(pose, vertexConsumer, color, endY, x1, z1, u2, v1);
        addVertex(pose, vertexConsumer, color, yOffset, x1, z1, u2, v2);
        addVertex(pose, vertexConsumer, color, yOffset, x2, z2, u1, v2);
        addVertex(pose, vertexConsumer, color, endY, x2, z2, u1, v1);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int color, int y, float x, float z, float u, float v) {
        vertexConsumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    // 沿用上游的「只算水平距离」写法
    @Override
    public boolean shouldRender(FormAttunerBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0)
                .closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }
}
