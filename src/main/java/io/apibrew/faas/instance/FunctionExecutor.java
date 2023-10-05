package io.apibrew.faas.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.apibrew.client.ApiException;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Record;
import io.apibrew.client.model.logic.Function;
import io.apibrew.client.model.logic.FunctionExecution;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class FunctionExecutor {
    private final Client client;
    private final Repository<Extension> extensionRepository;

    private final String FUNCTION_EXECUTION_CHAN = "faas-function-execution-chan";
    private boolean isRunning = true;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FunctionExecutor(Client client) {
        this.client = client;
        extensionRepository = client.repository(Extension.class);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void init() {
        registerExtensions();

        registerPoll();
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
        this.client.pollEvents(FUNCTION_EXECUTION_CHAN, (event) -> {
            this.executor.submit(() -> {
                try {
                    Extension.Event processedEvent = handleEvent(event);
                    this.client.writeEvent(FUNCTION_EXECUTION_CHAN, processedEvent);
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

    private Extension.Event handleEvent(Extension.Event event) {
        System.out.println("Handling event: " + event);

        for (Record record : event.getRecords()) {
            FunctionExecution functionExecution = objectMapper.convertValue(record.getProperties(), FunctionExecution.class);

            handleFunctionExecution(functionExecution);

            record.setProperties(objectMapper.convertValue(functionExecution, Object.class));
        }

        return event;
    }

    private void handleFunctionExecution(FunctionExecution functionExecution) {
        System.out.println("Function execution: " + functionExecution);

        functionExecution.setError("Not implemented");
    }

    private void registerExtensions() {
        log.info("Registering extensions");
        List<Extension> extensions = prepareExtensions();
        log.info("Extensions prepared");

        for (Extension extension : extensions) {
            log.info("Applying extension: " + extension.getName());
            Extension appliedExtension = extensionRepository.apply(extension);
            log.info("Extension applied: " + appliedExtension.getName());
        }
    }

    private List<Extension> prepareExtensions() {
        Extension functionExecutionExtension = new Extension();
        functionExecutionExtension.setName("faas-function-execution");
        functionExecutionExtension.setDescription("Function extension for FaaS");

        Extension.EventSelector selector = new Extension.EventSelector();
        selector.setActions(List.of(Extension.Action.CREATE));
        selector.setNamespaces(List.of("logic"));
        selector.setResources(List.of(FunctionExecution.entityInfo.getResource()));

        functionExecutionExtension.setSelector(selector);
        functionExecutionExtension.setOrder(1);
        functionExecutionExtension.setSync(true);
        functionExecutionExtension.setResponds(true);
        functionExecutionExtension.setFinalizes(true);
        functionExecutionExtension.setCall(new Extension.ExternalCall().withChannelCall(new Extension.ChannelCall().withChannelKey(FUNCTION_EXECUTION_CHAN)));

        return List.of(functionExecutionExtension);
    }

    public void unRegisterFunction(Function function) {
        log.info("Unregistering function: " + function.getName());
    }

    public void registerFunction(Function function) {
        log.info("Registering function: " + function.getName());
    }
}
