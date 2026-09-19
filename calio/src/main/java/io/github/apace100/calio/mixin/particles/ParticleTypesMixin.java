package io.github.apace100.calio.mixin.particles;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.calio.util.extensions.LegacyParticleOptionFactory;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleTypes.class)
public class ParticleTypesMixin {
    @ModifyReturnValue(method = "register(Ljava/lang/String;ZLjava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/core/particles/ParticleType;", at = @At("RETURN"))
    private static <T extends ParticleOptions> ParticleType<T> calio$addLegacyParticleFactories(ParticleType<T> original, @Local(argsOnly = true) String id) {
        if (!(original instanceof LegacyParticleOptionFactory factory))
            return original;

        switch (id) {
            case "block", "block_marker", "falling_dust" -> factory.calio$addLegacyParticleOptionFactory((data, provider) -> {
                try {
                    return new BlockParticleOption((ParticleType<BlockParticleOption>) original, BlockStateParser.parseForBlock(provider.lookupOrThrow(Registries.BLOCK), data, false).blockState());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "dust" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var reader = new StringReader(data);
                    var rgb = readVector3f(reader);
                    reader.expect(' ');
                    var scale = reader.readFloat();

                    return new DustParticleOptions(rgb, scale);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "dust_color_transition" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var reader = new StringReader(data);
                    var rgb = readVector3f(reader);
                    reader.expect(' ');
                    var rgb2 = readVector3f(reader);
                    reader.expect(' ');
                    var scale = reader.readFloat();

                    return new DustColorTransitionOptions(rgb, rgb2, scale);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "item" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var stack = new ItemParser(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)).parse(new StringReader(data));
                    return new ItemParticleOption((ParticleType<ItemParticleOption>) original, new ItemStack(stack.item(), 1, stack.components()));
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "vibration" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var reader = new StringReader(data);
                    var x = reader.readDouble();
                    reader.expect(' ');
                    var y = reader.readDouble();
                    reader.expect(' ');
                    var z = reader.readDouble();
                    reader.expect(' ');
                    var ticks = reader.readInt();

                    var pos = BlockPos.containing(x, y, z);
                    return new VibrationParticleOption(new BlockPositionSource(pos), ticks);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "shriek" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var reader = new StringReader(data);
                    var delay = reader.readInt();

                    return new ShriekParticleOption(delay);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });

            case "sculk_charge" -> factory.calio$addLegacyParticleOptionFactory(data -> {
                try {
                    var reader = new StringReader(data);
                    var roll = reader.readFloat();

                    return new SculkChargeParticleOptions(roll);
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return original;
    }

    private static Vector3f readVector3f(StringReader reader) throws CommandSyntaxException {
        var r = reader.readFloat();
        reader.expect(' ');
        var g = reader.readFloat();
        reader.expect(' ');
        var b = reader.readFloat();

        return new Vector3f(r, g, b);
    }
}
