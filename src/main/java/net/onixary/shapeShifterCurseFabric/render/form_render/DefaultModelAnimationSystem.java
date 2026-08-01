package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.player_animation.v3.AnimSystem;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormBodyType;
import net.onixary.shapeShifterCurseFabric.render.form_render.sub_controller.FormEyeBlinkController;
import net.onixary.shapeShifterCurseFabric.util.ClientUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import net.onixary.shapeShifterCurseFabric.util.util.CachedDataMap;
import net.onixary.shapeShifterCurseFabric.util.util.ICachedDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.GeoBone;

import java.util.*;
import java.util.function.Predicate;

public class DefaultModelAnimationSystem implements IModelAnimationSystem, IModifyHead_MAS {

    public final List<Tuple<String, String>> extraPartsMap = new ArrayList<>();

    public String leftArmGeoBoneID = "bipedLeftArm";
    public String rightArmGeoBoneID = "bipedRightArm";

    // 动画由动画库处理 没法重映射
    public String RM_HeadGeoBoneID = "bipedHead";
    public String RM_BodyGeoBoneID = "bipedBody";
    public String RM_LeftArmGeoBoneID = "bipedLeftArm";
    public String RM_RightArmGeoBoneID = "bipedRightArm";
    public String RM_LeftLegGeoBoneID = "bipedLeftLeg";
    public String RM_RightLegGeoBoneID = "bipedRightLeg";

    public FormEyeBlinkController eyeBlinkController = null;

    public partTransform headTransform = null;
    public partTransform bodyTransform = null;
    public partTransform leftArmTransform = null;
    public partTransform rightArmTransform = null;
    public partTransform leftLegTransform = null;
    public partTransform rightLegTransform = null;

    public static class partTransform {
        private final Vec3f pos;
        private final Vec3f rot;
        private final Vec3f pivot;
        public partTransform(Vec3f pos, Vec3f rot, Vec3f pivot) {
            this.pos = pos;
            this.rot = rot;
            this.pivot = pivot;
        }

        public void apply(@Nullable GeoBone bone) {
            if (bone == null) {
                return;
            }
            BoneSnapshot s = bone.frameSnapshot;
            if (s == null) {
                s = BoneSnapshot.create(bone);
                bone.frameSnapshot = s;
            }
            s.setTranslation(s.getTranslateX() + pos.x(), s.getTranslateY() + pos.y(), s.getTranslateZ() + pos.z());
            s.setRotation(s.getRotX() + rot.x(), s.getRotY() + rot.y(), s.getRotZ() + rot.z());
            // GL5 中 pivot 为只读（绑定位置由模型 JSON 决定），忽略 pivot 累加
        }

        public static partTransform of(JsonObject json) {
            Vec3f pos, rot, pivot;
            if (json.has("pos_x") && json.has("pos_y") && json.has("pos_z")) {
                pos = new Vec3f(json.get("pos_x").getAsFloat(), json.get("pos_y").getAsFloat(), json.get("pos_z").getAsFloat());
            } else {
                pos = new Vec3f(0.0F, 0.0F, 0.0F);
            }
            if (json.has("rot_x") && json.has("rot_y") && json.has("rot_z")) {
                rot = new Vec3f(json.get("rot_x").getAsFloat(), json.get("rot_y").getAsFloat(), json.get("rot_z").getAsFloat());
            } else {
                rot = new Vec3f(0.0F, 0.0F, 0.0F);
            }
            if (json.has("pivot_x") && json.has("pivot_y") && json.has("pivot_z")) {
                pivot = new Vec3f(json.get("pivot_x").getAsFloat(), json.get("pivot_y").getAsFloat(), json.get("pivot_z").getAsFloat());
            } else {
                pivot = new Vec3f(0.0F, 0.0F, 0.0F);
            }
            return new partTransform(pos, rot, pivot);
        }
    }

    public static List<String> load1DChainData(JsonObject json) {
        List<String> ChainData = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                String base = entry.getKey();
                JsonArray array = entry.getValue().getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    ChainData.add(base + "_" + array.get(i).getAsString());
                }
            }
        }
        return ChainData;
    }

    public static List<List<String>> load2DChainData(JsonObject json) {
        List<List<String>> ChainData = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                String base = entry.getKey();
                JsonArray array = entry.getValue().getAsJsonArray();
                List<String> chain = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    chain.add(base + "_" + array.get(i).getAsString());
                }
                ChainData.add(chain);
            }
        }
        return ChainData;
    }

    public NormalChainConfig tailChain = null;
    public NormalChainConfig headTailChain = null;
    public NormalChainConfig wingChainL = null;
    public NormalChainConfig wingChainR = null;

    public static class NormalChainConfig {
        public final @NotNull List<List<String>> chain;
        public Predicate<Player> condition = player -> true;
        public boolean inverted = false;

        public NormalChainConfig(@Nullable JsonObject json) {
            if (json == null) {
                this.chain = new ArrayList<>();
                return;
            }
            if (json.has("chain")) {
                this.chain = load2DChainData(json.get("chain").getAsJsonObject());
            } else {
                this.chain = new ArrayList<>();
            }
            if (json.has("condition")) {
                String conditionName = json.get("condition").getAsString();
                Identifier conditionID = Identifier.tryParse(conditionName);
                if (FormRenderUtils.conditionRegistry.containsKey(conditionID)) {
                    this.condition = FormRenderUtils.conditionRegistry.get(conditionID);
                } else {
                    ShapeShifterCurseFabric.LOGGER.warn("Unknown condition: {}", conditionName);
                }
            }
            if (json.has("inverted")) {
                this.inverted = json.get("inverted").getAsBoolean();
            }
        }

        public boolean canApply(Player player) {
            return condition.test(player) ^ inverted;
        }
    }


    /*

    "neck_config": {
      "chain": {
        "neck": [0, 1, 2]
      },
      "head": "ik_head",
      "yaw_axis": "-y",
      "pitch_axis": "x",
      "yaw_weights": [0.18, 0.27, 0.32, 0.23],
      "pitch_weights": [0.10, 0.22, 0.33, 0.35],
      "yaw_total": 1.0,
      "pitch_total": 1.0,
      "max_yaw_deg": 120.0,
      "max_pitch_up_deg": 70.0,
      "max_pitch_down_deg": 60.0
    }

     */

    public @Nullable NeckConfig neckConfig = null;

    public static class NeckConfig {
        public final List<String> chain;
        public @NotNull String headBone;
        public int yawAxis = -1;  // -1 -> 不转 0 -> +x 1 -> -x 2 -> +y 3 -> -y 4 -> +z 5 -> -z
        public int pitchAxis = -1;  // -1 -> 不转 0 -> +x 1 -> -x 2 -> +y 3 -> -y 4 -> +z 5 -> -z
        public float[] yawWeights;  // Length = len(chain) + 1
        public float[] pitchWeights;  // Length = len(chain) + 1
        public float maxYawDegree = 180.0F;
        public float maxPitchDegreeU = 180.0F;
        public float maxPitchDegreeD = 180.0F;

        public NeckConfig(@Nullable JsonObject json) throws Exception {
            if (json == null) {
                throw new Exception("neck_config is null");
            }
            if (json.has("chain")) {
                this.chain = load1DChainData(json.get("chain").getAsJsonObject());
            } else {
                throw new Exception("neck_config chain is null");
            }
            if (json.has("head")) {
                this.headBone = json.get("head").getAsString();
            } else {
                throw new Exception("neck_config head is null");
            }
            if (json.has("yaw_axis")) {
                this.yawAxis = translateAxisStringToID(json.get("yaw_axis").getAsString());
            }
            if (json.has("pitch_axis")) {
                this.pitchAxis = translateAxisStringToID(json.get("pitch_axis").getAsString());
            }
            Float fixYaw = null;
            Float fixPitch = null;
            int chainSize = this.chain.size() + 1;
            if (json.has("yaw_total")) {
                fixYaw = json.get("yaw_total").getAsFloat();
            }
            if (json.has("pitch_total")) {
                fixPitch = json.get("pitch_total").getAsFloat();
            }
            if (json.has("yaw_weights")) {
                this.yawWeights = readWeights(GsonHelper.getAsJsonArray(json, "yaw_weights", null), fixYaw, chainSize);
            }
            if (json.has("pitch_weights")) {
                this.pitchWeights = readWeights(GsonHelper.getAsJsonArray(json, "pitch_weights", null), fixPitch, chainSize);
            }
            if (json.has("max_yaw_deg")) {
                this.maxYawDegree = json.get("max_yaw_deg").getAsFloat();
            }
            if (json.has("max_pitch_up_deg")) {
                this.maxPitchDegreeU = json.get("max_pitch_up_deg").getAsFloat();
            }
            if (json.has("max_pitch_down_deg")) {
                this.maxPitchDegreeD = json.get("max_pitch_down_deg").getAsFloat();
            }
        }

        public String getRoot() {
            if (!this.chain.isEmpty()) {
                return this.chain.get(0);
            }
            return headBone;
        }

        public @Nullable GeoBone getRoot(FormModel model) {
            return model.getCachedGeoBone(getRoot());
        }

        public @Nullable GeoBone getHead(FormModel model) {
            return model.getCachedGeoBone(headBone);
        }

        public @Nullable GeoBone getBoneByChain(FormModel model, int index) {
            int chainSize = this.chain.size();
            if (index < chainSize) {
                return model.getCachedGeoBone(this.chain.get(index));
            }
            if (index == chainSize) {
                return model.getCachedGeoBone(this.headBone);
            }
            return null;
        }

        public static int translateAxisStringToID(String axis) {
            return switch (axis) {
                case "x" -> 0;
                case "-x" -> 1;
                case "y" -> 2;
                case "-y" -> 3;
                case "z" -> 4;
                case "-z" -> 5;
                default -> -1;
            };
        }

        public void setAxisRotation(@NotNull GeoBone bone, int axis, float value) {
            // GL5: GeoBone 只读，此方法逻辑已由 FormModel.setRotationAxisForBone 替代（经 render pass 会话）
        }

        public static float[] readWeights(JsonArray json, @Nullable Float total, int size) {
            float realTotal = 0.0f;
            float[] weights = new float[size];
            if (json == null) {
                if (total == null) {
                    total = 1.0f;
                }
                Arrays.fill(weights, total / size);
                return weights;
            }
            for (int i = 0; i < size; i++) {
                float weight = i < json.size() ? json.get(i).getAsFloat() : 0.0f;
                weights[i] = weight;
                realTotal += weight;
            }
            if (total != null && realTotal > 0.0001f) {
                float scale = total / realTotal;
                for (int i = 0; i < size; i++) {
                    weights[i] *= scale;
                }
            }
            return weights;
        }
    }

    private static final ICachedDataMap<UUID, Player, tailData> tailDataMap = new CachedDataMap<>(player -> new tailData(), Entity::getUUID);
    private static final ICachedDataMap<UUID, Player, neckData> neckDataMap = new CachedDataMap<>(neckData::new, Entity::getUUID);
    // 物品栏或其他需要额外渲染玩家用的数据缓存 应该就只需要一个吧
    private static final ICachedDataMap<UUID, Player, neckData> neckDataMapV = new CachedDataMap<>(neckData::new, Entity::getUUID);

    private static class tailData {
        private float tailDragAmount = 0.0F;
        private float tailDragAmountO;
        private float currentTailDragAmount = 0.0F;
        private float tailDragAmountVertical = 0.0F;
        private float tailDragAmountVerticalO;
        private float currentTailDragAmountVertical = 0.0F;
    }
    private static class neckData {
        private float headYaw;
        private float headPitch;
        private double lastRenderTick = -1d;

        neckData(Player player) {
            headYaw = Mth.wrapDegrees(player.yHeadRot - player.yBodyRot);
            headPitch = player.getXRot();
        }
    }

    /*
    "extra_parts_map": {
      "__anim_part__": "__model_part__"
    },
    "first_person_render": {
      "left_arm": "bipedLeftArm",
      "right_arm": "bipedRightArm"
    },
    "model_part_map": {
      "head": "bipedHead",
      "body": "bipedBody",
      "left_arm": "bipedLeftArm",
      "right_arm": "bipedRightArm",
      "left_leg": "bipedLeftLeg",
      "right_leg": "bipedRightLeg"
    },
    "part_extra_pos" {
      "head": {
        "pos_x": 0.0,
        "pos_y": 0.0,
        "pos_z": 0.0,
        "rot_x": 0.0,
        "rot_y": 0.0,
        "rot_z": 0.0,
        "pivot_x": 0.0,
        "pivot_y": 0.0,
        "pivot_z": 0.0
      }
    }
     */
    @Override
    public void loadConfig(@Nullable JsonObject json) {
        this.extraPartsMap.clear();
        if (json == null) {
            return;
        }
        if (json.has("extra_parts_map")) {
            JsonObject extraPartsMap = json.getAsJsonObject("extra_parts_map");
            for (String key : extraPartsMap.keySet()) {
                this.extraPartsMap.add(new Tuple<>(key, extraPartsMap.get(key).getAsString()));
            }
        }
        this.leftArmGeoBoneID = "bipedLeftArm";
        this.rightArmGeoBoneID = "bipedRightArm";
        if (json.has("first_person_render")) {
            JsonObject firstPersonRender = json.getAsJsonObject("first_person_render");
            if (firstPersonRender.has("left_arm")) {
                this.leftArmGeoBoneID = firstPersonRender.get("left_arm").getAsString();
            }
            if (firstPersonRender.has("right_arm")) {
                this.rightArmGeoBoneID = firstPersonRender.get("right_arm").getAsString();
            }
        }
        this.RM_HeadGeoBoneID = "bipedHead";
        this.RM_BodyGeoBoneID = "bipedBody";
        this.RM_LeftArmGeoBoneID = "bipedLeftArm";
        this.RM_RightArmGeoBoneID = "bipedRightArm";
        this.RM_LeftLegGeoBoneID = "bipedLeftLeg";
        this.RM_RightLegGeoBoneID = "bipedRightLeg";
        if (json.has("model_part_map")) {
            JsonObject modelPartMap = json.getAsJsonObject("model_part_map");
            if (modelPartMap.has("head")) {
                this.RM_HeadGeoBoneID = modelPartMap.get("head").getAsString();
            }
            if (modelPartMap.has("body")) {
                this.RM_BodyGeoBoneID = modelPartMap.get("body").getAsString();
            }
            if (modelPartMap.has("left_arm")) {
                this.RM_LeftArmGeoBoneID = modelPartMap.get("left_arm").getAsString();
            }
            if (modelPartMap.has("right_arm")) {
                this.RM_RightArmGeoBoneID = modelPartMap.get("right_arm").getAsString();
            }
            if (modelPartMap.has("left_leg")) {
                this.RM_LeftLegGeoBoneID = modelPartMap.get("left_leg").getAsString();
            }
            if (modelPartMap.has("right_leg")) {
                this.RM_RightLegGeoBoneID = modelPartMap.get("right_leg").getAsString();
            }
        }
        if (json.has("part_extra_pos")) {
            JsonObject partExtraPos = json.getAsJsonObject("part_extra_pos");
            if (partExtraPos.has("head")) {
                this.headTransform = partTransform.of(partExtraPos.get("head").getAsJsonObject());
            }
            if (partExtraPos.has("body")) {
                this.bodyTransform = partTransform.of(partExtraPos.get("body").getAsJsonObject());
            }
            if (partExtraPos.has("left_arm")) {
                this.leftArmTransform = partTransform.of(partExtraPos.get("left_arm").getAsJsonObject());
            }
            if (partExtraPos.has("right_arm")) {
                this.rightArmTransform = partTransform.of(partExtraPos.get("right_arm").getAsJsonObject());
            }
            if (partExtraPos.has("left_leg")) {
                this.leftLegTransform = partTransform.of(partExtraPos.get("left_leg").getAsJsonObject());
            }
            if (partExtraPos.has("right_leg")) {
                this.rightLegTransform = partTransform.of(partExtraPos.get("right_leg").getAsJsonObject());
            }
        }
        if (json.has("tail")) {
            this.tailChain = new NormalChainConfig(json.getAsJsonObject("tail"));
        }
        if (json.has("head_tail")) {
            this.headTailChain = new NormalChainConfig(json.getAsJsonObject("head_tail"));
        }
        if (json.has("wing_l")) {
            this.wingChainL = new NormalChainConfig(json.getAsJsonObject("wing_l"));
        }
        if (json.has("wing_r")) {
            this.wingChainR = new NormalChainConfig(json.getAsJsonObject("wing_r"));
        }
        if (json.has("neck_config")) {
            try {
                this.neckConfig = new NeckConfig(json.getAsJsonObject("neck_config"));
            } catch (Exception e) {
                this.neckConfig = null;
                ShapeShifterCurseFabric.LOGGER.error("Error parsing neck_config", e);
            }
        }
        if (json.has("eye_blink")) {
            try {
                this.eyeBlinkController = new FormEyeBlinkController(json.getAsJsonObject("eye_blink"));
            } catch (Exception e) {
                this.eyeBlinkController = null;
                ShapeShifterCurseFabric.LOGGER.error("Error parsing eye_blink", e);
            }
        }
    }

    public void ProcessExtraBone(FormModel m, Player player, String AnimBoneID, String OriginFursBoneID) {
        GeoBone bone =  m.resetBone(OriginFursBoneID);
        Vec3f AnimPosition = AnimSystem.getPlayerBone3DTransform(player, AnimBoneID, TransformType.POSITION, new Vec3f(0, 0, 0));
        m.setPositionForBone(OriginFursBoneID, new Vec3(AnimPosition.x(), -AnimPosition.y(), -AnimPosition.z()));
        m.setRotationForBone(OriginFursBoneID, AnimSystem.getPlayerBone3DTransform(player, AnimBoneID, TransformType.ROTATION, new Vec3f(0, 0, 0)));
        m.invertRotForPart(OriginFursBoneID, false, true, true);
    }

    public final void setRotationForTailBones(Player player, FormModel model, float limbAngle, float limbDistance, float age, float tailDragAmount, float tailDragAmountVertical) {
        IForm curForm = FormTextureUtils.getPlayerForm_Render(player);
        boolean isFeral = curForm.getBodyType() == PlayerFormBodyType.FERAL;
        float SWAY_RATE = 0.33333334F * 0.5F;
        float SWAY_SCALE = 0.05F;
        if(this.tailChain == null || !this.tailChain.canApply(player)) { return; }
        for (List<String> tailChain : this.tailChain.chain) {
            GeoBone firstTail = model.getCachedGeoBone(tailChain.get(0));
            if (firstTail == null) {
                continue;
            }
            float tailSway = SWAY_SCALE * Mth.cos(age * SWAY_RATE + (((float)Math.PI / 3.0F) * 0.75f));
            float tailBalance = Mth.cos(limbAngle * 0.6662F) * 0.325F * limbDistance;
            if(!isFeral){
                model.setRotationAxisForBone(firstTail, 2, -Mth.lerp(limbDistance, tailSway, tailBalance) - tailDragAmount * 0.75F);
            } else {
                model.setRotationAxisForBone(firstTail, 4, Mth.lerp(limbDistance, tailSway, tailBalance) + tailDragAmount * 0.75F);
            }
            model.setRotationAxisForBone(firstTail, 0, -tailDragAmountVertical * 0.75f);
            float offset = 0.0F;
            for(int i = 1; i < tailChain.size(); i++){
                GeoBone chainBone = model.getCachedGeoBone(tailChain.get(i));
                if (chainBone == null) {continue;}
                if(!isFeral){
                    model.setRotationAxisForBone(chainBone, 2, - Mth.lerp(limbDistance, SWAY_SCALE * Mth.cos(age * SWAY_RATE - (((float)Math.PI / 3.0F) * offset)), 0.0f) - tailDragAmount * 0.75F);
                } else{
                    model.setRotationAxisForBone(chainBone, 4, Mth.lerp(limbDistance, SWAY_SCALE * Mth.cos(age * SWAY_RATE - (((float)Math.PI / 3.0F) * offset)), 0.0f) + tailDragAmount * 0.75F);
                }
                model.setRotationAxisForBone(chainBone, 0, -tailDragAmountVertical * 0.75f * (offset + 0.75f));
                offset += 0.75F;
            }
        }
    }

    public final void setRotationForHeadTailBones(Player player, FormModel model, float headAngle, float age, float tailDragAmount, float tailDragAmountVertical){
        float SWAY_RATE = 0.33333334F * 0.5F;
        float SWAY_SCALE = 0.05F;
        if (this.headTailChain == null || !this.headTailChain.canApply(player)) { return; }
        for (List<String> tailChain : this.headTailChain.chain) {
            GeoBone firstHeadTail = model.getCachedGeoBone(tailChain.get(0));
            if (firstHeadTail == null) {
                continue;
            }
            float headTailSway = SWAY_SCALE * Mth.cos(age * SWAY_RATE + (((float)Math.PI / 3.0F) * 0.75f));
            float headTailBalance = Mth.cos(headAngle * 0.6662F) * 0.325F * 0.1f;
            model.setRotationAxisForBone(firstHeadTail, 2, -Mth.lerp(0.1f, headTailSway, headTailBalance) - tailDragAmount * 0.75F);
            model.setRotationAxisForBone(firstHeadTail, 0, -tailDragAmountVertical * 0.75f);
            float offset = 0.0F;
            for (int i = 1; i < tailChain.size(); i++){
                GeoBone chainBone = model.getCachedGeoBone(tailChain.get(i));
                if (chainBone == null) {continue;}
                model.setRotationAxisForBone(chainBone, 2, - Mth.lerp(0.1f, SWAY_SCALE * Mth.cos(age * SWAY_RATE - (((float)Math.PI / 3.0F) * offset)), 0.0f) - tailDragAmount * 0.75F);
                model.setRotationAxisForBone(chainBone, 0, -tailDragAmountVertical * 0.75f * (offset + 0.75f));
                offset += 0.75F;
            }
        }
    }

    public final void setRotationForWingBones(Player player, FormModel model, float limbAngle, float limbDistance, float age, float tailDragAmountVertical){
        float swayAngle = age * 20.0F * (float) (Math.PI / 180.0) + limbAngle;
        float sway_base = Mth.cos(swayAngle) * (float) Math.PI * 0.15F + limbDistance;
        float sway_l = (float) -(Math.PI / 4) + sway_base;
        float sway_r = (float) (Math.PI / 4) - sway_base;

        if (this.wingChainL != null && this.wingChainL.canApply(player)) {
            for (List<String> wingChain : this.wingChainL.chain) {
                GeoBone firstWing = model.getCachedGeoBone(wingChain.get(0));
                if (firstWing == null) { continue; }
                model.setRotationAxisForBone(firstWing, 2, sway_l);
                model.setRotationAxisForBone(firstWing, 0, -tailDragAmountVertical * 0.35f);
                float offset = 0.0F;
                for (int i = 1; i < wingChain.size(); i++) {
                    GeoBone chainBone = model.getCachedGeoBone(wingChain.get(i));
                    if (chainBone == null) { continue; }
                    model.setRotationAxisForBone(chainBone, 0, -tailDragAmountVertical * 0.75f * offset);
                    offset += 0.75F;
                }
            }
        }
        if (this.wingChainR != null && this.wingChainR.canApply(player)) {
            for (List<String> wingChain : this.wingChainR.chain) {
                GeoBone firstWing = model.getCachedGeoBone(wingChain.get(0));
                if (firstWing == null)  continue;
                model.setRotationAxisForBone(firstWing, 2, sway_r);
                model.setRotationAxisForBone(firstWing, 0, -tailDragAmountVertical * 0.35f);
                float offset = 0.0F;
                for (int i = 1; i < wingChain.size(); i++) {
                    GeoBone chainBone = model.getCachedGeoBone(wingChain.get(i));
                    if (chainBone == null) { continue; }
                    model.setRotationAxisForBone(chainBone, 0, -tailDragAmountVertical * 0.75f * offset);
                    offset += 0.75F;
                }
            }
        }
    }

    // 如果拓展需要修改这个逻辑 可以使用Mixin 我认为挂一个事件有点臃肿 推荐使用Inject Return False
    // 当然也有其他方法 比如写一个判断函数变量 不过我认为修改应该没这么勤
    private boolean shouldUseVirtualData(Player player) {
        return ClientUtils.isOpenInventoryScreen;
    }

    // Yaw, Pitch
    private Vec2 getLongNeckAngles(Player player, float tickDelta, float fallbackHeadYaw, float fallbackHeadPitch) {
        if (neckConfig == null) {
            return new Vec2(fallbackHeadYaw, fallbackHeadPitch);
        }
	    neckData data = (shouldUseVirtualData(player) ? neckDataMapV : neckDataMap).get(player);
        float viewYaw = player.getViewYRot(tickDelta);
        float targetHeadPitch = player.getViewXRot(tickDelta);
        float bodyYaw = LongNeckRenderUtils.lerpAngle(tickDelta, player.yBodyRotO, player.yBodyRot);
        float targetHeadYaw = Mth.wrapDegrees(viewYaw - bodyYaw);
        double renderTick = player.tickCount + tickDelta;
        float deltaTicks = (float) Mth.clamp(renderTick - data.lastRenderTick, 0.0D, 1.0D);
        data.lastRenderTick = renderTick;

        if (deltaTicks <= 0.0F) {
            return new Vec2(data.headYaw, data.headPitch);
        }
        float yawLerp = Mth.clamp(deltaTicks * 0.45F, 0.0F, 1.0F);
        float pitchLerp = Mth.clamp(deltaTicks * 0.35F, 0.0F, 1.0F);
        data.headYaw = LongNeckRenderUtils.lerpAngleAwayFrom(yawLerp, data.headYaw, targetHeadYaw, 180.0F);
        data.headPitch = Mth.lerp(pitchLerp, data.headPitch, targetHeadPitch);
        if (!Float.isFinite(data.headYaw)) {
            data.headYaw = fallbackHeadYaw;
        }
        if (!Float.isFinite(data.headPitch)) {
            data.headPitch = fallbackHeadPitch;
        }
        return new Vec2(data.headYaw, data.headPitch);
    }

    public void setRotationForNeckBones(Player player, FormModel model, float headYaw, float headPitch) {
        if (neckConfig == null) {
            return;
        }
        float yawDeg = Mth.clamp(headYaw, -neckConfig.maxYawDegree, neckConfig.maxYawDegree);
        float pitchDeg = Mth.clamp(headPitch, -neckConfig.maxPitchDegreeU, neckConfig.maxPitchDegreeD);
        float yawRad = yawDeg * Mth.DEG_TO_RAD;
        float pitchRad = pitchDeg * Mth.DEG_TO_RAD;
        for (int i = 0; i <= neckConfig.chain.size(); i++) {
            GeoBone bone = neckConfig.getBoneByChain(model, i);
            if (bone == null) {
                continue;
            }
            model.resetRotationForGeoBone(bone);
            model.setRotationAxisForBone(bone, neckConfig.yawAxis, yawRad * neckConfig.yawWeights[i]);
            model.setRotationAxisForBone(bone, neckConfig.pitchAxis, pitchRad * neckConfig.pitchWeights[i]);
        }
    }

    @Override
    public void beforeRender(FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        tailData td = tailDataMap.get(player);
        float targetDrag = Mth.lerp(tickDelta, td.tailDragAmountO, td.tailDragAmount);
        td.currentTailDragAmount = Mth.lerp(0.04f, td.currentTailDragAmount, targetDrag);
        float targetDragVertical = Mth.lerp(tickDelta, td.tailDragAmountVerticalO, td.tailDragAmountVertical);
        td.currentTailDragAmountVertical = Mth.lerp(0.04f, td.currentTailDragAmountVertical, targetDragVertical);
    }


    @Override
    public void processAnimation(FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        tailData td = tailDataMap.get(player);
        model.resetBone(RM_HeadGeoBoneID);
        model.resetBone(RM_BodyGeoBoneID);
        model.resetBone(RM_LeftArmGeoBoneID);
        model.resetBone(RM_RightArmGeoBoneID);
        model.resetBone(RM_LeftLegGeoBoneID);
        model.resetBone(RM_RightLegGeoBoneID);
        for (Tuple<String, String> pair : extraPartsMap) {
            ProcessExtraBone(model, player, pair.getA(), pair.getB());
        }
        PlayerModel playerModel = (PlayerModel) renderer.getModel();
        model.setRotationForBone(RM_HeadGeoBoneID, FormRenderUtils.getPartRotation(playerModel.head));
        model.translatePositionForBone(RM_HeadGeoBoneID, FormRenderUtils.getPartPosition(playerModel.head));
        model.translatePositionForBone(RM_BodyGeoBoneID, FormRenderUtils.getPartPosition(playerModel.body));
        model.translatePositionForBone(RM_LeftArmGeoBoneID, FormRenderUtils.getPartPosition(playerModel.leftArm));
        model.translatePositionForBone(RM_RightArmGeoBoneID, FormRenderUtils.getPartPosition(playerModel.rightArm));
        model.translatePositionForBone(RM_LeftLegGeoBoneID, FormRenderUtils.getPartPosition(playerModel.leftLeg));
        model.translatePositionForBone(RM_RightLegGeoBoneID, FormRenderUtils.getPartPosition(playerModel.rightLeg));
        model.translatePositionForBone(RM_LeftArmGeoBoneID, new Vec3(5, 2, 0));
        model.translatePositionForBone(RM_RightArmGeoBoneID, new Vec3(-5, 2, 0));
        model.translatePositionForBone(RM_LeftLegGeoBoneID, new Vec3(2, 12, 0));
        model.translatePositionForBone(RM_RightLegGeoBoneID, new Vec3(-2, 12, 0));
        model.setRotationForBone(RM_BodyGeoBoneID, FormRenderUtils.getPartRotation(playerModel.body));
        Vec2 neckAngles = getLongNeckAngles(player, tickDelta, headYaw, headPitch);
        this.setRotationForNeckBones(player, model, neckAngles.x, neckAngles.y);
        this.setRotationForTailBones(player, model, limbAngle, limbDistance, player.tickCount, td.currentTailDragAmount, td.currentTailDragAmountVertical);
        this.setRotationForHeadTailBones(player, model, neckAngles.x, player.tickCount, td.currentTailDragAmount, td.currentTailDragAmountVertical);
        this.setRotationForWingBones(player, model, limbAngle, limbDistance, player.tickCount, td.currentTailDragAmountVertical);
        if (this.bodyTransform != null) this.bodyTransform.apply(model.getCachedGeoBone(RM_BodyGeoBoneID));
        model.invertRotForPart(RM_BodyGeoBoneID, false, true, false);
        model.setRotationForBone(RM_LeftArmGeoBoneID, FormRenderUtils.getPartRotation(playerModel.leftArm));
        model.setRotationForBone(RM_RightArmGeoBoneID, FormRenderUtils.getPartRotation(playerModel.rightArm));
        model.setRotationForBone(RM_LeftLegGeoBoneID, FormRenderUtils.getPartRotation(playerModel.leftLeg));
        model.setRotationForBone(RM_RightLegGeoBoneID, FormRenderUtils.getPartRotation(playerModel.rightLeg));
        if (this.headTransform != null) this.headTransform.apply(model.getCachedGeoBone(RM_HeadGeoBoneID));
        if (this.leftArmTransform != null) this.leftArmTransform.apply(model.getCachedGeoBone(RM_LeftArmGeoBoneID));
        if (this.rightArmTransform != null) this.rightArmTransform.apply(model.getCachedGeoBone(RM_RightArmGeoBoneID));
        if (this.leftLegTransform != null) this.leftLegTransform.apply(model.getCachedGeoBone(RM_LeftLegGeoBoneID));
        if (this.rightLegTransform != null) this.rightLegTransform.apply(model.getCachedGeoBone(RM_RightLegGeoBoneID));
        model.invertRotForPart(RM_HeadGeoBoneID, false, true, true);
        model.invertRotForPart(RM_RightArmGeoBoneID, false, true, true);
        model.invertRotForPart(RM_LeftArmGeoBoneID, false, true, true);
        model.invertRotForPart(RM_LeftLegGeoBoneID, false, true, true);
        model.invertRotForPart(RM_RightLegGeoBoneID, false, true, true);
        if (eyeBlinkController != null) {
            eyeBlinkController.update(model, player, tickDelta);
        }
        if (neckConfig != null) {
            GeoBone neckHead = neckConfig.getHead(model);
            GeoBone neckRoot = neckConfig.getRoot(model);
            if (neckHead != null && neckRoot != null) {
                // GL5: setTrackingMatrices 无对应物（骨骼位置由渲染管线自动追踪），忽略
                model.setBoneHidden(neckRoot, LongNeckRenderUtils.isFirstPersonModelActiveForSelf(player));
            }
        }
    }

    @Override
    public void afterRender(FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // GL5: 骨骼隐藏由 render pass 内的 snapshot 控制，renderPosed 后自动清理，无需在此恢复
    }

    @Override
    public void finishRender(FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, PoseStack matrices, MultiBufferSource vertexConsumers, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        tailData td = tailDataMap.get(player);
        td.tailDragAmountO = td.tailDragAmount;
        td.tailDragAmount *= 0.75F;
        td.tailDragAmount -= (float) (Math.toRadians((player.yBodyRot - player.yBodyRotO)) * 0.55F);
        td.tailDragAmount = Mth.clamp(td.tailDragAmount, -1.6F, 1.6F);
        float verticalSpeed = (float) player.getDeltaMovement().y;
        float targetVerticalDrag = Mth.clamp(verticalSpeed * 1.5f, -1.6f, 1.6f);
        td.tailDragAmountVertical *= 0.8F;
        td.tailDragAmountVertical += targetVerticalDrag * 0.15F;
        td.tailDragAmountVertical = Mth.clamp(td.tailDragAmountVertical, -1.6f, 1.6f);
        td.tailDragAmountVerticalO = td.tailDragAmountVertical;
    }

    @Override
    public @Nullable GeoBone beforeRenderFirstPerson(@Nullable GeoBone geoBone, FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, ModelPart arm, ModelPart sleeve) {
        PlayerModel playerModel = (PlayerModel) renderer.getModel();
        boolean IsRenderRight = arm.equals(playerModel.rightArm);
        String GeoBoneName = IsRenderRight ? this.rightArmGeoBoneID : this.leftArmGeoBoneID;
        // GL5: GeoBone 从 BakedGeoModel 直接按名查找并缓存，无 AnimationProcessor 注册流程
        geoBone = model.getCachedGeoBone(GeoBoneName);
        return geoBone;
    }

    @Override
    public @Nullable GeoBone processAnimationFirstPerson(@Nullable GeoBone geoBone, FormRenderer formRenderer, FormModel model, AvatarRenderer renderer, Player player, ModelPart arm, ModelPart sleeve) {
        PlayerModel playerModel = (PlayerModel) renderer.getModel();
        boolean IsRenderRight = arm.equals(playerModel.rightArm);
        String GeoBoneName = IsRenderRight ? this.rightArmGeoBoneID : this.leftArmGeoBoneID;
        model.resetBone(GeoBoneName);
        model.translatePositionForBone(GeoBoneName, FormRenderUtils.getPartPosition(arm));
        model.translatePositionForBone(GeoBoneName, new Vec3(5 * (IsRenderRight ? -1.0 : 1.0), 2, 0));
        model.setRotationForBone(GeoBoneName, FormRenderUtils.getPartRotation(arm));
        model.invertRotForPart(GeoBoneName, false, true, true);
        return geoBone;
    }

    @Override
    public void modifyHeadPart(Player player, HumanoidModel<?> model, FormModel formModel) {
        if (this.neckConfig == null) {
            return;
        }
        GeoBone neckHead = this.neckConfig.getHead(formModel);
        if (neckHead == null) {
            return;
        }
        ModelPart head = model.getHead();
        if (head == null) {
            return;
        }
        PoseStack headMatrix = FormRenderUtils.computeModelMatrixStack(neckHead);
        Matrix4f matrix4f = headMatrix.last().pose();
        Vector4f headPos = headMatrix.last().pose().transform(new Vector4f(0, 0, 0, 1));
        head.x = (float) (headPos.x);
        head.y = (float) (-headPos.y + 24);
        head.z = (float) (-headPos.z);
        Matrix3f rotationMatrix = matrix4f.get3x3(new Matrix3f());
        Vector3f euler = new Vector3f();
        rotationMatrix.getEulerAnglesZYX(euler);
        head.xRot = euler.x();
        head.yRot = -euler.y();
        head.zRot = -euler.z();
    }
}