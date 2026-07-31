package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

public class FormRenderer extends GeoObjectRenderer<FormAnimatable, Void, GeoRenderState.Impl> {
    public FormAnimatable realAnimatable = null;
    public FormModel realModel = null;

    // GL5 render pass 数据通道
    public static final DataTicket<Player> TICKET_PLAYER = DataTicket.create("ssc_player", Player.class);
    public static final DataTicket<Integer> TICKET_CHANNEL = DataTicket.create("ssc_channel", Integer.class);
    public static final DataTicket<Float> TICKET_LIMB_ANGLE = DataTicket.create("ssc_limb_angle", Float.class);
    public static final DataTicket<Float> TICKET_LIMB_DISTANCE = DataTicket.create("ssc_limb_distance", Float.class);
    public static final DataTicket<Float> TICKET_HEAD_YAW = DataTicket.create("ssc_head_yaw", Float.class);
    public static final DataTicket<Float> TICKET_HEAD_PITCH = DataTicket.create("ssc_head_pitch", Float.class);
    public static final int CHANNEL_NORMAL = 0;
    public static final int CHANNEL_FULLBRIGHT = 1;
    public static final int CHANNEL_OUTLINE = 2;

    public FormRenderer(JsonObject modelJson) {
        super(new FormModel(modelJson));
        this.realModel = (FormModel) this.model;
        this.realAnimatable = new FormAnimatable();
    }

    public void setPlayer(Player player, boolean slim) {
        this.realAnimatable.setPlayer(player);
        this.realModel.setPlayer(player, slim);
    }

    @Override
    public Identifier getTextureLocation(GeoRenderState.Impl renderState) {
        Integer channel = renderState.getGeckolibData(TICKET_CHANNEL);
        if (channel != null && channel == CHANNEL_FULLBRIGHT) {
            Identifier full = this.realModel.getFullbrightTextureResource(this.realAnimatable);
            if (full != null) return full;
        }
        return super.getTextureLocation(renderState);
    }

    @Override
    public RenderType getRenderType(GeoRenderState.Impl renderState, Identifier texture) {
        Integer channel = renderState.getGeckolibData(TICKET_CHANNEL);
        if (channel != null && channel == CHANNEL_OUTLINE) return RenderTypes.outline(texture);
        if (channel != null && channel == CHANNEL_FULLBRIGHT) return RenderTypes.entityTranslucentEmissive(texture);
        return RenderTypes.entityTranslucent(texture);
    }

    // GL5 骨骼操作挂接点：render pass 内执行 SSC 的 processAnimation（PAL 驱动 + 轴向转换 + IK）
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState.Impl> renderPassInfo, BoneSnapshots snapshots) {
        FormModel model = this.realModel;
        model.beginRenderPass(snapshots);
        try {
            Player player = renderPassInfo.getGeckolibData(TICKET_PLAYER);
            if (player == null) player = model.entity;
            if (player == null || model.AnimationSystem == null) return;
            AvatarRenderer renderer = (AvatarRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            float partialTick = renderPassInfo.renderState().getPartialTick();
            float limbAngle = renderPassInfo.getOrDefaultGeckolibData(TICKET_LIMB_ANGLE, 0f);
            float limbDistance = renderPassInfo.getOrDefaultGeckolibData(TICKET_LIMB_DISTANCE, 0f);
            float headYaw = renderPassInfo.getOrDefaultGeckolibData(TICKET_HEAD_YAW, 0f);
            float headPitch = renderPassInfo.getOrDefaultGeckolibData(TICKET_HEAD_PITCH, 0f);
            model.AnimationSystem.processAnimation(this, model, renderer, player, limbAngle, limbDistance, partialTick, 0f, headYaw, headPitch);
        } finally {
            model.endRenderPass();
        }
    }
}
