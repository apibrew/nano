package io.apibrew.nano.instance;

import io.apibrew.client.ApiException;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.Watcher;
import io.apibrew.client.impl.ChannelEventPoller;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import io.apibrew.nano.helper.ListDiffer;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.Setter;
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
    private final ChannelEventPoller poller;
    private final List<Code> codes = new ArrayList<>();

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
        this.poller = ChannelEventPoller.builder()
                .client(client)
                .channelKey(channelKey)
                .consumer(this::handleEvent)
                .threadName("InstanceDataStore poller[" + instance.getName() + "]")
                .build();
    }

    private void handleEvent(Extension.Event event) {
        try {
            processEvent(event);
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
        loadCodes();
        loadResources();

        poller.start();

        // resource watcher
        this.resourceWatcher = resourceRepository.watch((event) -> {
            log.info("Resource event: " + event);
            loadResources();
        });

        resourceWatcher.start();
    }

    public void stop() {
        executorService.shutdown();
        poller.close();
        try {
            this.resourceWatcher.close();
        } catch (Exception e) {
            log.error("Unable to close resource watcher", e);
        }
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
        syncDataExtension.setName("nano-sync");
        syncDataExtension.setDescription("Nano code extension");

        Extension.EventSelector selector = new Extension.EventSelector();
        selector.setActions(List.of(Extension.Action.CREATE, Extension.Action.UPDATE, Extension.Action.DELETE));
        selector.setNamespaces(List.of("nano"));
        selector.setResources(List.of(
                Code.entityInfo.getResource()
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

        if (!event.getResource().getNamespace().getName().equals(Code.NAMESPACE)) {
            log.warn("Ignoring event for namespace: " + event.getResource().getNamespace().getName());
            return;
        }

        switch (event.getResource().getName()) {
            case Code.RESOURCE:
                loadCodes();
                break;
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

            log.info("Resources loaded: " + this.codes.size());
        } finally {
            lock.unlock();
        }
    }

    private void loadCodes() {
        log.info("Loading codes");
        lock.lock();
        try {
            List<Code> codes = codeRepository.list().getContent();

            ListDiffer.DiffResult<Code> diffResult = ListDiffer.diff(this.codes, codes, (f1, f2) -> Objects.equals(f1.getId(), f2.getId()), Code::equals);

            for (Code code : diffResult.added) {
                log.info("Code added: " + code);
                this.registerCode(code);
            }

            for (Code code : diffResult.deleted) {
                log.info("Code deleted: " + code);
                this.unRegisterCode(code);
            }

            for (Code code : diffResult.updated) {
                log.info("Code updated: " + code);
                this.unRegisterCode(code);
                this.registerCode(code);
            }

            this.codes.clear();
            this.codes.addAll(codes);

            log.info("Codes loaded: " + this.codes.size());
        } finally {
            lock.unlock();
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
