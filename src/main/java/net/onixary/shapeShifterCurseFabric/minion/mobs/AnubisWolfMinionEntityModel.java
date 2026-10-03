package net.onixary.shapeShifterCurseFabric.minion.mobs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AnubisWolfMinionEntityModel extends EntityModel<WolfRenderState> {
    private static final String REAL_HEAD = "real_head";
    private static final String UPPER_BODY = "upper_body";
    private static final String REAL_TAIL = "real_tail";
    private final ModelPart head;
    private final ModelPart torso;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart neck;

    public AnubisWolfMinionEntityModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.torso = root.getChild("body");
        this.neck = root.getChild("upperBody");
        this.rightHindLeg = root.getChild("leg0");
        this.leftHindLeg = root.getChild("leg1");
        this.rightFrontLeg = root.getChild("leg2");
        this.leftFrontLeg = root.getChild("leg3");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition head = modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 14).addBox(-3.4F, -5.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 14).addBox(-2.3F, -6.0F, -0.2F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 14).addBox(1.4F, -5.0F, 0.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 14).addBox(1.3F, -6.0F, -0.2F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-1.5F, 0.9844F, -5.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 13.5F, -7.0F));

        PartDefinition body = modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-4.0F, -3.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition upperBody = modelPartData.addOrReplaceChild("upperBody", CubeListBuilder.create().texOffs(21, 0).addBox(-4.0F, -3.0F, -3.0F, 8.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(43, 18).addBox(-1.0F, -5.3F, 2.2F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition leg0 = modelPartData.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 16.0F, 7.0F));

        PartDefinition leg1 = modelPartData.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 18).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 16.0F, 7.0F));

        PartDefinition leg2 = modelPartData.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 6).addBox(-1.6F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 16.0F, -4.0F));

        PartDefinition leg3 = modelPartData.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 6).addBox(0.6F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 16.0F, -4.0F));

        PartDefinition tail = modelPartData.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(9, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 24).addBox(-0.5F, 4.0F, 0.3F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 12.0F, 8.0F));
        return LayerDefinition.create(modelData, 64, 32);
    }

    // ⚠ 26.1 的模型动画只剩一个钩子：Model#setupAnim(S state)（单参）。
    //   原先拆在两个旧签名里的逻辑全都不会被调用，模型会恒定停在建模默认姿势（表现为「没动画」）：
    //     - prepareMobModel(state, limbAngle, limbDistance) —— 26.1 已无此方法
    //     - setupAnim(state, f, g, tailAngle, headYaw, headPitch) —— 5 参版，与 26.1 的单参版只是重载、不算覆写
    //   两者在此合并为单参覆写；数据一律改从 render state 取
    //   （walkAnimationPos / walkAnimationSpeed / xRot / yRot 来自 LivingEntityRenderState，
    //     isAngry / isSitting / tailAngle 来自 WolfRenderState）。
    //   另：headParts() / bodyParts() 在 26.1 的 Model/EntityModel 中同样已不存在，一并移除。
    @Override
    public void setupAnim(WolfRenderState state) {
        super.setupAnim(state);   // 26.1 基类实现 = resetPose()

        float limbAngle = state.walkAnimationPos;
        float limbDistance = state.walkAnimationSpeed;

        if (state.isAngry) {
            this.tail.yRot = 0.0F;
        } else {
            this.tail.yRot = Mth.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        }

        if (state.isSitting) {
            this.neck.setPos(-1.0F, 16.0F, -3.0F);
            this.neck.xRot = 1.2566371F;
            this.neck.yRot = 0.0F;
            this.torso.setPos(0.0F, 18.0F, 0.0F);
            this.torso.xRot = ((float)Math.PI / 4F);
            this.tail.setPos(-1.0F, 21.0F, 6.0F);
            this.rightHindLeg.setPos(-2.5F, 22.7F, 2.0F);
            this.rightHindLeg.xRot = ((float)Math.PI * 1.5F);
            this.leftHindLeg.setPos(0.5F, 22.7F, 2.0F);
            this.leftHindLeg.xRot = ((float)Math.PI * 1.5F);
            this.rightFrontLeg.xRot = 5.811947F;
            this.rightFrontLeg.setPos(-2.49F, 17.0F, -4.0F);
            this.leftFrontLeg.xRot = 5.811947F;
            this.leftFrontLeg.setPos(0.51F, 17.0F, -4.0F);
        } else {
            this.torso.setPos(0.0F, 14.0F, 2.0F);
            this.torso.xRot = ((float)Math.PI / 2F);
            this.neck.setPos(-1.0F, 14.0F, -3.0F);
            this.neck.xRot = this.torso.xRot;
            this.tail.setPos(-1.0F, 12.0F, 8.0F);
            this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
            this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
            this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
            this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
            this.rightHindLeg.xRot = Mth.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
            this.leftHindLeg.xRot = Mth.cos(limbAngle * 0.6662F + (float)Math.PI) * 1.4F * limbDistance;
            this.rightFrontLeg.xRot = Mth.cos(limbAngle * 0.6662F + (float)Math.PI) * 1.4F * limbDistance;
            this.leftFrontLeg.xRot = Mth.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        }

        this.neck.zRot = Mth.cos(limbAngle * 0.6662F) * 0.08F;
        this.torso.zRot = Mth.cos(limbAngle * 0.6662F) * 0.16F;

        this.head.xRot = state.xRot * ((float)Math.PI / 180F);
        this.head.yRot = state.yRot * ((float)Math.PI / 180F);
        this.tail.xRot = state.tailAngle;
    }
}
