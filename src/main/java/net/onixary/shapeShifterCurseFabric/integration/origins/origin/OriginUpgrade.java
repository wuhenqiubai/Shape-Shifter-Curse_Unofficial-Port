package net.onixary.shapeShifterCurseFabric.integration.origins.origin;

import com.google.gson.JsonElement;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.onixary.shapeShifterCurseFabric.integration.origins.data.OriginsDataTypes;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Deprecated
public record OriginUpgrade(ResourceLocation advancementCondition, ResourceLocation upgradeToOrigin, @Nullable String announcement) {

    public static final SerializableData DATA = new SerializableData()
        .add("condition", SerializableDataTypes.IDENTIFIER)
        .add("origin", SerializableDataTypes.IDENTIFIER)
        .add("announcement", SerializableDataTypes.STRING, null);

    @Deprecated
    public ResourceLocation getAdvancementCondition() {
        return advancementCondition;
    }

    @Deprecated
    public ResourceLocation getUpgradeToOrigin() {
        return upgradeToOrigin;
    }

    @Deprecated
    @Nullable
    public String getAnnouncement() {
        return announcement;
    }

    public SerializableData.Instance toData() {
        SerializableData.Instance data = DATA.new Instance();
        data.set("condition", advancementCondition);
        data.set("origin", upgradeToOrigin);
        data.set("announcement", announcement);
        return data;
    }

    public static OriginUpgrade fromData(SerializableData.Instance data) {
        return new OriginUpgrade(data.get("condition"), data.get("origin"), data.get("announcement"));
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        OriginsDataTypes.UPGRADE.send(buffer, this);
    }

    public static OriginUpgrade read(RegistryFriendlyByteBuf buffer) {
        return fromData(DATA.read(buffer));
    }

    public static OriginUpgrade fromJson(JsonElement jsonElement) {
        return OriginsDataTypes.UPGRADE.read(jsonElement);
    }

}