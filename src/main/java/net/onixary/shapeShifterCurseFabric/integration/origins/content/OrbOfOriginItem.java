package net.onixary.shapeShifterCurseFabric.integration.origins.content;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.onixary.shapeShifterCurseFabric.integration.origins.Origins;
import net.onixary.shapeShifterCurseFabric.integration.origins.component.OriginComponent;
import net.onixary.shapeShifterCurseFabric.integration.origins.networking.ModPackets;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.Origin;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayer;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginLayers;
import net.onixary.shapeShifterCurseFabric.integration.origins.origin.OriginRegistry;
import net.onixary.shapeShifterCurseFabric.integration.origins.registry.ModComponents;
import net.onixary.shapeShifterCurseFabric.networking.BytePayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrbOfOriginItem extends Item {

    public OrbOfOriginItem() {
        super(new Item.Settings().maxCount(1).rarity(Rarity.RARE));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(!world.isClient) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            Map<OriginLayer, Origin> targets = getTargets(stack);
	        if (!targets.isEmpty()) {
                for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
                    component.setOrigin(target.getKey(), target.getValue());
                }
            } else {
                for (OriginLayer layer : OriginLayers.getLayers()) {
                    if(layer.isEnabled()) {
                        component.setOrigin(layer, Origin.EMPTY);
                    }
                }
            }
            component.checkAutoChoosingLayers(user, false);
            component.sync();
            PacketByteBuf data = PacketByteBufs.create();
            data.writeBoolean(false);
	        ServerPlayNetworking.send((ServerPlayerEntity) user, new BytePayload(BytePayload.id(ModPackets.OPEN_ORIGIN_SCREEN), data));
        }
        if(!user.isCreative()) {
            stack.decrement(1);
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        Map<OriginLayer, Origin> targets = getTargets(stack);
        for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
            if(target.getValue() == Origin.EMPTY) {
                tooltip.add(Text.translatable("item.origins.orb_of_origin.layer_generic",
                    Text.translatable(target.getKey().getTranslationKey())).formatted(Formatting.GRAY));
            } else {
                tooltip.add(Text.translatable("item.origins.orb_of_origin.layer_specific",
                    Text.translatable(target.getKey().getTranslationKey()),
                    target.getValue().getName()).formatted(Formatting.GRAY));
            }
        }
    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {
        HashMap<OriginLayer, Origin> targets = new HashMap<>();
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(customData == null) {
            return targets;
        }
        NbtCompound nbt = customData.copyNbt();
        if(!nbt.contains("Targets", NbtElement.LIST_TYPE)) {
            return targets;
        }

	    NbtElement targetsElement = nbt.get("Targets");
	    if (!(targetsElement instanceof NbtList targetList)) {
		    return targets;
	    }

        for (NbtElement nbtElement : targetList) {
            if(nbtElement instanceof NbtCompound targetNbt) {
                if(targetNbt.contains("Layer", NbtElement.STRING_TYPE)) {
                    try {
                        Identifier id = Identifier.tryParse(targetNbt.getString("Layer"));
	                    if (id == null) {
		                    continue;
	                    }

                        OriginLayer layer = OriginLayers.getLayer(id);
	                    if (layer == null) {
		                    continue;
	                    }

                        Origin origin = Origin.EMPTY;
                        if(targetNbt.contains("Origin", NbtElement.STRING_TYPE)) {
                            Identifier originId = Identifier.tryParse(targetNbt.getString("Origin"));
	                        if (originId != null) {
		                        Origin registryOrigin = OriginRegistry.get(originId);
		                        if (registryOrigin != null) {
			                        origin = registryOrigin;
		                        }
	                        }
                        }
                        if(layer.isEnabled() && (layer.contains(origin) || origin.isSpecial())) {
                            targets.put(layer, origin);
                        }
                    } catch (Exception e) {
	                    Origins.LOGGER.warn("Failed to parse origin target from NBT", e);
                    }
                }
            }
        }
        return targets;
    }
}
