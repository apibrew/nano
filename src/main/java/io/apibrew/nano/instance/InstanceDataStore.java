package io.apibrew.nano.instance;

import io.apibrew.client.ApiException;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.Watcher;
import io.apibrew.client.ext.impl.PollerExtensionService;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import io.apibrew.client.ext.Condition;
import io.apibrew.client.ext.Handler;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Log4j2
public class InstanceDataStore {

    private final Client client;
    private final Repository<Extension> extensionRepository;
    private final Repository<Code> codeRepository;
    private final Repository<Resource> resourceRepository;

    private final Lock lock = new ReentrantLock();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String channelKey = "nano-sync-chan";
    private final List<Code> codes = new ArrayList<>();
    private final PollerExtensionService ext;
    private final Handler<Code> codeHandler;

    @Setter
    private Consumer<Code> codeUnRegisterHandler;
    @Setter
    private Consumer<Code> codeRegisterHandler;
    private final List<Resource> resources = new ArrayList<>();
    private final Map<String, Resource> resourceNameSlashNamespaceMap = new HashMap<>();
    private Watcher<Resource> resourceWatcher;

    public InstanceDataStore(Client client, NanoInstance instance) {
        this.client = client;
        this.extensionRepository = client.repository(Extension.class);
        this.codeRepository = client.repository(Code.class);
        this.resourceRepository = client.repository(Resource.class);
        this.ext = new PollerExtensionService(channelKey, client, channelKey);
        this.codeHandler = this.ext.handler(Code.class);
    }


    public void init() {
        log.info("Initializing instance data-store");
        loadResources();


        // resource watcher
        this.resourceWatcher = resourceRepository.watch((event) -> {
            log.info("Resource event: " + event);
            loadResources();
        });

        resourceWatcher.start();

        loadCodes();

        codeHandler.when(Condition.beforeCreate()).operate((event, code) -> {
            registerCode(code);

            return code;
        });

        codeHandler.when(Condition.beforeUpdate()).operate((event, code) -> {
            unRegisterCode(code);
            registerCode(code);

            return code;
        });

        codeHandler.when(Condition.beforeDelete()).operate((event, code) -> {
            unRegisterCode(code);

            return code;
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                this.ext.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SneakyThrows
    public void stop() {
        executorService.shutdown();
        this.ext.close();
        try {
            this.resourceWatcher.close();
        } catch (Exception e) {
            log.error("Unable to close resource watcher", e);
        }
    }

    private void loadResources() {
        log.info("Loading resources");
        lock.lock();
        try {
            List<Resource> resources = resourceRepository.list().getContent();

            this.resources.clear();
            this.resourceNameSlashNamespaceMap.clear();
            this.resources.addAll(resources);

            for (Resource resource : resources) {
                this.resourceNameSlashNamespaceMap.put(resource.getNamespace().getName() + "/" + resource.getName(), resource);
            }

            log.info("Resources loaded: " + this.resources.size());
        } finally {
            lock.unlock();
        }
    }

    private void loadCodes() {
        log.info("Loading codes");

        for (Code code : codeRepository.list().getContent()) {
            try {
                log.info("Code added: " + code);
                this.registerCode(code);
            } catch (Exception e) {
                log.error("Error registering code: " + code, e);
            }
        }
    }


    private void unRegisterCode(Code code) {
        codeUnRegisterHandler.accept(code);
    }

    private void registerCode(Code code) {
        codeRegisterHandler.accept(code);
    }

    public Resource getResourceByName(String namespace, String resource) {
        if (resourceNameSlashNamespaceMap.containsKey(namespace + "/" + resource)) {
            return resourceNameSlashNamespaceMap.get(namespace + "/" + resource);
        }

        throw new IllegalArgumentException("Resource not found: " + namespace + "/" + resource);
    }
}
