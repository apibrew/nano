package io.apibrew.nano.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class CodeExecutor {
    private final Client client;
    private final Repository<Extension> extensionRepository;
    private final InstanceDataStore dataStore;
    private boolean isRunning = true;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final GraalVMCodeExecutor graalVMCodeExecutor;

    public CodeExecutor(Client client, NanoInstance instance, InstanceDataStore dataStore) {
        this.client = client;
        extensionRepository = client.repository(Extension.class);
        objectMapper.registerModule(new JavaTimeModule());
        this.dataStore = dataStore;
        this.graalVMCodeExecutor = new GraalVMCodeExecutor(dataStore, client, instance);
    }

    public void init() {
    }

    public void stop() {
        isRunning = false;
        executor.shutdown();
        graalVMCodeExecutor.stop();
    }

    public void unRegisterCode(Code code) {
        log.info("Unregistering code: " + code.getName());
        graalVMCodeExecutor.unRegisterCode(code);
    }

    public void registerCode(Code code) {
        log.info("Registering code: " + code.getName());
        graalVMCodeExecutor.registerCode(code);
    }

}
