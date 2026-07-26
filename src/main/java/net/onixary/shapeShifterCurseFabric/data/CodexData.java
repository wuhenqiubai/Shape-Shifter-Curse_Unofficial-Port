package net.onixary.shapeShifterCurseFabric.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.cursed_moon.CursedMoon;
import net.onixary.shapeShifterCurseFabric.player_form.utils.FormUtils;
import net.onixary.shapeShifterCurseFabric.status_effects.attachment.EffectManager;

public class CodexData {
    // 集中管理Codex的数据

    public static enum ContentType{
        TITLE,
        EQUIP,
        APPEARANCE,
        PROS,
        CONS,
        INSTINCTS,
        NAME
    }
    // static texts
    // headers
    public static final Component headerStatus = Component.translatable("codex.header.status");
    public static final Component headerEquip = Component.translatable("codex.header.equip");
    public static final Component headerAppearance = Component.translatable("codex.header.appearance");
    public static final Component headerPros = Component.translatable("codex.header.pros");
    public static final Component headerCons = Component.translatable("codex.header.cons");
    public static final Component headerInstincts = Component.translatable("codex.header.instincts");
    // status
    private static final Component statusNormal = Component.translatable("codex.status.normal");
    private static final Component statusInfected = Component.translatable("codex.status.infected");
    private static final Component statusBeforeMoon = Component.translatable("codex.status.before_moon");
    private static final Component statusUnderMoon = Component.translatable("codex.status.under_moon");
    // description text before content
    private static final Component descAppearance_normal = Component.translatable("codex.desc.appearance_normal");
    private static final Component descPros_normal = Component.translatable("codex.desc.pros_normal");
    private static final Component descCons_normal = Component.translatable("codex.desc.cons_normal");
    private static final Component descInstincts_normal = Component.translatable("codex.desc.instincts_normal");
    private static final Component descAppearance_0 = Component.translatable("codex.desc.appearance_0");
    private static final Component descPros_0 = Component.translatable("codex.desc.pros_0");
    private static final Component descCons_0 = Component.translatable("codex.desc.cons_0");
    private static final Component descInstincts_0 = Component.translatable("codex.desc.instincts_0");
    private static final Component descAppearance_1 = Component.translatable("codex.desc.appearance_1");
    private static final Component descPros_1 = Component.translatable("codex.desc.pros_1");
    private static final Component descCons_1 = Component.translatable("codex.desc.cons_1");
    private static final Component descInstincts_1 = Component.translatable("codex.desc.instincts_1");
    private static final Component descAppearance_2 = Component.translatable("codex.desc.appearance_2");
    private static final Component descPros_2 = Component.translatable("codex.desc.pros_2");
    private static final Component descCons_2 = Component.translatable("codex.desc.cons_2");
    private static final Component descInstincts_2 = Component.translatable("codex.desc.instincts_2");
    private static final Component descAppearance_3 = Component.translatable("codex.desc.appearance_3");
    private static final Component descPros_3 = Component.translatable("codex.desc.pros_3");
    private static final Component descCons_3 = Component.translatable("codex.desc.cons_3");
    private static final Component descInstincts_3 = Component.translatable("codex.desc.instincts_3");
    
    public static Component getPlayerStatusText(Player player){
        // 根据当前角色状态与环境返回对应的状态文本
        StringBuilder statusTextBuilder = new StringBuilder();
        boolean hasAnyStatus = false;

        /* 重构后不需要了 仅用于参考旧实现逻辑
        PlayerEffectAttachment currentTransformEffect;

        // 使用环境检测而不是玩家类型检测
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && player instanceof ClientPlayerEntity) {
            currentTransformEffect = ClientEffectAttachmentCache.getAttachment();
        } else {
            currentTransformEffect = player.getAttached(EFFECT_ATTACHMENT);
        }

        if(currentTransformEffect != null && currentTransformEffect.currentToForm != null){
            ShapeShifterCurseFabric.LOGGER.info("current effect successfully receive: " + currentTransformEffect.currentEffect);
            statusTextBuilder.append(statusInfected.getString());
            hasAnyStatus = true;
        }
         */
        if (EffectManager.hasTransformativeEffect(player)) {
            statusTextBuilder.append(statusInfected.getString());
            hasAnyStatus = true;
        }

        if(CursedMoon.isCursedMoonDay(player.level())){
            if(CursedMoon.isNight(player.level())){
                statusTextBuilder.append(statusUnderMoon.getString());
                hasAnyStatus = true;
            }
            else{
                statusTextBuilder.append(statusBeforeMoon.getString());
                hasAnyStatus = true;
            }
        }

        if(!hasAnyStatus){
            statusTextBuilder.append(statusNormal.getString());
        }

        return Component.literal(statusTextBuilder.toString());
    }

    public static Component getDescText(ContentType type, Player player) {
        int tier = FormUtils.getPlayerForm(player).getFormTier();
        // 什么时候也得把 本能Desc 改一下
        switch (type) {
            case INSTINCTS -> {
                return switch (tier) {
                    case -1, 0 -> descInstincts_normal;
                    case 1 -> descInstincts_0;
                    case 2 -> descInstincts_1;
                    case 3 -> descInstincts_2;
                    case 4 -> descInstincts_3;
                    default -> Component.empty();
                };
            }
        }
        return Component.empty();
    }

    public static Component getContentText(ContentType type, Player player){
        return FormUtils.getPlayerForm(player).getContentText(type);
    }
}
