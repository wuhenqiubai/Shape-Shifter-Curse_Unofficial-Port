package net.onixary.shapeShifterCurseFabric.integration.origins.content;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if(!world.isClientSide()) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            Map<OriginLayer, Origin> targets = getTargets(stack);
            if(targets.size() > 0) {
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
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
            data.writeBoolean(false);
	        ServerPlayNetworking.send((ServerPlayer) user, new BytePayload(BytePayload.id(ModPackets.OPEN_ORIGIN_SCREEN), data));
        }
        if(!user.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        Map<OriginLayer, Origin> targets = getTargets(stack);
        for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
            if(target.getValue() == Origin.EMPTY) {
                tooltip.add(Component.translatable("item.origins.orb_of_origin.layer_generic",
                    Component.translatable(target.getKey().getTranslationKey())).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("item.origins.orb_of_origin.layer_specific",
                    Component.translatable(target.getKey().getTranslationKey()),
                    target.getValue().getName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {
        HashMap<OriginLayer, Origin> targets = new HashMap<>();
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if(customData == null) {
            return targets;
        }
        CompoundTag nbt = customData.copyTag();
        if(!nbt.contains("Targets")) {
            return targets;
        }

	    Tag targetsElement = nbt.get("Targets");
	    if (!(targetsElement instanceof ListTag targetList)) {
		    return targets;
	    }

        for (Tag nbtElement : targetList) {
            if(nbtElement instanceof CompoundTag targetNbt) {
                if(targetNbt.contains("Layer")) {
                    try {
                        Identifier id = Identifier.tryParse(String.valueOf(targetNbt.getString("Layer")));
	                    if (id == null) {
		                    continue;
	                    }

                        OriginLayer layer = OriginLayers.getLayer(id);
	                    if (layer == null) {
		                    continue;
	                    }

                        Origin origin = Origin.EMPTY;
                        if(targetNbt.contains("Origin")) {
                            Identifier originId = Identifier.parse(String.valueOf(targetNbt.getString("Origin")));
                            origin = OriginRegistry.get(originId);
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