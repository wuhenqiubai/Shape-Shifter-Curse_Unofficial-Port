package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.util.FormSkinSystem;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.*;

@SuppressWarnings("removal")
public class FormModel extends GeoModel<FormAnimatable> {
    public static List<FormModel> loadedModel = new ArrayList<>();
    public static HashMap<Player, Boolean> SlimMap = new HashMap<>();

    public static final String MissingGeoString = ShapeShifterCurseFabric.MOD_ID + ":geo/missing.geo.json";
    public static final String MissingTextureString = ShapeShifterCurseFabric.MOD_ID + ":textures/missing.png";
    public static final String MissingAnimationString = ShapeShifterCurseFabric.MOD_ID + ":animations/missing.animation.json";

    public Player entity;

    public JsonObject modelJson;

    public String Name = "";  // 用于皮肤系统 先留一下API
    public Identifier Layer = null;  // 用于皮肤系统 先留一下API
    public Identifier Form = null;  // 用于皮肤系统 先留一下API

    public static int modelIDIter = 0;
    public int modelID = -1;

    public boolean SlimOnly = false;

    private final HashMap<String, Vec3> prevBoneRotation = new HashMap<>();
    public boolean WideOnly = false;
    public boolean UseMultiplyMask = false;
    public boolean UseAzureAnim = false;
    public Identifier ModelResource = ShapeShifterCurseFabric.identifier("geo/missing.geo.json");
    public Identifier ModelResource_Slim = ShapeShifterCurseFabric.identifier("geo/missing.geo.json");

    public Identifier TextureResource = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier TextureMaskResource = null;
    public Identifier TextureResource_Slim = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier TextureMaskResource_Slim = null;

    public Identifier OverlayTextureResource = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier OverlayTextureMaskResource = null;
    public Identifier OverlayTextureResource_Slim = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier OverlayTextureMaskResource_Slim = null;

    public Identifier EmissiveTextureResource = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier EmissiveTextureMaskResource = null;
    public Identifier EmissiveTextureResource_Slim = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier EmissiveTextureMaskResource_Slim = null;

    public Identifier FullBrightTextureResource = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier FullBrightTextureMaskResource = null;
    public Identifier FullBrightTextureResource_Slim = ShapeShifterCurseFabric.identifier("textures/missing.png");
    public Identifier FullBrightTextureMaskResource_Slim = null;

    public Identifier Animation = ShapeShifterCurseFabric.identifier("animations/missing.animation.json");

    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_Textures = new HashMap<>();
    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_Textures_Slim = new HashMap<>();

    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_OverlayTexture = new HashMap<>();
    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_OverlayTexture_Slim = new HashMap<>();

    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_EmissiveTexture = new HashMap<>();
    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_EmissiveTexture_Slim = new HashMap<>();

    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_FullBrightTexture = new HashMap<>();
    public HashMap<FormTextureUtils.ColorSetting, Identifier> ColorMask_Baked_FullBrightTexture_Slim = new HashMap<>();

    // Hidden Parts
    public boolean Hidden_Hat = false;
    public boolean Hidden_Head = false;
    public boolean Hidden_Body = false;
    public boolean Hidden_Jacket = false;
    public boolean Hidden_LeftArm = false;
    public boolean Hidden_RightArm = false;
    public boolean Hidden_LeftSleeve = false;
    public boolean Hidden_RightSleeve = false;
    public boolean Hidden_LeftLeg = false;
    public boolean Hidden_RightLeg = false;
    public boolean Hidden_LeftPants = false;
    public boolean Hidden_RightPants = false;

    public IModelAnimationSystem AnimationSystem = null;

    // builtin_controller_data
    // chain -> [["tail0_0", "tail0_1"], [tail1_0", "tail1_1"]]
    public List<List<String>> BCD_TailChain = new ArrayList<>();
    public List<List<String>> BCD_TailChainHead = new ArrayList<>();
    public List<List<String>> BCD_WingChainL = new ArrayList<>();
    public List<List<String>> BCD_WingChainR = new ArrayList<>();

    public static class NeckIkConfig {
        public String mount = "neck_mount";
        public String head = "ik_head";
        public List<String> chain = new ArrayList<>();
        public char yawAxis = 'y';
        public char pitchAxis = 'x';
        public float yawSign = -1.0f;
        public float pitchSign = 1.0f;
        public float[] yawWeights = new float[0];
        public float[] pitchWeights = new float[0];
        public float maxYawDeg = 85.0f;
        public float maxPitchUpDeg = 55.0f;
        public float maxPitchDownDeg = 45.0f;
    }
    // NECK FEATURES END

    public FormModel(JsonObject json) {
        this.modelJson = json;
        this.CompileModel();
        this.modelID = modelIDIter++;
    }

    public void CompileModel() {
        this.ColorMask_Baked_Textures.clear();
        this.ColorMask_Baked_Textures_Slim.clear();
        this.ColorMask_Baked_OverlayTexture.clear();
        this.ColorMask_Baked_OverlayTexture_Slim.clear();
        this.ColorMask_Baked_EmissiveTexture.clear();
        this.ColorMask_Baked_EmissiveTexture_Slim.clear();
        this.ColorMask_Baked_FullBrightTexture.clear();
        this.ColorMask_Baked_FullBrightTexture_Slim.clear();

        this.Name = GsonHelper.getAsString(this.modelJson, "name", "");
        if (this.modelJson.has("layer")) {
            this.Layer = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "layer", ""));
        } else {
            this.Layer = null;
        }
        if (this.modelJson.has("form")) {
            this.Form = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "form", ""));
        } else {
            this.Form = null;
        }
        this.SlimOnly = GsonHelper.getAsBoolean(this.modelJson, "slim_only", false);
        this.WideOnly = GsonHelper.getAsBoolean(this.modelJson, "wide_only", false);
        this.UseMultiplyMask = GsonHelper.getAsBoolean(this.modelJson, "use_multiply_mask", false);

        this.ModelResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "model", MissingGeoString));
        this.ModelResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "model_slim", MissingGeoString));

        this.TextureResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "texture", MissingTextureString));
        this.TextureResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "texture_slim", MissingTextureString));
        if (this.modelJson.has("texture_mask")) {
            this.TextureMaskResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "texture_mask", MissingTextureString));
        } else {
            this.TextureMaskResource = null;
        }
        if (this.modelJson.has("texture_mask_slim")) {
            this.TextureMaskResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "texture_mask_slim", MissingTextureString));
        } else {
            this.TextureMaskResource_Slim = null;
        }

        this.OverlayTextureResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "overlay", MissingTextureString));
        this.OverlayTextureResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "overlay_slim", MissingTextureString));
        if (this.modelJson.has("overlay_mask")) {
            this.OverlayTextureMaskResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "overlay_mask", MissingTextureString));
        } else {
            this.OverlayTextureMaskResource = null;
        }
        if (this.modelJson.has("overlay_mask_slim")) {
            this.OverlayTextureMaskResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "overlay_mask_slim", MissingTextureString));
        } else {
            this.OverlayTextureMaskResource_Slim = null;
        }

        this.EmissiveTextureResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "emissive_overlay", MissingTextureString));
        this.EmissiveTextureResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "emissive_overlay_slim", MissingTextureString));
        if (this.modelJson.has("emissive_overlay_mask")) {
            this.EmissiveTextureMaskResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "emissive_overlay_mask", MissingTextureString));
        } else {
            this.EmissiveTextureMaskResource = null;
        }
        if (this.modelJson.has("emissive_overlay_mask_slim")) {
            this.EmissiveTextureMaskResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "emissive_overlay_mask_slim", MissingTextureString));
        } else {
            this.EmissiveTextureMaskResource_Slim = null;
        }

        this.FullBrightTextureResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "fullbright_texture", MissingTextureString));
        if (this.modelJson.has("fullbright_texture_mask")) {
            this.FullBrightTextureMaskResource = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "fullbright_texture_mask", MissingTextureString));
        } else {
            this.FullBrightTextureMaskResource = null;
        }
        this.FullBrightTextureResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "fullbright_texture_slim", MissingTextureString));
        if (this.modelJson.has("fullbright_texture_mask_slim")) {
            this.FullBrightTextureMaskResource_Slim = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "fullbright_texture_mask_slim", MissingTextureString));
        } else {
            this.FullBrightTextureMaskResource_Slim = null;
        }

        this.UseAzureAnim = GsonHelper.getAsBoolean(this.modelJson, "use_azurelib_anim", false);
        this.Animation = Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "animations", MissingAnimationString));

        this.Hidden_Hat = false;
        this.Hidden_Head = false;
        this.Hidden_Body = false;
        this.Hidden_Jacket = false;
        this.Hidden_LeftArm = false;
        this.Hidden_RightArm = false;
        this.Hidden_LeftSleeve = false;
        this.Hidden_RightSleeve = false;
        this.Hidden_LeftLeg = false;
        this.Hidden_RightLeg = false;
        this.Hidden_LeftPants = false;
        this.Hidden_RightPants = false;
        JsonArray hiddenArray = GsonHelper.getAsJsonArray(this.modelJson, "hidden", null);
        if (hiddenArray != null) {
            for (int i = 0; i < hiddenArray.size(); i++) {
                String hidden = hiddenArray.get(i).getAsString();
                switch (hidden) {
                    case "hat" -> { this.Hidden_Hat = true; }
                    case "head" -> { this.Hidden_Head = true; }
                    case "body" -> { this.Hidden_Body = true; }
                    case "jacket" -> { this.Hidden_Jacket = true; }
                    case "leftArm" -> { this.Hidden_LeftArm = true; }
                    case "rightArm" -> { this.Hidden_RightArm = true; }
                    case "leftSleeve" -> { this.Hidden_LeftSleeve = true; }
                    case "rightSleeve" -> { this.Hidden_RightSleeve = true; }
                    case "leftLeg" -> { this.Hidden_LeftLeg = true; }
                    case "rightLeg" -> { this.Hidden_RightLeg = true; }
                    case "leftPants" -> { this.Hidden_LeftPants = true; }
                    case "rightPants" -> { this.Hidden_RightPants = true; }
                }
            }
        }
        this.AnimationSystem = null;
        if (this.modelJson.has("animation_system")) {
            this.AnimationSystem = FormRenderUtils.get_MAS(Identifier.tryParse(GsonHelper.getAsString(this.modelJson, "animation_system", null)), this.modelJson.getAsJsonObject("animation_system_config"));
        }
        if (AnimationSystem == null) {
            this.AnimationSystem = FormRenderUtils.get_MAS(FormRenderUtils.DEFAULT_MAS, null);
        }
        this.loadBCD();
    }

    public List<List<String>> loadChainData(JsonObject json) {
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

    public void loadBCD() {
        // BCD 目前没参数了 之前的迁移至DefaultModelAnimationSystem里了 不过这套系统还留着 后续想加新参数可以在这里写
    }

    public void setPlayer(Player player, boolean slim) {
        this.entity = player;
        SlimMap.put(player, slim);
    }

    public boolean useSlim(boolean slim) {
        if (SlimOnly) {
            return true;
        }
        if (WideOnly) {
            return false;
        }
        return slim;
    }

    public Identifier getModelResource(boolean slim) {
        return useSlim(slim) ? ModelResource_Slim : ModelResource;
    }

    private Identifier readCacheOrBake(HashMap<FormTextureUtils.ColorSetting, Identifier> Cache, Identifier Resource, Identifier ResourceMask, FormTextureUtils.ColorSetting colorSetting) {
        Identifier CachedTexture = Cache.get(colorSetting);
        if (CachedTexture != null) {
            return CachedTexture;
        }
        CachedTexture = FormTextureUtils.BakeTexture(Resource, ResourceMask, colorSetting, UseMultiplyMask);
        if (CachedTexture == null) {
            CachedTexture = Resource;
        }
        Cache.put(colorSetting, CachedTexture);
        return CachedTexture;
    }

    public Identifier getTextureResource(boolean slim) {
        boolean uslim = useSlim(slim);
        Identifier Resource = uslim ? this.TextureResource_Slim : this.TextureResource;
        Identifier ResourceMask = uslim ? this.TextureMaskResource_Slim : this.TextureMaskResource;
        if (this.entity != null) {
            FormSkinSystem.FormSkin formSkin = FormSkinSystem.getFormSkin(this.entity.getUUID(), this.Form);
            if (formSkin != null) {
                Identifier SkinResource = formSkin.getSkinTexture(uslim);
                if (SkinResource != null) {
                    return SkinResource;
                }
            }
        }
        if (ResourceMask != null) {
            if (FormTextureUtils.useTempFormTexture && Objects.equals(this.entity, Minecraft.getInstance().player)) {
                return FormTextureUtils.tempFormTextureProcessor.getTexture(this.modelID, uslim ? "texture_slim" : "texture", Resource, ResourceMask, UseMultiplyMask);
            }
            FormTextureUtils.ColorSetting colorSetting = FormTextureUtils.getPlayerColorSetting(this.entity);
            if (colorSetting != null) {
                HashMap<FormTextureUtils.ColorSetting, Identifier> Cache = uslim ? ColorMask_Baked_Textures_Slim : ColorMask_Baked_Textures;
                return readCacheOrBake(Cache, Resource, ResourceMask, colorSetting);
            }
        }
        return Resource;
    }

    public Identifier getOverlayTextureResource(boolean slim) {
        boolean uslim = useSlim(slim);
        Identifier Resource = uslim ? this.OverlayTextureResource_Slim : this.OverlayTextureResource;
        Identifier ResourceMask = uslim ? this.OverlayTextureMaskResource_Slim : this.OverlayTextureMaskResource;
        if (this.entity != null) {
            FormSkinSystem.FormSkin formSkin = FormSkinSystem.getFormSkin(this.entity.getUUID(), this.Form);
            if (formSkin != null) {
                Identifier SkinResource = formSkin.getSkinOverlayTexture(uslim);
                if (SkinResource != null) {
                    return SkinResource;
                }
            }
        }
        if (ResourceMask != null) {
            if (FormTextureUtils.useTempFormTexture && Objects.equals(this.entity, Minecraft.getInstance().player)) {
                return FormTextureUtils.tempFormTextureProcessor.getTexture(this.modelID, uslim ? "overlay_texture_slim" : "overlay_texture", Resource, ResourceMask, UseMultiplyMask);
            }
            FormTextureUtils.ColorSetting colorSetting = FormTextureUtils.getPlayerColorSetting(this.entity);
            if (colorSetting != null) {
                HashMap<FormTextureUtils.ColorSetting, Identifier> Cache = uslim ? ColorMask_Baked_OverlayTexture_Slim : ColorMask_Baked_OverlayTexture;
                return readCacheOrBake(Cache, Resource, ResourceMask, colorSetting);
            }
        }
        return Resource;
    }

    public Identifier getEmissiveTextureResource(boolean slim) {
        boolean uslim = useSlim(slim);
        Identifier Resource = uslim ? this.EmissiveTextureResource_Slim : this.EmissiveTextureResource;
        Identifier ResourceMask = uslim ? this.EmissiveTextureMaskResource_Slim : this.EmissiveTextureMaskResource;
        if (this.entity != null) {
            FormSkinSystem.FormSkin formSkin = FormSkinSystem.getFormSkin(this.entity.getUUID(), this.Form);
            if (formSkin != null) {
                Identifier SkinResource = formSkin.getSkinEmissiveTexture(uslim);
                if (SkinResource != null) {
                    return SkinResource;
                }
            }
        }
        if (ResourceMask != null) {
            if (FormTextureUtils.useTempFormTexture && Objects.equals(this.entity, Minecraft.getInstance().player)) {
                return FormTextureUtils.tempFormTextureProcessor.getTexture(this.modelID, uslim ? "emissive_texture_slim" : "emissive_texture", Resource, ResourceMask, UseMultiplyMask);
            }
            FormTextureUtils.ColorSetting colorSetting = FormTextureUtils.getPlayerColorSetting(this.entity);
            if (colorSetting != null && ResourceMask != null) {
                HashMap<FormTextureUtils.ColorSetting, Identifier> Cache = uslim ? ColorMask_Baked_EmissiveTexture_Slim : ColorMask_Baked_EmissiveTexture;
                return readCacheOrBake(Cache, Resource, ResourceMask, colorSetting);
            }
        }
        return Resource;
    }

    public Identifier getFullBrightTextureResource(boolean slim) {
        boolean uslim = useSlim(slim);
        Identifier Resource = uslim ? this.FullBrightTextureResource_Slim : this.FullBrightTextureResource;
        Identifier ResourceMask = uslim ? this.FullBrightTextureMaskResource_Slim : this.FullBrightTextureMaskResource;
        if (this.entity != null) {
            FormSkinSystem.FormSkin formSkin = FormSkinSystem.getFormSkin(this.entity.getUUID(), this.Form);
            if (formSkin != null) {
                Identifier SkinResource = formSkin.getSkinFullBrightTexture(uslim);
                if (SkinResource != null) {
                    return SkinResource;
                }
            }
        }
        if (ResourceMask != null) {
            if (FormTextureUtils.useTempFormTexture && Objects.equals(this.entity, Minecraft.getInstance().player)) {
                return FormTextureUtils.tempFormTextureProcessor.getTexture(this.modelID, uslim ? "fullbright_texture_slim" : "fullbright_texture", Resource, ResourceMask, UseMultiplyMask);
            }
            FormTextureUtils.ColorSetting colorSetting = FormTextureUtils.getPlayerColorSetting(this.entity);
            if (colorSetting != null) {
                HashMap<FormTextureUtils.ColorSetting, Identifier> Cache = uslim ? ColorMask_Baked_FullBrightTexture_Slim : ColorMask_Baked_FullBrightTexture;
                return readCacheOrBake(Cache, Resource, ResourceMask, colorSetting);
            }
        }
        return Resource;
    }

    public final HashMap<String, GeoBone> geoBoneCache = new HashMap<>();

    // GL5 render pass 会话：BoneSnapshots 在 adjustModelBonesForRender 时注入，render 结束后清理
    public BoneSnapshots currentSnapshots = null;

    public void beginRenderPass(BoneSnapshots snapshots) {
        this.currentSnapshots = snapshots;
    }

    public void endRenderPass() {
        this.currentSnapshots = null;
    }

    private @Nullable BoneSnapshot getBoneSnapshot(String bone_name) {
        if (currentSnapshots == null) return null;
        return currentSnapshots.get(bone_name).orElse(null);
    }

    public final @Nullable GeoBone getCachedGeoBone(String name) {
        GeoBone bone = geoBoneCache.get(name);
        if (bone == null) {
            Identifier modelRes = getModelResource(SlimMap.getOrDefault(this.entity, false));
            bone = getBakedModel(modelRes).getBone(name).orElse(null);
            if (bone != null) {
                geoBoneCache.put(name, bone);
            }
        }
        return bone;
    }

    public final @Nullable GeoBone translatePositionForBone(String bone_name, Vec3 pos) {
        BoneSnapshot s = getBoneSnapshot(bone_name);
        if (s == null) return null;
        s.setTranslation((float)pos.x + s.getTranslateX(), (float)pos.y + s.getTranslateY(), (float)pos.z + s.getTranslateZ());
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone setPositionForBone(String bone_name, Vec3 pos) {
        BoneSnapshot s = getBoneSnapshot(bone_name);
        if (s == null) return null;
        s.setTranslation((float)pos.x, (float)pos.y, (float)pos.z);
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone setRotationForBone(String bone_name, Vec3 rot) {
        BoneSnapshot s = getBoneSnapshot(bone_name);
        if (s == null) return null;
        s.setRotation((float)rot.x, (float)rot.y, (float)rot.z);
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone setRotationForBone(String bone_name, Vec3f rot) {
        return setRotationForBone(bone_name, new Vec3(rot.x(), rot.y(), rot.z()));
    }

    public final @Nullable GeoBone setModelPositionForBone(String bone_name, Vec3 pos) {
        // GL5 中 GeoBone 无 modelPosition 概念，骨骼绑定位置由 pivot 决定，此方法仅保留签名以兼容调用方
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone setModelPositionForBone(String bone_name, Vec3f pos) {
        return setModelPositionForBone(bone_name, new Vec3(pos.x(), pos.y(), pos.z()));
    }

    public final @Nullable GeoBone setScaleForBone(String bone_name, Vec3 scale) {
        BoneSnapshot s = getBoneSnapshot(bone_name);
        if (s == null) return null;
        s.setScale((float)scale.x, (float)scale.y, (float)scale.z);
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone setScaleForBone(String bone_name, Vec3f scale) {
        return setScaleForBone(bone_name, new Vec3(scale.x(), scale.y(), scale.z()));
    }

    public final @Nullable GeoBone invertRotForPart(String bone_name, boolean x, boolean y, boolean z) {
        BoneSnapshot s = getBoneSnapshot(bone_name);
        if (s == null) return null;
        s.setRotation(s.getRotX() * (x ? -1 : 1), s.getRotY() * (y ? -1 : 1), s.getRotZ() * (z ? -1 : 1));
        return getCachedGeoBone(bone_name);
    }

    public final @Nullable GeoBone resetBone(String bone_name) {
        setPositionForBone(bone_name, new Vec3(0, 0, 0));
        setRotationForBone(bone_name, new Vec3(0, 0, 0));
        setScaleForBone(bone_name, new Vec3(1, 1, 1));
        return getCachedGeoBone(bone_name);
    }

    /**
     * GL5：对单根 GeoBone 按轴设置旋转（当前 render pass 会话内生效）。
     * axisId 映射：0=x, 1=-x, 2=y, 3=-y, 4=z, 5=-z
     */
    public final void setRotationAxisForBone(@Nullable GeoBone bone, int axisId, float value) {
        if (currentSnapshots == null || bone == null) return;
        currentSnapshots.get(bone.name()).ifPresent(s -> {
            switch (axisId) {
                case 0 -> s.setRotX(value);
                case 1 -> s.setRotX(-value);
                case 2 -> s.setRotY(value);
                case 3 -> s.setRotY(-value);
                case 4 -> s.setRotZ(value);
                case 5 -> s.setRotZ(-value);
            }
        });
    }

    /** GL5：清零单根 GeoBone 的三轴旋转（当前 render pass 会话内生效） */
    public final void resetRotationForGeoBone(@Nullable GeoBone bone) {
        if (currentSnapshots == null || bone == null) return;
        currentSnapshots.get(bone.name()).ifPresent(s -> s.setRotation(0, 0, 0));
    }

    /** GL5：隐藏/显示单根 GeoBone（当前 render pass 会话内生效，对应 GL4 GeoBone.setHidden） */
    public final void setBoneHidden(@Nullable GeoBone bone, boolean hidden) {
        if (currentSnapshots == null || bone == null) return;
        currentSnapshots.get(bone.name()).ifPresent(s -> s.skipRender(hidden));
    }

    @Override
    public @NonNull Identifier getModelResource(GeoRenderState animatable) {
        Player player = this.entity;
        // Skin Model System Not Implemented
        // if (player != null) {
        //     FormSkinSystem.FormSkin formSkin = FormSkinSystem.getFormSkin(player.getUuid(), this.Form);
        //     if (formSkin != null) {
        //         Identifier formModel = formSkin.getSkinModel(useSlim(SlimMap.getOrDefault(player, false)));
        //         if (formModel != null) {
        //             return formModel;
        //         }
        //     }
        // }
        return getModelResource(SlimMap.getOrDefault(player, false));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState animatable) {
        Player player = this.entity;
        return getTextureResource(SlimMap.getOrDefault(player, false));
    }

    public Identifier getFullbrightTextureResource(FormAnimatable animatable) {
        Player player = animatable.e;
        return getFullBrightTextureResource(SlimMap.getOrDefault(player, false));

    }

    @Override
    public Identifier getAnimationResource(FormAnimatable animatable) {
        return this.Animation;
    }

//    @Override
//    public void handleAnimations(FormAnimatable animatable, long instanceId, AnimationState<FormAnimatable> animationState, float partialTick) {
//        if (this.UseAzureAnim) {
//            super.handleAnimations(animatable, instanceId, animationState);
//        }
//    }

}