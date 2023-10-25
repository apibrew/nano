package io.apibrew.nano.instance.proxy;

import io.apibrew.client.model.Resource;
import io.apibrew.nano.instance.CodeExecutor;
import io.apibrew.nano.instance.GraalVmNanoEngine;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

@RequiredArgsConstructor
public class LoadResourceProxy implements ProxyExecutable {
    private final CodeExecutor codeExecutor;

    @Override
    public Object execute(Value... arguments) {
        Resource resource;
        if (arguments.length == 0) {
            throw new IllegalArgumentException("Resource name is required");
        } else if (arguments.length == 1) {
            resource = codeExecutor.getResourceByName("default", arguments[0].asString());
            return Value.asValue(new ResourceObjectProxy(codeExecutor, resource));
        } else if (arguments.length == 2) {
            resource = codeExecutor.getResourceByName(arguments[0].asString(), arguments[1].asString());
            return Value.asValue(new ResourceObjectProxy(codeExecutor, resource));
        } else {
            throw new IllegalArgumentException("Too many arguments");
        }
    }
}
