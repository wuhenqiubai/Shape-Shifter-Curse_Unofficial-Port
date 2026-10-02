package io.github.apace100.apoli.power.factory.action;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.networking.PlayerMountPacket;
import io.github.apace100.apoli.power.factory.action.bientity.DamageAction;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

public class BiEntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.BIENTITY_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, ApoliDataTypes.BIENTITY_CONDITION));
        register(ChoiceAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, ApoliDataTypes.BIENTITY_CONDITION));
        register(DelayAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, entities -> !entities.getA().level().isClientSide()));

        register(new ActionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("action", ApoliDataTypes.BIENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Tuple<Entity, Entity>>.Instance)data.get("action")).accept(new Tuple<>(entities.getB(), entities.getA()));
            }));
        register(new ActionFactory<>(Apoli.identifier("actor_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getA());
            }));
        register(new ActionFactory<>(Apoli.identifier("target_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getB());
            }));

        register(new ActionFactory<>(Apoli.identifier("mount"), new SerializableData(),
            (data, entities) -> {
                entities.getA().startRiding(entities.getB(), true, true);
                if(!entities.getA().level().isClientSide() && entities.getB() instanceof Player) {
                    ServerPlayNetworking.send((ServerPlayer) entities.getB(), new PlayerMountPacket(entities.getA().getId(), entities.getB().getId()));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("set_in_love"), new SerializableData(),
            (data, entities) -> {
                if(entities.getB() instanceof Animal && entities.getA() instanceof Player) {
                    ((Animal)entities.getB()).setInLove((Player)entities.getA());
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("tame"), new SerializableData(),
            (data, entities) -> {
                if(entities.getB() instanceof TamableAnimal && entities.getA() instanceof Player) {
                    if(!((TamableAnimal)entities.getB()).isTame()) {
                        ((TamableAnimal)entities.getB()).tame((Player)entities.getA());
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("client", SerializableDataTypes.BOOLEAN, true)
            .add("server", SerializableDataTypes.BOOLEAN, true)
            .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entities) -> {
                Entity actor = entities.getA(), target = entities.getB();
                if (target instanceof Player
                    && (target.level().isClientSide() ?
                        !data.getBoolean("client") : !data.getBoolean("server")))
                    return;
                Vector3f vec = new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                TriConsumer<Float, Float, Float> method = target::push;
                if(data.getBoolean("set"))
                    method = target::setDeltaMovement;
                Space.transformVectorToBase(target.position().subtract(actor.position()), vec, actor.getYRot(), true); // vector normalized by method
                method.accept(vec.x, vec.y, vec.z);
                target.hurtMarked = true;
            }));
        register(DamageAction.getFactory());
    }

    private static void register(ActionFactory<Tuple<Entity, Entity>> actionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
