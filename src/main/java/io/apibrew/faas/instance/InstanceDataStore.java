package io.apibrew.faas.instance;

import io.apibrew.client.ApiException;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.impl.ChannelEventPoller;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.*;
import io.apibrew.faas.helper.ListDiffer;
import io.apibrew.faas.model.FaasInstance;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

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
    private final List<Lambda> lambdas = new ArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String channelKey = "faas-sync-chan";
    private final List<FunctionExecutionEngine> functionEngines = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ChannelEventPoller poller;

    @Setter
    private Consumer<Function> functionRegisterHandler;

    @Setter
    private Consumer<Function> functionUnRegisterHandler;

    public InstanceDataStore(Client client, FaasInstance instance) {
        this.client = client;
        this.extensionRepository = client.repository(Extension.class);
        this.functionRepository = client.repository(Function.class);
        this.functionExecutionEngineRepository = client.repository(FunctionExecutionEngine.class);
        this.functionTriggerRepository = client.repository(FunctionTrigger.class);
        this.resourceRuleRepository = client.repository(ResourceRule.class);
        this.lambdaRepository = client.repository(Lambda.class);
        this.poller = ChannelEventPoller.builder()
                .client(client)
                .channelKey(channelKey)
                .consumer(this::handleEvent)
                .threadName("InstanceDataStore poller[" + instance.getName() + "]")
                .build();
    }

    private void handleEvent(Extension.Event event) {
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
    }

    public void init() {
        log.info("Initializing instance data-store");
        registerExtensions();
        loadAll();

        poller.start();
    }

    public void stop() {
        executorService.shutdown();
        poller.close();
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

    private void processEvent(Extension.Event event) {
        log.info("Handling event: " + event);

        if (!event.getResource().getNamespace().getName().equals(Function.NAMESPACE)) {
            log.warn("Ignoring event for namespace: " + event.getResource().getNamespace().getName());
            return;
        }

        switch (event.getResource().getName()) {
            case Function.RESOURCE:
                loadFunctions();
                break;
            case FunctionExecutionEngine.RESOURCE:
                loadFunctionExecutionEngines();
                break;
            case FunctionTrigger.RESOURCE:
                loadFunctionTriggers();
                break;
            case ResourceRule.RESOURCE:
                loadResourceRules();
                break;
            case Lambda.RESOURCE:
                loadLambdas();
                break;
        }
    }

    public void loadFunctions() {
        log.info("Loading functions");
        lock.lock();
        try {
            List<Function> functions = functionRepository.list().getContent();

            ListDiffer.DiffResult<Function> diffResult = ListDiffer.diff(this.functions, functions, (f1, f2) -> Objects.equals(f1.getId(), f2.getId()), Function::equals);

            for (Function function : diffResult.added) {
                log.info("Function added: " + function);
                this.registerFunction(function);
                this.functions.add(function);
            }

            for (Function function : diffResult.deleted) {
                log.info("Function deleted: " + function);
                this.unRegisterFunction(function);
                this.functions.remove(function);
            }

            for (Function function : diffResult.updated) {
                log.info("Function updated: " + function);
                this.unRegisterFunction(function);
                this.registerFunction(function);
                this.functions.remove(function);
                this.functions.add(function);
            }

            log.info("Functions loaded: " + this.functions.size());
        } finally {
            lock.unlock();
        }
    }

    private void unRegisterFunction(Function function) {
        functionUnRegisterHandler.accept(function);
    }

    private void registerFunction(Function function) {
        functionRegisterHandler.accept(function);
    }

    private void loadAll() {
        loadFunctions();
        loadFunctionExecutionEngines();
        loadFunctionTriggers();
        loadResourceRules();

        functions.forEach(this::registerFunction);
    }

    private void loadResourceRules() {
        log.info("Loading resource rules");
        this.resourceRules.clear();
        this.resourceRules.addAll(resourceRuleRepository.list().getContent());

        log.info("Resource rules loaded: " + this.resourceRules.size());
    }

    private void loadLambdas() {
        log.info("Loading lambdas");
        this.lambdas.clear();
        this.lambdas.addAll(lambdaRepository.list().getContent());

        log.info("Lambdas loaded: " + this.lambdas.size());

    }

    private void loadFunctionTriggers() {
        log.info("Loading function triggers");
        this.functionTriggers.clear();
        this.functionTriggers.addAll(functionTriggerRepository.list().getContent());

        log.info("Function triggers loaded: " + this.functionTriggers.size());
    }

    private void loadFunctionExecutionEngines() {
        log.info("Loading function execution engines");
        this.functionEngines.clear();
        this.functionEngines.addAll(functionExecutionEngineRepository.list().getContent());

        log.info("Function execution engines loaded: " + this.functionEngines.size());
    }

    public List<FunctionExecutionEngine> getEngines() {
        return functionEngines;
    }
}
