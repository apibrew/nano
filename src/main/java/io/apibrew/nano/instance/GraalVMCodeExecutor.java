package io.apibrew.nano.instance;

import io.apibrew.client.Client;
import io.apibrew.client.EntityInfo;
import io.apibrew.client.GenericRecord;
import io.apibrew.client.impl.ChannelEventPoller;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import io.apibrew.common.ext.Handler;
import io.apibrew.common.impl.PollerExtensionService;
import io.apibrew.nano.instance.proxy.ConsoleProxy;
import io.apibrew.nano.instance.proxy.LoadResourceProxy;
import io.apibrew.nano.model.Code;
import io.apibrew.nano.model.NanoInstance;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class GraalVMCodeExecutor {
    private final ExecutorService virtualThreadsExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final Map<String, Context> contextMap = new ConcurrentHashMap<>();
    @Getter
    private final InstanceDataStore dataStore;

    private final Map<String, Handler<GenericRecord>> handlerMap = new HashMap<>();
    private final PollerExtensionService ext;

    private ThreadLocal<List<String>> codeOperators = new ThreadLocal<>();

    public GraalVMCodeExecutor(InstanceDataStore dataStore, Client client, NanoInstance instance) {
        this.dataStore = dataStore;

        log.info("Initializing GraalVMCodeExecutor");

        log.info("Testing nodejs");
        execute(new Code().withLanguage(Code.Language.JAVASCRIPT)
                .withName("test-nodejs")
                .withContent(Base64.getEncoder().encodeToString("var a = 'test'".getBytes())));

        log.info("Testing python");
        execute(new Code().withLanguage(Code.Language.PYTHON)
                .withName("test-python")
                .withContent(Base64.getEncoder().encodeToString("def run(input):\n    return input".getBytes())));

        log.info("GraalVMCodeExecutor initialized");

        this.ext = new PollerExtensionService(client, "nano-code-ext-chan");

        executorService.execute(() -> {
            try {
                this.ext.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void unRegisterCode(Code code) {
//        throw new UnsupportedOperationException("Registered code cannot be unregistered");
    }

    public void registerCode(Code code) {
        log.info("Registering code: " + code.getName());
        executorService.execute(() -> registerAsync(code));
    }

    public void execute(Code code) {
        executorService.execute(() -> registerAsync(code));
    }

    private void registerAsync(Code code) {
        Context context = locateContext(code);
        String content = new String(Base64.getDecoder().decode(code.getContent()));
        try {
            codeOperators.set(new ArrayList<>());
            AtomicBoolean timeoutCancel = handleTimeout(code, context);

            log.debug("Begin executing code: " + code.getName());

            if (code.getLanguage() == Code.Language.JAVASCRIPT) {
                Source source = Source.newBuilder("js", content, code.getName() + ".js")
                        .mimeType("application/javascript+module")
                        .build();

                context.eval(source);
            } else if (code.getLanguage() == Code.Language.PYTHON) {
                context.eval("python", content);
            }

            log.debug("end executing code: " + code.getName());
            timeoutCancel.set(true);
        } catch (Exception e) {
            System.out.println("Error while executing code: " + content);
            log.error("Error while executing code: " + code.getName(), e);
            contextMap.remove(prepareContextName(code), context);
        } finally {
            if (!codeOperators.get().isEmpty()) {
                this.ext.registerPendingItems();
            }

            codeOperators.set(null);
        }
    }

    private Context locateContext(Code code) {
        String contextName = prepareContextName(code);

        contextMap.computeIfAbsent(contextName, name -> prepareNewContext());

        return contextMap.get(contextName);
    }

    private static String prepareContextName(Code code) {
        return Thread.currentThread().getName() + "-" + code.getName();
    }

    private Context prepareNewContext() {
        Context context = Context.newBuilder("python", "js")
                .allowExperimentalOptions(true)
                .option("js.esm-eval-returns-exports", "true")
//                .allowIO(IOAccess.newBuilder()
//                        .fileSystem(new ModuleFileSystem(this))
//                        .build())

//                .sandbox(SandboxPolicy.UNTRUSTED)
                .build();

        registerProxies(context);

        return context;
    }

    private void registerProxies(Context context) {
        context.getBindings("python").putMember("resource", new LoadResourceProxy(this));
        context.getBindings("python").putMember("console", new ConsoleProxy(this));

        context.getBindings("js").putMember("resource", new LoadResourceProxy(this));
        context.getBindings("js").putMember("console", new ConsoleProxy(this));
    }

    private AtomicBoolean handleTimeout(Code code, Context context) {
        AtomicBoolean isExecuted = new AtomicBoolean(false);

        virtualThreadsExecutorService.submit(() -> {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                log.error(e);
            }

            if (!isExecuted.get()) {
                context.close(true);
                contextMap.remove(prepareContextName(code), context);
            }
        });

        return isExecuted;
    }

    public void stop() {
        virtualThreadsExecutorService.shutdown();
    }

    public Handler<GenericRecord> locateHandler(Resource resource) {
        EntityInfo<GenericRecord> entityInfo = EntityInfo.fromResource(resource);

        handlerMap.computeIfAbsent(entityInfo.toString(), name -> prepareHandler(entityInfo));

        return handlerMap.get(entityInfo.toString());
    }

    private Handler<GenericRecord> prepareHandler(EntityInfo<GenericRecord> entityInfo) {
        return ext.handler(entityInfo);
    }

    public void ensureInsideCodeInitializer() {
        if (codeOperators.get() == null) {
            throw new RuntimeException("code operator registration is only allowed upon code initialization");
        }
    }

    public void registerOperatorId(String operatorId) {
        codeOperators.get().add(operatorId);
    }
}
