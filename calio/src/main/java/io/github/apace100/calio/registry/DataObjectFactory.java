package io.github.apace100.calio.registry;

import io.github.apace100.calio.data.SerializableData;

public interface DataObjectFactory<T> {

    SerializableData getData();
    T fromData(SerializableData.Instance instance);
    SerializableData.Instance toData(T t);
}
