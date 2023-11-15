package io.apibrew.nano.instance.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.apibrew.client.*;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Record;
import io.apibrew.client.model.Resource;
import io.apibrew.client.ext.Condition;
import io.apibrew.client.ext.Handler;
import io.apibrew.nano.instance.CodeExecutor;
import io.apibrew.nano.instance.RecordHelper;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.util.BooleanExpressionUtil;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.apibrew.client.ext.Condition.*;
import static io.apibrew.nano.instance.proxy.TransferProxy.wrapGeneric;

@Log4j2
public class ResourceObjectProxy extends AbstractProxyObject {
    private final CodeExecutor codeExecutor;
    private final Resource resource;
    private final Repository<GenericRecord> repository;
    private final Handler<GenericRecord> handler;
    private final RecordHelper recordHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResourceObjectProxy(CodeExecutor codeExecutor, Resource resource) {
        this.codeExecutor = codeExecutor;
        this.resource = resource;
        this.handler = codeExecutor.locateHandler(resource);
        this.repository = codeExecutor.locateRepository(resource);
        this.recordHelper = new RecordHelper(codeExecutor, resource, repository);

        prepareMethods();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void prepareMethods() {
        registerFunction("beforeCreate", operator(beforeCreate()));
        registerFunction("beforeDelete", operator(beforeDelete(), true));
        registerFunction("beforeUpdate", operator(beforeUpdate()));
        registerFunction("beforeGet", operator(beforeGet()));
        registerFunction("beforeList", operator(beforeList()));

        registerFunction("afterCreate", operator(afterCreate()));
        registerFunction("afterUpdate", operator(afterUpdate()));
        registerFunction("afterDelete", operator(afterDelete()));
        registerFunction("afterGet", operator(afterGet()));
        registerFunction("afterList", operator(afterList()));

        registerFunction("onCreate", operator(afterCreate()));
        registerFunction("onUpdate", operator(afterUpdate()));
        registerFunction("onDelete", operator(afterDelete()));
        registerFunction("onGet", operator(afterGet()));
        registerFunction("onList", operator(afterList()));
        registerFunction("bindCreate", bindToFn("create"));
        registerFunction("bindUpdate", bindToFn("update"));
        registerFunction("bindDelete", bindToFn("delete"));
        registerFunction("bindGet", bindToFn("get"));
        registerFunction("bindList", bindToFn("list"));

        registerFunction("preprocess", operator(and(before(), or(Condition.create(), Condition.update()))));
        registerFunction("postprocess", operator(and(after(), or(Condition.create(), Condition.update()))));
        registerFunction("check", check(and(after(), or(Condition.create(), Condition.update()))));

        registerFunction("count", countFn());
        registerFunction("load", loadFn());
        registerFunction("create", createFn());
        registerFunction("update", updateFn());
        registerFunction("save", saveFn());
        registerFunction("delete", deleteFn());
        registerFunction("findById", findByIdFn());
        registerFunction("get", findByIdFn());
    }

    private Consumer<Value[]> bindToFn(String action) {
        return (Value[] values) -> bindTo(action, values);
    }

    private void bindTo(String action, Value[] values) {
        if (values.length != 3) {
            throw new IllegalArgumentException("Expected 2 argument, got " + values.length);
        }

        Value resourceValue = values[0];
        Value mapFromValue = values[1];
        Value mapToValue = values[2];

        if (!resourceValue.isProxyObject() || !(resourceValue.asProxyObject() instanceof ResourceObjectProxy)) {
            throw new IllegalArgumentException("Expected resource as first argument");
        }

        if (!mapFromValue.canExecute()) {
            throw new IllegalArgumentException("Expected executable as second argument");
        }

        if (!mapToValue.canExecute()) {
            throw new IllegalArgumentException("Expected executable as third argument");
        }

        ResourceObjectProxy resourceObjectProxy = resourceValue.asProxyObject();

        Function<GenericRecord, GenericRecord> mapFrom = prepareRecordMapper(mapFromValue, resource);
        Function<GenericRecord, GenericRecord> mapTo = prepareRecordMapper(mapToValue, resourceObjectProxy.resource);

        switch (action) {
            case "create":
                this.handler.when(beforeCreate()).operate((event, item) -> mapFrom.apply(resourceObjectProxy.repository.create(mapTo.apply(item))));
                break;
            case "update":
                this.handler.when(beforeUpdate()).operate((event, item) -> mapFrom.apply(resourceObjectProxy.repository.update(mapTo.apply(item))));
                break;
            case "delete":
                this.handler.when(beforeDelete()).operate((event, item) -> mapFrom.apply(resourceObjectProxy.repository.delete(item.getId().toString())));
                break;
            case "get":
                this.handler.when(beforeGet()).operate((event, item) -> mapFrom.apply(resourceObjectProxy.repository.get(item.getId().toString())));
                break;
            case "list":
                this.handler.when(beforeList()).operate((event, item) -> {
                    codeExecutor.executeInContextThread(() -> {
                        List<String> resolvedReferences = new ArrayList<>();

                        if (event.getRecordSearchParams().getResolveReferences() != null) {
                            resolvedReferences = event.getRecordSearchParams().getResolveReferences();
                        }

                        Container<GenericRecord> result = resourceObjectProxy.repository.list(ListRecordParams.builder()
                                .limit(event.getRecordSearchParams().getLimit())
                                .offset(event.getRecordSearchParams().getOffset())
                                .query(event.getRecordSearchParams().getQuery())
                                .resolveReferences(resolvedReferences)
                                .build());

                        event.setTotal((long) result.getTotal());
                        event.setRecords(result.getContent().stream().map((GenericRecord genericRecord) -> {
                            Record record = new Record();
                            record.setProperties(objectMapper.convertValue(mapFrom.apply(genericRecord).getProperties(), Object.class));
                            return record;
                        }).collect(Collectors.toList()));
                    });

                    return item;
                });
                break;
        }
    }

    private Function<GenericRecord, GenericRecord> prepareRecordMapper(Value mapToValue, Resource resultResource) {
        return (record) -> {
            Value result = mapToValue.execute(Value.asValue(new GenericRecordProxy(resultResource, record)));

            if (result.isProxyObject() && result.asProxyObject() instanceof GenericRecordProxy) {
                return ((GenericRecordProxy) result.asProxyObject()).getRecord();
            } else {
                return new GenericRecordProxy(resultResource, result).getRecord();
            }
        };
    }

    private Function<Value[], Value> findByIdFn() {
        return this::findById;
    }

    private Value findById(Value[] values) {
        if (values.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
        }

        String id = values[0].asString();

        GenericRecord record = repository.get(id);

        return Value.asValue(new GenericRecordProxy(resource, record));
    }

    private Function<Value[], Value> deleteFn() {
        return this::delete;
    }

    private Value delete(Value[] values) {
        if (values.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
        }

        String id = recordHelper.identifyRecord(values[0]);

        repository.delete(id);

        return Value.asValue(null);
    }

    private Function<Value[], Value> saveFn() {
        return this::save;
    }

    private Value save(Value[] values) {
        if (values.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
        }

        GenericRecord record = recordHelper.resolveRecordFromValue(values[0]);

        if (record.getId() == null) {
            record = repository.create(record);
        } else {
            record = repository.update(record);
        }

        return Value.asValue(new GenericRecordProxy(resource, record));
    }

    private Function<Value[], Value> updateFn() {
        return this::update;
    }

    private Value update(Value[] values) {
        if (values.length == 0 || values.length > 2) {
            throw new IllegalArgumentException("Expected 1 or 2 argument, got " + values.length);
        }

        GenericRecord record;
        if (values.length == 1) {
            record = recordHelper.resolveRecordFromValue(values[0]);

            if (record.getId() == null) {
                throw new IllegalArgumentException("Record does not have an id");
            }
        } else {
            String id = recordHelper.identifyRecord(values[0]);

            record = recordHelper.resolveRecordFromValue(values[1]);

            record.getProperties().put("id", id);
        }

        record = repository.update(record);

        return Value.asValue(new GenericRecordProxy(resource, record));
    }

    private Function<Value[], Value> createFn() {
        return this::create;
    }

    private Value create(Value[] values) {
        if (values.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
        }

        GenericRecord record = recordHelper.resolveRecordFromValue(values[0]);

        record = repository.create(record);

        return Value.asValue(new GenericRecordProxy(resource, record));
    }

    private Function<Value[], Value> loadFn() {
        return this::load;
    }

    private Value load(Value[] values) {
        if (values.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + values.length);
        }
        GenericRecord record = recordHelper.resolveRecordFromValue(values[0]);

        record = recordHelper.loadRecord(record);

        return Value.asValue(new GenericRecordProxy(resource, record));
    }

    private Function<Value[], Value> countFn() {
        return this::count;
    }

    private Value count(Value[] arguments) {
        if (arguments.length == 0) {
            return Value.asValue(repository.list().getTotal());
        } else if (arguments.length == 1) {
            return Value.asValue(repository.list(BooleanExpressionUtil.booleanExpressionFrom(arguments[0])).getTotal());
        } else {
            throw new IllegalArgumentException("Too many arguments");
        }
    }

    private Consumer<Value[]> operator(Condition<GenericRecord> condition) {
        return operator(condition, false);
    }

    private Consumer<Value[]> operator(Condition<GenericRecord> condition, boolean preload) {
        return (Value[] arguments) -> handleOperator(arguments, (executable) -> executeOperator(handler.when(condition), preload, executable));
    }

    private Consumer<Value[]> check(Condition<GenericRecord> condition) {
        return (Value[] arguments) -> {
            String message;

            if (arguments.length == 2) {
                message = arguments[1].asString();
            } else {
                message = "Record validation failed";
            }

            handleOperator(arguments, (executable) -> checkOperator(handler.when(condition), executable, message));
        };
    }

    private void handleOperator(Value[] arguments, Consumer<Value> handler) {
        codeExecutor.ensureInsideCodeInitializer();

        if (arguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument, got " + arguments.length);
        }

        Value executable = arguments[0];

        try {
            if (!executable.canExecute()) {
                throw new IllegalArgumentException("given argument is not executable: " + executable);
            }
            handler.accept(executable);

        } catch (RuntimeException e) {
            log.error("Error executing beforeCreate", e);
            throw e;
        }
    }

    private void executeOperator(Handler<GenericRecord> updatedHandler, boolean preload, Value executable) {
        Code code = codeExecutor.getCurrentInitializingCode();

        String operatorId = updatedHandler.operate((event, item) -> {
            AtomicReference<GenericRecord> record = new AtomicReference<>(item);

            if (preload) {
                record.set(recordHelper.loadRecord(record.get()));
            }

            codeExecutor.executeInContextThread(() -> {
                log.debug("[" + code.getName() + "]Executing beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
                Value recordVal = Value.asValue(new GenericRecordProxy(resource, record.get()));
                Value eventVal = wrapGeneric(objectMapper.convertValue(event, Map.class));
                executable.execute(recordVal, eventVal);
                log.debug("[" + code.getName() + "]Executed beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
            });

            return record.get();
        });

        codeExecutor.registerOperatorId(operatorId);
    }

    private void checkOperator(Handler<GenericRecord> updatedHandler, Value executable, String message) {
        Code code = codeExecutor.getCurrentInitializingCode();

        String operatorId = updatedHandler.operate((event, item) -> {
            AtomicReference<GenericRecord> record = new AtomicReference<>(item);
            AtomicReference<Boolean> result = new AtomicReference<>();

            codeExecutor.executeInContextThread(() -> {
                log.debug("[" + code.getName() + "]Executing beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
                Value execResult = executable.execute(Value.asValue(new GenericRecordProxy(resource, record.get())));

                result.set(execResult.asBoolean());

                log.debug("[" + code.getName() + "]Executed beforeCreate for " + resource.getNamespace().getName() + "/" + resource.getName());
            });

            if (!result.get()) {
                throw new ApiException(Extension.Code.RECORD_VALIDATION_ERROR, message);
            }

            return record.get();
        });

        codeExecutor.registerOperatorId(operatorId);
    }

    @Override
    public String toString() {
        return resource.getNamespace().getName() + "/" + resource.getName();
    }
}
