package net.onixary.shapeShifterCurseFabric.player_form;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface IFormGroup {
    @NotNull ResourceLocation getGroupID();

    @NotNull Map<Integer, List<Tuple<Integer, IForm>>> getGroupData();

    default @NotNull IFormGroup registerForm(int formTier, int formWeight, IForm form) {
        getGroupData().computeIfAbsent(formTier, k -> new ArrayList<>()).add(new Tuple<>(formWeight, form));
        form.setFormGroup(this, formTier);
        return this;
    }

    default @Nullable List<Tuple<Integer, IForm>> getFormWeightList(int formTier) {
        return getGroupData().get(formTier);
    }

    default @Nullable List<IForm> getFormList(int formTier) {
        List<Tuple<Integer, IForm>> formWeightList = getFormWeightList(formTier);
        if (formWeightList == null) return null;
        List<IForm> forms = new ArrayList<>();
        for (Tuple<Integer, IForm> formWeight : formWeightList) {
            forms.add(formWeight.getB());
        }
        return forms;
    }

    default @Nullable IForm getRandomForm(int formTier, RandomSource random, @Nullable Predicate<IForm> predicate) {
        List<Tuple<Integer, IForm>> formWeightList = getFormWeightList(formTier);
        if (formWeightList == null) return null;
        List<Tuple<Integer, IForm>> eligible = new ArrayList<>();
        for (Tuple<Integer, IForm> pair : formWeightList) {
            IForm form = pair.getB();
            if (predicate == null || predicate.test(form)) {
                eligible.add(pair);
            }
        }
        if (eligible.isEmpty()) return null;
        int totalWeight = 0;
        for (Tuple<Integer, IForm> pair : eligible) {
            Integer weight = pair.getA();
            if (weight != null && weight > 0) {
                totalWeight += weight;
            }
        }
        if (totalWeight <= 0) return null;
        int randomValue = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Tuple<Integer, IForm> pair : eligible) {
            Integer weight = pair.getA();
            if (weight != null && weight > 0) {
                cumulative += weight;
                if (randomValue < cumulative) {
                    return pair.getB();
                }
            }
        }
        return null;
    }
}