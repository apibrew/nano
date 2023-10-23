package io.apibrew.nano.instance.proxy;

import io.apibrew.client.GenericRecord;
import io.apibrew.client.model.Resource;
import io.apibrew.common.ext.Condition;
import io.apibrew.common.ext.Handler;
import io.apibrew.nano.instance.GraalVMCodeExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

@RequiredArgsConstructor
@Log4j2
public class ResourceObjectProxy extends AbstractProxyObject {
    private final GraalVMCodeExecutor graalVMCodeExecutor;
    private final Resource resource;

    public void beforeCreate(Value executable) {
        graalVMCodeExecutor.ensureInsideCodeInitializer();
        try {
            if (!executable.canExecute()) {
                throw new IllegalArgumentException("given argument is not executable: " + executable);
            }

            Handler<GenericRecord> handler = graalVMCodeExecutor.locateHandler(resource);

            String operatorId = handler.when(Condition.beforeCreate()).operate((event, item) -> {
                executable.execute(Value.asValue(new GenericRecordProxy(resource, item)));
                return item;
            });

            graalVMCodeExecutor.registerOperatorId(operatorId);

        } catch (RuntimeException e) {
            log.error("Error executing beforeCreate", e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return resource.getNamespace().getName() + "/" + resource.getName();
    }
}
