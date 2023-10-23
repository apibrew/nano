package io.apibrew.nano.instance;

import io.apibrew.client.Client;
import io.apibrew.client.Config;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.controller.InstanceClient;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class NanoInstanceClient implements InstanceClient {

    private static final String FAAS_HANDLER = "nano-handler";
    private final Client client;
    private final Repository<Extension> extensionRepository;

    private final InstanceDataStore dataStore;
    private final String PSEUDO_EXTENSION_CHAN = "nano-pseudo-extension-chan";
    private final CodeExecutor codeExecutor;
    private boolean isRunning;

    public NanoInstanceClient(Client client, NanoInstance instance) {
        this.client = client;

        extensionRepository = client.repository(Extension.class);
        dataStore = new InstanceDataStore(client, instance);
        codeExecutor = new CodeExecutor(client, instance, dataStore);
    }

    public void init() {
        log.info("Initializing instance client");

        dataStore.setCodeRegisterHandler(this::registerCode);
        dataStore.setCodeUnRegisterHandler(this::unRegisterCode);

        registerExtensions();
        dataStore.init();
        codeExecutor.init();
    }

    public void stop() {
        isRunning = false;
        codeExecutor.stop();
        dataStore.stop();
    }

    private void unRegisterCode(Code code) {
        codeExecutor.unRegisterCode(code);
    }

    private void registerCode(Code code) {
        codeExecutor.registerCode(code);
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
        resourceBeforeExtension.setDescription("Code extension for nano");

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

