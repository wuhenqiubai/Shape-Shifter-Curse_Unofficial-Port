package net.onixary.shapeShifterCurseFabric.mana;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class ManaHandler {
    // 由于在Server端上没有ClientPlayerEntity 所以通一使用PlayerEntity
    private @NotNull BiConsumer<ManaComponent, Player> onClientInit;
    private @NotNull BiConsumer<ManaComponent, Player> onServerInit;
    private @NotNull BiConsumer<ManaComponent, Player> onClientManaTick;
    private @NotNull BiConsumer<ManaComponent, Player> onServerManaTick;
    private @NotNull BiConsumer<ManaComponent, Player> onClientManaFull;
    private @NotNull BiConsumer<ManaComponent, Player> onServerManaFull;
    private @NotNull BiConsumer<ManaComponent, Player> onClientManaEmpty;
    private @NotNull BiConsumer<ManaComponent, Player> onServerManaEmpty;
    private @NotNull BiConsumer<ManaComponent, Player> onClientManaChange;
    private @NotNull BiConsumer<ManaComponent, Player> onServerManaChange;
    private boolean Immutable = false;

    public ManaHandler() {
        this.onClientInit = (component, player) -> {};
        this.onServerInit = (component, player) -> {};
        this.onClientManaTick = (component, player) -> {};
        this.onServerManaTick = (component, player) -> {};
        this.onClientManaFull = (component, player) -> {};
        this.onServerManaFull = (component, player) -> {};
        this.onClientManaEmpty = (component, player) -> {};
        this.onServerManaEmpty = (component, player) -> {};
        this.onClientManaChange = (component, player) -> {};
        this.onServerManaChange = (component, player) -> {};
        this.Immutable = false;
    }

    public ManaHandler(@NotNull BiConsumer<ManaComponent, Player> onClientInit,
                       @NotNull BiConsumer<ManaComponent, Player> onServerInit,
                       @NotNull BiConsumer<ManaComponent, Player> onClientManaTick,
                       @NotNull BiConsumer<ManaComponent, Player> onServerManaTick,
                       @NotNull BiConsumer<ManaComponent, Player> onClientManaFull,
                       @NotNull BiConsumer<ManaComponent, Player> onServerManaFull,
                       @NotNull BiConsumer<ManaComponent, Player> onClientManaEmpty,
                       @NotNull BiConsumer<ManaComponent, Player> onServerManaEmpty,
                       @NotNull BiConsumer<ManaComponent, Player> onClientManaChange,
                       @NotNull BiConsumer<ManaComponent, Player> onServerManaChange
    ) {
        this.onClientInit = onClientInit;
        this.onServerInit = onServerInit;
        this.onClientManaTick = onClientManaTick;
        this.onServerManaTick = onServerManaTick;
        this.onClientManaFull = onClientManaFull;
        this.onServerManaFull = onServerManaFull;
        this.onClientManaEmpty = onClientManaEmpty;
        this.onServerManaEmpty = onServerManaEmpty;
        this.onClientManaChange = onClientManaChange;
        this.onServerManaChange = onServerManaChange;
        this.Immutable = false;
    }

    public ManaHandler setOnClientInit(@NotNull BiConsumer<ManaComponent, Player> onClientInit) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onClientInit = onClientInit;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnClientInit() {
        return this.onClientInit;
    }
    public ManaHandler setOnServerInit(@NotNull BiConsumer<ManaComponent, Player> onServerInit) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onServerInit = onServerInit;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnServerInit() {
        return this.onServerInit;
    }
    public ManaHandler setOnClientManaTick(@NotNull BiConsumer<ManaComponent, Player> onClientManaTick) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onClientManaTick = onClientManaTick;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnClientManaTick() {
        return this.onClientManaTick;
    }
    public ManaHandler setOnServerManaTick(@NotNull BiConsumer<ManaComponent, Player> onServerManaTick) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onServerManaTick = onServerManaTick;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnServerManaTick() {
        return this.onServerManaTick;
    }
    public ManaHandler setOnClientManaFull(@NotNull BiConsumer<ManaComponent, Player> onClientManaFull) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onClientManaFull = onClientManaFull;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnClientManaFull() {
        return this.onClientManaFull;
    }
    public ManaHandler setOnServerManaFull(@NotNull BiConsumer<ManaComponent, Player> onServerManaFull) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onServerManaFull = onServerManaFull;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnServerManaFull() {
        return this.onServerManaFull;
    }
    public ManaHandler setOnClientManaEmpty(@NotNull BiConsumer<ManaComponent, Player> onClientManaEmpty) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onClientManaEmpty = onClientManaEmpty;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnClientManaEmpty() {
        return this.onClientManaEmpty;
    }
    public ManaHandler setOnServerManaEmpty(@NotNull BiConsumer<ManaComponent, Player> onServerManaEmpty) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onServerManaEmpty = onServerManaEmpty;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnServerManaEmpty() {
        return this.onServerManaEmpty;
    }
    public ManaHandler setOnClientManaChange(@NotNull BiConsumer<ManaComponent, Player> onClientManaChange) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onClientManaChange = onClientManaChange;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnClientManaChange() {
        return this.onClientManaChange;
    }
    public ManaHandler setOnServerManaChange(@NotNull BiConsumer<ManaComponent, Player> onServerManaChange) {
        if (this.Immutable) { throw new RuntimeException("Cannot modify a immutable ManaHandler"); }
        this.onServerManaChange = onServerManaChange;
        return this;
    }
    public @NotNull BiConsumer<ManaComponent, Player> getOnServerManaChange() {
        return this.onServerManaChange;
    }
    public ManaHandler setImmutable() {
        this.Immutable = true;
        return this;
    }
}