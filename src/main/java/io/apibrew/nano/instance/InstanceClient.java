package io.apibrew.nano.instance;

import io.apibrew.client.Client;
import io.apibrew.client.Config;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.*;
import io.apibrew.nano.model.NanoInstance;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class InstanceClient {

    private static final String FAAS_HANDLER = "nano-handler";
    private final Client client;
    private final Repository<Extension> extensionRepository;
    private final Repository<Function> functionRepository;
    private final Repository<FunctionExecutionEngine> functionExecutionEngineRepository;
    private final Repository<FunctionTrigger> functionTriggerRepository;
    private final Repository<ResourceRule> resourceRuleRepository;

    private final InstanceDataStore dataStore;
    private final String PSEUDO_EXTENSION_CHAN = "nano-pseudo-extension-chan";
    private final FunctionExecutor functionExecutor;
    private boolean isRunning;

    public InstanceClient(NanoInstance instance) {
        Config.Server serverConfig = prepareServerConfig(instance);

        client = Client.newClientByServerConfig(serverConfig);
        extensionRepository = client.repository(Extension.class);
        functionRepository = client.repository(Function.class);
        functionExecutionEngineRepository = client.repository(FunctionExecutionEngine.class);
        functionTriggerRepository = client.repository(FunctionTrigger.class);
        resourceRuleRepository = client.repository(ResourceRule.class);
        dataStore = new InstanceDataStore(client, instance);
        functionExecutor = new FunctionExecutor(client, instance);
    }

    private static Config.Server prepareServerConfig(NanoInstance instance) {
        Config.Server serverConfig = new Config.Server();
        serverConfig.setHost(instance.getServerConfig().getHost());
        serverConfig.setInsecure(instance.getServerConfig().getInsecure());
        Config.Authentication authentication = new Config.Authentication();
        authentication.setUsername(instance.getServerConfig().getAuthentication().getUsername());
        authentication.setPassword(instance.getServerConfig().getAuthentication().getPassword());
        authentication.setToken(instance.getServerConfig().getAuthentication().getToken());
        serverConfig.setAuthentication(authentication);
        return serverConfig;
    }

    public void init() {
        log.info("Initializing instance client");

        dataStore.setFunctionRegisterHandler(this::registerFunction);
        dataStore.setFunctionUnRegisterHandler(this::unRegisterFunction);

        registerExtensions();
        registerFunctionExecutionEngines();
        dataStore.init();
        functionExecutor.init();

        functionExecutor.setEngines(dataStore.getEngines());
    }

    public void stop() {
        isRunning = false;
        functionExecutor.stop();
        dataStore.stop();
    }

    private void unRegisterFunction(Function function) {
        functionExecutor.unRegisterFunction(function);
    }

    private void registerFunction(Function function) {
        functionExecutor.registerFunction(function);
    }

    private void registerFunctionExecutionEngines() {
        log.info("Registering function execution engines");
        List<FunctionExecutionEngine> functionExecutionEngines = prepareFunctionExecutionEngines();
        log.info("Function execution engines prepared");

        for (FunctionExecutionEngine functionExecutionEngine : functionExecutionEngines) {
            log.info("Applying function execution engine: " + functionExecutionEngine.getName());
            FunctionExecutionEngine appliedFunctionExecutionEngine = functionExecutionEngineRepository.apply(functionExecutionEngine);
            log.info("Function execution engine applied: " + appliedFunctionExecutionEngine.getName());
        }
    }

    private List<FunctionExecutionEngine> prepareFunctionExecutionEngines() {
        List<FunctionExecutionEngine> list = new ArrayList<>();

        list.add(new FunctionExecutionEngine().withName("nano-nodejs-engine"));
        list.add(new FunctionExecutionEngine().withName("nano-python-engine"));

        return list;
    }

    public void registerExtensions() {
        log.info("Registering extensions");
        List<Extension> extensions = prepareExtensions();
        log.info("Extensions prepared");

        for (Extension extension : extensions) {
            log.info("Applying extension: " + extension.getName());
            Extension appliedExtension = extensionRepository.apply(extension);
            log.info("Extension applied: " + appliedExtension.getName());
        }
    }

    public List<Extension> prepareExtensions() {
        List<Extension> list = new ArrayList<>();

        for (Extension.Action action : Extension.Action.values()) {
            list.add(preparePseudoExtension(action, 90, true, action.name().toLowerCase() + "-before"));
            list.add(preparePseudoExtension(action, 250, true, action.name().toLowerCase() + "-after"));
            list.add(preparePseudoExtension(action, 250, false, action.name().toLowerCase() + "-async"));
        }

        return list;
    }

    private Extension preparePseudoExtension(Extension.Action action, int order, boolean sync, String nameSuffix) {
        Extension.EventSelector selector;
        Extension resourceBeforeExtension = new Extension();
        resourceBeforeExtension.setName(FAAS_HANDLER + "-" + nameSuffix);
        resourceBeforeExtension.setDescription("Function extension for nano");

        selector = new Extension.EventSelector();

        // handle only correct action
        selector.setActions(List.of(action));

        // handle when annotation is matched
        selector.setAnnotations(Map.of(FAAS_HANDLER, nameSuffix));

        resourceBeforeExtension.setSelector(selector);
        resourceBeforeExtension.setOrder(order);
        resourceBeforeExtension.setSync(sync);
        resourceBeforeExtension.setResponds(true);
        resourceBeforeExtension.setFinalizes(false);
        resourceBeforeExtension.setCall(new Extension.ExternalCall().withChannelCall(new Extension.ChannelCall().withChannelKey(PSEUDO_EXTENSION_CHAN)));
        return resourceBeforeExtension;
    }
}

