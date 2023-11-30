package io.apibrew.nano.instance;

import io.apibrew.client.Client;
import io.apibrew.client.controller.InstanceClient;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NanoInstanceClient implements InstanceClient {

    private final InstanceDataStore dataStore;
    private final GraalVmNanoEngine graalVmNanoEngine;

    public NanoInstanceClient(Client client, NanoInstance instance) {
        dataStore = new InstanceDataStore(client, instance);
        this.graalVmNanoEngine = new GraalVmNanoEngine(dataStore, client, instance);
    }

    public void init() {
        log.info("Initializing instance client");

        dataStore.setCodeRegisterHandler(this::registerCode);
        dataStore.setCodeUnRegisterHandler(this::unRegisterCode);

        graalVmNanoEngine.init();
        dataStore.init();
    }

    public void stop() {
        graalVmNanoEngine.stop();
        dataStore.stop();
    }

    private void unRegisterCode(Code code) {
        graalVmNanoEngine.unRegisterCode(code);
    }

    private void registerCode(Code code) {
        graalVmNanoEngine.registerCode(code);
    }
}

