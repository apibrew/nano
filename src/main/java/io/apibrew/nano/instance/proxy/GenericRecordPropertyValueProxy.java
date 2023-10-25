package io.apibrew.nano.instance.proxy;

import io.apibrew.client.model.Resource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericRecordPropertyValueProxy {
    private final Resource.Property property;
    private final Object value;

}
