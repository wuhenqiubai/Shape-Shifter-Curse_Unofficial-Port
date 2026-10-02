package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.util.ApoliConfigClient;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Field;

@Pseudo
@Mixin(ConfigScreenProvider.class)
public class ConfigScreenProviderMixin {

    @WrapOperation(method = "get()Lnet/minecraft/client/gui/screens/Screen;", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getDeclaredFields()[Ljava/lang/reflect/Field;"))
    private Field[] getEvenSuperFields(Class aClass, Operation<Field[]> original) {
        if(aClass == ApoliConfigClient.class) {
            return aClass.getFields();
        } else {
            return original.call(aClass);
        }
    }
}
