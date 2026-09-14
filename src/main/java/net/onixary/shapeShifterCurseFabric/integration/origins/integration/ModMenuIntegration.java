package net.onixary.shapeShifterCurseFabric.integration.origins.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.apace100.apoli.util.ApoliConfigClient;
// 26.1：Cloth Config 把 getConfigScreen 从 AutoConfig 搬到了 AutoConfigClient
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfigClient.getConfigScreen(ApoliConfigClient.class, parent).get();
    }
}