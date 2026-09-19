package io.github.apace100.calio.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Either;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class MergedHolderSet<T> implements HolderSet<T> {
    private final Collection<HolderSet<T>> holderSets;

    public MergedHolderSet(Collection<HolderSet<T>> holderSets) {
        this.holderSets = holderSets;
    }

    @Override
    public Stream<Holder<T>> stream() {
        return Streams.concat(holderSets.stream().map(HolderSet::stream).toArray(Stream[]::new));
    }

    @Override
    public int size() {
        int current = 0;

        for (HolderSet<T> set : holderSets) {
            current += set.size();
        }

        return current;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        var leftEithers = new ArrayList<TagKey<T>>();
        var rightEithers = new ArrayList<Holder<T>>();

        for (HolderSet<T> set : this.holderSets) {
            set.unwrap().ifLeft(leftEithers::add).ifRight(rightEithers::addAll);
        }

        if (!rightEithers.isEmpty()) {
            return Either.right(rightEithers);
        }

        if (!leftEithers.isEmpty()) {
            return Either.left(leftEithers.get(0));
        }

        return Either.right(rightEithers);
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        var list = this.holderSets.stream().flatMap(HolderSet::stream).toList();
        return Util.getRandomSafe(list, random);
    }

    @Override
    public Holder<T> get(int index) {
        return this.holderSets.stream().flatMap(HolderSet::stream).toList().get(index);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        for (HolderSet<T> set : holderSets) {
            if (set.contains(holder))
                return true;
        }

        return false;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        for (HolderSet<T> set : holderSets) {
            if (set.canSerializeIn(owner))
                return true;
        }

        return false;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        for (HolderSet<T> set : holderSets) {
            var key = set.unwrapKey();

            if (key.isPresent())
                return key;
        }

        return Optional.empty();
    }

    @Override
    public @NotNull Iterator<Holder<T>> iterator() {
        return Iterators.concat(this.holderSets.stream().map(e -> e.iterator()).toArray(Iterator[]::new));
    }
}
