package net.onixary.shapeShifterCurseFabric.status_effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

// 暂时弃用，描述放在书页UI中
public class TStatusHUDHandler {
    public static void register(){
        // 在原版效果列表下方添加描述
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) {
                ScreenEvents.afterRender(screen).register((_screen, context, mouseX, mouseY, delta) -> {
                    if (Minecraft.getInstance().player == null) return;

                    // 原版状态栏的起始坐标（需根据版本调整）
                    int baseX = width - 120;
                    int baseY = (int)(30 / 0.5);

                    // 遍历所有药水效果
                    int index = 0;
                    for (MobEffectInstance effect : Minecraft.getInstance().player.getActiveEffects()) {
                        if (effect.getEffect() instanceof BaseTransformativeStatusEffect) {
                            Component description = Component.translatable(effect.getDescriptionId() + ".description");

                            PoseStack matrices = context.pose();
                            matrices.pushPose();
                            matrices.scale(0.5f, 0.5f, 1.0f); // 缩放为 75% 大小
                            // 计算 Y 坐标（每个效果间隔 20 像素）
                            int y = baseY + (index * (int)(20 / 0.5));

                            context.drawString(
                                    client.font,
                                    description,
                                    (int)(baseX / 0.5f),
                                    (int)(y / 0.5f),
                                    0xFFFFFF
                            );
                            matrices.popPose();
                        }
                        index++;
                    }
                });
            }
        });
    }
}
