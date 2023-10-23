package io.apibrew.nano.instance.proxy;

import io.apibrew.client.model.Resource;
import io.apibrew.nano.instance.GraalVMCodeExecutor;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

@RequiredArgsConstructor
public class LoadResourceProxy implements ProxyExecutable {
    private final GraalVMCodeExecutor graalVMCodeExecutor;

    @Override
    public Object execute(Value... arguments) {
        Resource resource;
        if (arguments.length == 0) {
            throw new IllegalArgumentException("Resource name is required");
        } else if (arguments.length == 1) {
            resource = this.graalVMCodeExecutor.getDataStore().getResourceByName("default", arguments[0].asString());
            return Value.asValue(new ResourceObjectProxy(graalVMCodeExecutor, resource));
        } else if (arguments.length == 2) {
            resource = this.graalVMCodeExecutor.getDataStore().getResourceByName(arguments[0].asString(), arguments[1].asString());
            return Value.asValue(new ResourceObjectProxy(graalVMCodeExecutor, resource));
        } else {
            throw new IllegalArgumentException("Too many arguments");
        }
    }
}
