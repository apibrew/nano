package io.apibrew.nano.instance.proxy;

import io.apibrew.client.GenericRecord;
import io.apibrew.client.model.Resource;
import io.apibrew.common.ext.Condition;
import io.apibrew.common.ext.Handler;
import io.apibrew.nano.instance.CodeExecutor;
import io.apibrew.nano.model.Code;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.apibrew.common.ext.Condition.*;

@Log4j2
public class ResourceObjectProxy extends AbstractProxyObject {
    private final CodeExecutor codeExecutor;
    private final Resource resource;
    private final Handler<GenericRecord> handler;

    public ResourceObjectProxy(CodeExecutor codeExecutor, Resource resource) {
        this.codeExecutor = codeExecutor;
        this.resource = resource;
        this.handler = codeExecutor.locateHandler(resource);
    }

    public Map<String, Consumer<Value>> operatorMethods() {
        Map<String, Consumer<Value>> result = new HashMap<>();

        result.put("beforeCreate", operator(beforeCreate()));

        return result;
    }

    public Map<String, Function<Value[], Value>> methods() {
        Map<String, Function<Value[], Value>> list = new HashMap<>();

        for (Map.Entry<String, Consumer<Value>> entry : operatorMethods().entrySet()) {
            list.put(entry.getKey(), (Value[] values) -> {
                if (values.length != 1) {
                    throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
                }
                entry.getValue().accept(values[0]);
                return null;
            });
        }

        return list;
    }

    private Consumer<Value> operator(Condition<GenericRecord> condition) {
        return (Value executable) -> handleOperator(executable, handler.when(condition));
    }

    private void handleOperator(Value executable, Handler<GenericRecord> updatedHandler) {
        codeExecutor.ensureInsideCodeInitializer();
        try {
            if (!executable.canExecute()) {
                throw new IllegalArgumentException("given argument is not executable: " + executable);
            }
            Code code = codeExecutor.getCurrentInitializingCode();

            String operatorId = updatedHandler.operate((event, item) -> {
                codeExecutor.executeInContextThread(() -> {
                    log.debug("[" + code.getName() + "]Executing beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
                    executable.execute(Value.asValue(new GenericRecordProxy(resource, item)));
                    log.debug("[" + code.getName() + "]Executed beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
                });
                return item;
            });

            codeExecutor.registerOperatorId(operatorId);

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
