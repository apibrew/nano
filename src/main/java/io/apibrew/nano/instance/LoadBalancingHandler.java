package io.apibrew.nano.instance;

import io.apibrew.client.GenericRecord;
import io.apibrew.client.model.Extension;
import io.apibrew.common.ExtensionInfo;
import io.apibrew.common.ext.Condition;
import io.apibrew.common.ext.Handler;
import io.apibrew.common.ext.Operator;
import io.apibrew.nano.model.Code;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LoadBalancingHandler implements Handler<GenericRecord> {
    private final Handler<GenericRecord> delegate;

    @Override
    public Handler<GenericRecord> when(Condition<GenericRecord> condition) {
        return new LoadBalancingHandler(delegate.when(condition));
    }

    @Override
    public Handler<GenericRecord> configure(Function<ExtensionInfo, ExtensionInfo> function) {
        return new LoadBalancingHandler(delegate.configure(function));
    }

    @Override
    public String operate(Operator<GenericRecord> operator) {
        return delegate.operate(handler -> {
            return operator.operate(handler);
        });
    }

    @Override
    public String operate(BiFunction<Extension.Event, GenericRecord, GenericRecord> biFunction) {
        return delegate.operate((event, genericRecord) -> {
            return biFunction.apply(event, genericRecord);
        });
    }

    @Override
    public void unRegister(String s) {
        delegate.unRegister(s);
    }
}
