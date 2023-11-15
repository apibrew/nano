package io.apibrew.nano.instance;

import io.apibrew.client.Client;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.GenericRecord;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Resource;
import io.apibrew.client.ext.Handler;
import io.apibrew.client.ext.impl.PollerExtensionService;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Log4j2
public class GraalVmNanoEngine {
    @Getter
    private final InstanceDataStore dataStore;

    private final Map<String, Handler<GenericRecord>> handlerMap = new HashMap<>();
    private final PollerExtensionService ext;

    private final List<CodeExecutor> codeExecutors = new ArrayList<>();
    private final Client client;

    public GraalVmNanoEngine(InstanceDataStore dataStore, Client client, NanoInstance instance) {
        this.dataStore = dataStore;

        log.info("GraalVMCodeExecutor initialized");

        this.ext = new PollerExtensionService("nano-code-ext-chan", client, "nano-code-ext-chan");
        this.client = client;

        for (int i = 0; i < 1; i++) {
            this.codeExecutors.add(new CodeExecutor(client, ext, this));
        }
    }

    public void init() {
        log.info("Initializing GraalVMCodeExecutor");

        log.info("Testing nodejs");
        execute(new Code().withLanguage(Code.Language.JAVASCRIPT)
                .withName("test-nodejs")
                .withContent(Base64.getEncoder().encodeToString("var a = 'test'".getBytes())));

        log.info("Testing python");
        execute(new Code().withLanguage(Code.Language.PYTHON)
                .withName("test-python")
                .withContent(Base64.getEncoder().encodeToString("def run(input):\n    return input".getBytes())));

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                this.ext.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void registerCode(Code code) {
        log.info("Registering code: " + code.getName());
        codeExecutors.forEach(item -> item.register(code));
    }

    public void unRegisterCode(Code code) {
        log.info("Unregistering code: " + code.getName());
        codeExecutors.forEach(item -> item.unRegister(code));
    }

    public void execute(Code code) {
        log.info("Executing code: " + code.getName());
        codeExecutors.forEach(item -> item.execute(code));
    }

    public void stop() {
        log.info("Stopping GraalVMCodeExecutor");
        codeExecutors.forEach(CodeExecutor::stop);
    }

    public Handler<GenericRecord> locateHandler(Resource resource) {
        EntityInfo<GenericRecord> entityInfo = EntityInfo.fromResource(resource);

        handlerMap.computeIfAbsent(entityInfo.toString(), name -> new LoadBalancingHandler(prepareHandler(entityInfo)));

        return handlerMap.get(entityInfo.toString());
    }

    public Repository<GenericRecord> locateRepository(Resource resource) {
        return client.repository(EntityInfo.fromResource(resource));
    }

    private Handler<GenericRecord> prepareHandler(EntityInfo<GenericRecord> entityInfo) {
        return ext.handler(entityInfo);
    }
}
