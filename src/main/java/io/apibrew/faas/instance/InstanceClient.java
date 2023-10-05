package io.apibrew.faas.instance;

import io.apibrew.client.Client;
import io.apibrew.client.Config;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.*;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class InstanceClient {

    private static final String FAAS_HANDLER = "faas-handler";
    private final Client client;
    private final Repository<Extension> extensionRepository;
    private final Repository<Function> functionRepository;
    private final Repository<FunctionExecutionEngine> functionExecutionEngineRepository;
    private final Repository<FunctionTrigger> functionTriggerRepository;
    private final Repository<ResourceRule> resourceRuleRepository;

    private final InstanceDataStore dataStore;
    private final String FUNCTION_EXECUTION_CHAN = "faas-function-execution-chan";
    private final String PSEUDO_EXTENSION_CHAN = "faas-pseudo-extension-chan";

    public InstanceClient(Config.Server serverConfig) {
        client = Client.newClientByServerConfig(serverConfig);
        extensionRepository = client.repository(Extension.class);
        functionRepository = client.repository(Function.class);
        functionExecutionEngineRepository = client.repository(FunctionExecutionEngine.class);
        functionTriggerRepository = client.repository(FunctionTrigger.class);
        resourceRuleRepository = client.repository(ResourceRule.class);
        dataStore = new InstanceDataStore(client);
    }

    public void init() {
        log.info("Initializing instance client");
        registerExtensions();
        registerFunctionExecutionEngines();
        registerPoll();
        dataStore.init();
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

        list.add(new FunctionExecutionEngine().withName("faas-nodejs-engine"));
        list.add(new FunctionExecutionEngine().withName("faas-python-engine"));
//        list.add(new FunctionExecutionEngine().withName("faas-java-engine"));

        return list;
    }

    private void registerPoll() {

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

        Extension functionExecutionExtension = new Extension();
        functionExecutionExtension.setName("faas-function-execution");
        functionExecutionExtension.setDescription("Function extension for FaaS");

        Extension.EventSelector selector = new Extension.EventSelector();
        selector.setActions(List.of(Extension.Action.CREATE));
        selector.setNamespaces(List.of("logic"));
        selector.setResources(List.of(Function.entityInfo.getResource()));

        functionExecutionExtension.setSelector(selector);
        functionExecutionExtension.setOrder(1);
        functionExecutionExtension.setSync(true);
        functionExecutionExtension.setResponds(true);
        functionExecutionExtension.setFinalizes(true);
        functionExecutionExtension.setCall(new Extension.ExternalCall().withChannelCall(new Extension.ChannelCall().withChannelKey(FUNCTION_EXECUTION_CHAN)));

        for (Extension.Action action : Extension.Action.values()) {
            list.add(preparePseudoExtension(action, 90, true, action.name().toLowerCase() + "-before"));
            list.add(preparePseudoExtension(action, 250, true, action.name().toLowerCase() + "-after"));
            list.add(preparePseudoExtension(action, 250, false, action.name().toLowerCase() + "-async"));
        }


        list.add(functionExecutionExtension);

        return list;
    }

    private Extension preparePseudoExtension(Extension.Action action, int order, boolean sync, String nameSuffix) {
        Extension.EventSelector selector;
        Extension resourceBeforeExtension = new Extension();
        resourceBeforeExtension.setName(FAAS_HANDLER + "-" + nameSuffix);
        resourceBeforeExtension.setDescription("Function extension for FaaS");

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

