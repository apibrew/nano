package io.apibrew.faas.instance;

import io.apibrew.client.ApiException;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.*;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class InstanceDataStore {

    private final Client client;
    private final Repository<Extension> extensionRepository;
    private final Repository<Function> functionRepository;
    private final Repository<FunctionExecutionEngine> functionExecutionEngineRepository;
    private final Repository<FunctionTrigger> functionTriggerRepository;
    private final Repository<ResourceRule> resourceRuleRepository;
    private final Repository<Lambda> lambdaRepository;

    private final Lock lock = new ReentrantLock();
    private final List<Function> functions = new ArrayList<>();
    private final List<FunctionExecutionEngine> functionExecutionEngines = new ArrayList<>();
    private final List<FunctionTrigger> functionTriggers = new ArrayList<>();
    private final List<ResourceRule> resourceRules = new ArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String channelKey = "faas-sync-chan";
    private final List<FunctionExecutionEngine> functionEngines = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public InstanceDataStore(Client client) {
        this.client = client;
        this.extensionRepository = client.repository(Extension.class);
        this.functionRepository = client.repository(Function.class);
        this.functionExecutionEngineRepository = client.repository(FunctionExecutionEngine.class);
        this.functionTriggerRepository = client.repository(FunctionTrigger.class);
        this.resourceRuleRepository = client.repository(ResourceRule.class);
        this.lambdaRepository = client.repository(Lambda.class);
    }

    private boolean isRunning = true;

    public void init() {
        log.info("Initializing instance data-store");
        registerExtensions();
        registerPoll();
        loadAll();
    }

    public void stop() {
        isRunning = false;
    }

    public void registerExtensions() {
        log.info("Registering extensions for data-store");
        List<Extension> extensions = prepareExtensions();
        log.info("Extensions prepared for data-store");

        for (Extension extension : extensions) {
            log.info("Applying extension: " + extension.getName());
            Extension appliedExtension = extensionRepository.apply(extension);
            log.info("Extension applied: " + appliedExtension.getName());
        }

        log.info("Extensions registered for data-store");
    }

    private List<Extension> prepareExtensions() {
        List<Extension> list = new ArrayList<>();

        Extension syncDataExtension = new Extension();
        syncDataExtension.setName("faas-sync");
        syncDataExtension.setDescription("Function extension for FaaS");

        Extension.EventSelector selector = new Extension.EventSelector();
        selector.setActions(List.of(Extension.Action.CREATE, Extension.Action.UPDATE, Extension.Action.DELETE));
        selector.setNamespaces(List.of("logic"));
        selector.setResources(List.of(
                Function.entityInfo.getResource(),
                FunctionExecutionEngine.entityInfo.getResource(),
                FunctionTrigger.entityInfo.getResource(),
                ResourceRule.entityInfo.getResource(),
                Lambda.entityInfo.getResource()
        ));

        syncDataExtension.setSelector(selector);
        syncDataExtension.setOrder(300);
        syncDataExtension.setSync(false);
        syncDataExtension.setCall(new Extension.ExternalCall().withChannelCall(new Extension.ChannelCall().withChannelKey(channelKey)));

        list.add(syncDataExtension);
        return list;
    }

    private void registerPoll() {
        executor.submit(() -> {
            while (isRunning) {
                try {
                    doLocalPoll();
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doLocalPoll() {
        this.client.pollEvents(channelKey, (event) -> {
            this.executorService.submit(() -> {
                try {
                    this.client.writeEvent(channelKey, event);
                    executor.execute(() -> handleEvent(event));
                } catch (ApiException var3) {
                    log.error("Unable to process event[ApiException]", var3);
                    event.setError(var3.getError());
                } catch (Exception var4) {
                    log.error("Unable to process event", var4);
                    event.setError((new Extension.Error()).withMessage(var4.getMessage()));
                }

            });
            return true;
        });
    }

    private void handleEvent(Extension.Event event) {
        log.info("Handling event: " + event);
    }

    public void loadFunctions() {
        log.info("Loading functions");
        lock.lock();
        try {
            List<Function> functions = functionRepository.list().getContent();
            // ... remaining logic translated from Go to Java
        } finally {
            lock.unlock();
        }
    }

    private void loadAll() {
        loadFunctions();
        loadFunctionExecutionEngines();
        loadFunctionTriggers();
        loadResourceRules();
    }

    private void loadResourceRules() {
    }

    private void loadFunctionTriggers() {
    }

    private void loadFunctionExecutionEngines() {
        log.info("Loading function execution engines");
        this.functionEngines.clear();
        this.functionEngines.addAll(functionExecutionEngineRepository.list().getContent());

        log.info("Function execution engines loaded: " + this.functionEngines.size());
    }
}
