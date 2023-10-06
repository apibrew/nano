package io.apibrew.faas.instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.logic.Function;
import io.apibrew.client.model.logic.FunctionExecution;
import io.apibrew.client.model.logic.FunctionExecutionEngine;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.IOAccess;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Log4j2
public class GraalVMFunctionExecutor {
    Map<String, Function> functionMap = new HashMap<>();
    private Map<UUID, String> engineNameIdMap = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService virtualThreadsExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    public void unRegisterFunction(Function function) {
        functionMap.remove(function.getPackage() + "-" + function.getName());
    }

    public void registerFunction(Function function) {
        functionMap.put(function.getPackage() + "-" + function.getName(), function);
    }

    public void execute(FunctionExecution functionExecution) {
        Function function = functionMap.get(functionExecution.getFunction().getPackage() + "-" + functionExecution.getFunction().getName());

        if (function == null) {
            functionExecution.setError(new Extension.Error().withCode(Extension.Code.RECORD_VALIDATION_ERROR).withMessage("Function not found"));
            return;
        }

        execute(function, functionExecution);
    }

    private void execute(Function function, FunctionExecution functionExecution) {
        String engine = engineNameIdMap.get(function.getEngine().getId());

        System.out.println("engine: " + engine);

        switch (engine) {
            case "faas-nodejs-engine":
                executeWithNodeJs(function, functionExecution);
                break;
            case "faas-python-engine":
                executeWithPython(function, functionExecution);
                break;
        }

        if (functionExecution.getError() != null) {
           functionExecution.setStatus(FunctionExecution.Status.ERROR);
        } else {
            functionExecution.setStatus(FunctionExecution.Status.SUCCESS);
        }
    }

    private void executeWithPython(Function function, FunctionExecution functionExecution) {
        try (Context context = Context.newBuilder("python")
                .allowExperimentalOptions(true)
                .allowHostAccess(HostAccess.newBuilder()
                        .allowAccessAnnotatedBy(AllowGuestAccess.class)
                        .build())
                .build()) {
            handleTimeout(context);

            var bindings = context.getBindings("python");
            log.debug("Script: " + function.getScript());
            context.eval("python", function.getScript());

            Value runFn = bindings.getMember("run");

            if (runFn != null && runFn.canExecute()) {
                Value result = runFn.execute(functionExecution.getInput());
                functionExecution.setOutput(mapper.convertValue((result.as(Object.class)), Object.class));
            } else {
                functionExecution.setOutput("ok");
            }
            functionExecution.getFunction();
        } catch (Exception e) {
            functionExecution.setError(new Extension.Error().withCode(Extension.Code.INTERNAL_ERROR).withMessage(e.getMessage()));
        }
    }

    private void executeWithNodeJs(Function function, FunctionExecution functionExecution) {
        try (Context context = Context.newBuilder("js")
                .allowExperimentalOptions(true)
                .resourceLimits(ResourceLimits.newBuilder()
                        .build())
                .allowHostAccess(HostAccess.newBuilder()
                        .allowAccessAnnotatedBy(AllowGuestAccess.class)
                        .build())
                .build()) {
            handleTimeout(context);

            var bindings = context.getBindings("js");
            log.debug("Script: " + function.getScript());
            context.eval("js", function.getScript());

            Value runFn = bindings.getMember("run");

            if (runFn != null && runFn.canExecute()) {
                Value result = runFn.execute(functionExecution.getInput());
                functionExecution.setOutput(mapper.convertValue((result.as(Object.class)), Object.class));
            } else {
                functionExecution.setOutput("ok");
            }
            functionExecution.getFunction();
        } catch (Exception e) {
            functionExecution.setError(new Extension.Error().withCode(Extension.Code.INTERNAL_ERROR).withMessage(e.getMessage()));
        }
    }

    private void handleTimeout(Context context) {
        virtualThreadsExecutorService.submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                log.error(e);
            }

            context.close(true);
        });
    }

    public void setEngines(List<FunctionExecutionEngine> engines) {
        engineNameIdMap = engines.stream().collect(Collectors.toMap(FunctionExecutionEngine::getId, FunctionExecutionEngine::getName));
    }

    public void stop() {
        virtualThreadsExecutorService.shutdown();
    }
}
