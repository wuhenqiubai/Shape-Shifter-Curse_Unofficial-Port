package net.onixary.shapeShifterCurseFabric.render.form_render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.PlayerOriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import net.onixary.shapeShifterCurseFabric.player_form.IForm;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.util.FormTextureUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.model.GeoBone;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FormRenderUtils {
    public static final HashMap<Identifier, Supplier<IModelAnimationSystem>> modelAnimationSystemRegistry = new HashMap<>();
    public static final HashMap<Identifier, Predicate<Player>> conditionRegistry = new HashMap<>();
    static {
        registerCondition(ShapeShifterCurseFabric.identifier("always_true"), player -> true);
        registerCondition(ShapeShifterCurseFabric.identifier("always_false"), player -> false);
        registerCondition(ShapeShifterCurseFabric.identifier("is_sneaking"), Entity::isShiftKeyDown);
        registerCondition(ShapeShifterCurseFabric.identifier("is_sprinting"), Entity::isSprinting);
    }

    public static void registerCondition(Identifier identifier, Predicate<Player> condition) {
        conditionRegistry.put(identifier, condition);
    }

    public static boolean isRenderingInWorld = false;

    // { "layer(slot)": {"form": formRenderer} }
    public static final HashMap<Identifier, HashMap<Identifier, FormRenderer>> formRendererRegistry = new HashMap<>();

    public static final Identifier DEFAULT_MAS = register_MAS(ShapeShifterCurseFabric.identifier("default"), DefaultModelAnimationSystem::new);

    public static class BoneBipedState {
        public final float x;
        public final float y;
        public final float z;
        public final float rot_x;
        public final float rot_y;
        public final float rot_z;
        public final float pivot_x;
        public final float pivot_y;
        public final float pivot_z;
        public final float scale_x;
        public final float scale_y;
        public final float scale_z;

        private @Nullable ModelPart cachedPart = null;
        @SuppressWarnings("removal")
        private @Nullable GeoBone cachedBone = null;

        public BoneBipedState(float x, float y, float z, float rot_x, float rot_y, float rot_z, float pivot_x, float pivot_y, float pivot_z, float scale_x, float scale_y, float scale_z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rot_x = rot_x;
            this.rot_y = rot_y;
            this.rot_z = rot_z;
            this.pivot_x = pivot_x;
            this.pivot_y = pivot_y;
            this.pivot_z = pivot_z;
            this.scale_x = scale_x;
            this.scale_y = scale_y;
            this.scale_z = scale_z;
        }

        public BoneBipedState(ModelPart part) {
            this(0f, 0f, 0f, part.xRot, part.yRot, part.zRot, part.x, part.y, part.z, part.xScale, part.yScale, part.zScale);
            this.cachedPart = part;
        }

        @SuppressWarnings("removal")
        public BoneBipedState(GeoBone bone) {
            this(bone.getPosX(), bone.getPosY(), bone.getPosZ(), bone.getRotX(), bone.getRotY(), bone.getRotZ(), bone.getPivotX(), bone.getPivotY(), bone.getPivotZ(), bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            this.cachedBone = bone;
        }

        public void apply(ModelPart part) {
            part.xRot = rot_x;
            part.yRot = rot_y;
            part.zRot = rot_z;
            part.x = pivot_x;
            part.y = pivot_y;
            part.z = pivot_z;
            part.xScale = scale_x;
            part.yScale = scale_y;
            part.zScale = scale_z;
        }

        @SuppressWarnings("removal")
        public void apply(GeoBone bone) {
            bone.setPosX(x);
            bone.setPosY(y);
            bone.setPosZ(z);
            bone.setRotX(rot_x);
            bone.setRotY(rot_y);
            bone.setRotZ(rot_z);
            bone.setPivotX(pivot_x);
            bone.setPivotY(pivot_y);
            bone.setPivotZ(pivot_z);
            bone.setScaleX(scale_x);
            bone.setScaleY(scale_y);
            bone.setScaleZ(scale_z);
        }

        public void restore() {
            if (cachedPart != null) {
                apply(cachedPart);
            }
            if (cachedBone != null) {
                apply(cachedBone);
            }
        }
    }

    public static void onClientInit() {
        WorldRenderEvents.END.register(context -> isRenderingInWorld = false);
        WorldRenderEvents.START.register(context -> isRenderingInWorld = true);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FormModelResourceReloadListener());
    }

    public static Identifier register_MAS(Identifier id, Supplier<IModelAnimationSystem> supplier) {
        modelAnimationSystemRegistry.put(id, supplier);
        return id;
    }

    public static @Nullable IModelAnimationSystem get_MAS(Identifier id, @Nullable JsonObject json) {
	    if (id == null) {
		    return null;
	    }

        @Nullable Supplier<IModelAnimationSystem> supplier = modelAnimationSystemRegistry.get(id);
        if (supplier != null) {
	        try {
		        IModelAnimationSystem system = supplier.get();
		        if (system != null) {
			        system.loadConfig(json);
			        return system;
		        }
	        } catch (Exception e) {
		        ShapeShifterCurseFabric.LOGGER.warn("Failed to create or configure animation system for identifier: {}", id, e);
	        }
        }
        return null;
    }

    public static void registerFormRenderer(Identifier slotID, Identifier formID, FormRenderer renderer) {
        formRendererRegistry.computeIfAbsent(slotID, k -> new HashMap<>()).put(formID, renderer);
    }

    public static @Nullable FormRenderer getFormRenderer(Identifier slotID, Identifier formID) {
        return formRendererRegistry.getOrDefault(slotID, new HashMap<>()).get(formID);
    }

    public static void loadFormRenderer(Identifier slotID, Identifier formID, FormRenderer renderer) {
        formRendererRegistry.computeIfAbsent(slotID, k -> new HashMap<>()).put(formID, renderer);
    }

    public static Vec3 getPartPosition(ModelPart part) {
        var t = part.storePose();
        return new Vec3(t.x, t.y, t.z).reverse();
    }

    public static Vec3 getPartRotation(ModelPart part) {
        var t = part.storePose();
        return new Vec3(t.xRot, t.yRot, t.zRot);
    }

    @SuppressWarnings("removal")
    public static PoseStack computeModelMatrixStack(GeoBone bone) {
        PoseStack matrices = new PoseStack();
        if (bone == null) return matrices;
        List<GeoBone> chain = new ArrayList<>();
        for (GeoBone b = bone; b != null; b = b.getParent()) {
            chain.add(b);
        }
        Collections.reverse(chain);
        // matrices.translate(0.5F, 0.51F, 0.5F);
        for (int i = 0; i < chain.size(); i++) {
            GeoBone b = chain.get(i);
            matrices.translate(-b.getPosX(), b.getPosY(), b.getPosZ());
            matrices.translate(b.getPivotX(), b.getPivotY(), b.getPivotZ());
            matrices.mulPose(Axis.ZP.rotation(b.getRotZ()));
            matrices.mulPose(Axis.YP.rotation(b.getRotY()));
            matrices.mulPose(Axis.XP.rotation(b.getRotX()));
            matrices.scale(b.getScaleX(), b.getScaleY(), b.getScaleZ());
            if (i < chain.size() - 1) {
                matrices.translate(-b.getPivotX(), -b.getPivotY(), -b.getPivotZ());
            }
        }
        return matrices;
    }

    public static Vec3 getPartScale(ModelPart part) {
        return new Vec3(part.xScale, part.yScale, part.zScale);
    }

    public static @Nullable FormRenderer searchFirstRenderer(Player player, Predicate<FormRenderer> predicate) {
        return getPlayerAllFormRenderer(player).stream().filter(predicate).findFirst().orElse(null);
    }

    // Origins 版本核心 如果需要重构形态系统需要重新写一份这个函数
    public static List<FormRenderer> getPlayerAllFormRenderer(Player player) {
        if (FormTextureUtils.useTempFormModel && Objects.equals(player, Minecraft.getInstance().player)) {
            List<FormRenderer> formRenderers = new ArrayList<>();
            Identifier formID = FormTextureUtils.tempFormModelProcessor.getLayerID();
            FormRenderer formRenderer = FormRenderUtils.getFormRenderer(Identifier.fromNamespaceAndPath("origins", "origin"), formID);
            if (formRenderer == null) {
                ShapeShifterCurseFabric.LOGGER.warn("ShapeShifterCurseFabric: PlayerFormDynamic.ModelID is not null, but the model is not registered: {}", formID);
                return new ArrayList<>();
            }
            formRenderers.add(formRenderer);
            return formRenderers;
        }
        try {
            // IForm playerFormBase = FormUtils.getPlayerForm(player);
            // if (playerFormBase instanceof DynamicForm pfd) {
            //     List<FormRenderer> formRenderers = new ArrayList<>();
            //     Pair<Identifier, Identifier> currentLayer = pfd.getCurrentRenderLayer();
            //     if (currentLayer != null) {
            //         FormRenderer formRenderer = FormRenderUtils.getFormRenderer(currentLayer.getLeft(), currentLayer.getRight());
            //         if (formRenderer == null) {
            //             ShapeShifterCurseFabric.LOGGER.warn("ShapeShifterCurseFabric: PlayerFormDynamic.layerRenderOverwrite is not null, but the model is not registered: {} - {}", currentLayer.getLeft(), currentLayer.getRight());
            //             return new ArrayList<>();
            //         }
            //         formRenderers.add(formRenderer);
            //         return formRenderers;
            //     }
            // }
            IForm playerFormBase = FormUtils.getPlayerForm(player);
            Tuple<Identifier, Identifier> currentLayer = playerFormBase.getRenderLayerOverride();
            if (currentLayer != null) {
                List<FormRenderer> formRenderers = new ArrayList<>();
                FormRenderer formRenderer = FormRenderUtils.getFormRenderer(currentLayer.getA(), currentLayer.getB());
                if (formRenderer == null) {
                    ShapeShifterCurseFabric.LOGGER.warn("ShapeShifterCurseFabric: IForm.layerRenderOverwrite is not null, but the model is not registered: {} - {}", currentLayer.getA(), currentLayer.getB());
                    return new ArrayList<>();
                }
                formRenderers.add(formRenderer);
                return formRenderers;
            }
        } catch (Exception ignored) {}
        PlayerOriginComponent poc = (PlayerOriginComponent) ModComponents.ORIGIN.get(player);
        HashMap<OriginLayer, Origin> OriginData = poc.getOrigins();
        List<FormRenderer> formRenderers = new ArrayList<>();
        for (Map.Entry<OriginLayer, Origin> entry : OriginData.entrySet()) {
            Identifier layer = entry.getKey().getIdentifier();
            Identifier form = entry.getValue().getIdentifier();
            FormRenderer formRenderer = FormRenderUtils.getFormRenderer(layer, form);
            if (formRenderer != null) {
                formRenderers.add(formRenderer);
            }
        }
        return formRenderers;
    }
}