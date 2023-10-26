package io.apibrew.nano.instance;

import io.apibrew.client.ApiException;
import io.apibrew.client.GenericRecord;
import io.apibrew.client.Repository;
import io.apibrew.client.model.Extension;
import io.apibrew.client.model.Resource;
import io.apibrew.common.ext.ExtensionService;
import io.apibrew.common.ext.Handler;
import io.apibrew.nano.instance.proxy.ConsoleProxy;
import io.apibrew.nano.instance.proxy.LoadResourceProxy;
import io.apibrew.nano.model.Code;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class CodeExecutor {

    private static final ExecutorService virtualThreadsExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    private final Context context;

    private final Map<String, List<String>> codeOperators = new HashMap<>();
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(1000);
    private final ExtensionService ext;
    private final GraalVmNanoEngine graalVmNanoEngine;
    @Getter
    private Code currentInitializingCode;

    @Getter
    private boolean isRunning = true;

    private final Thread thread = new Thread(this::run);

    public CodeExecutor(ExtensionService ext, GraalVmNanoEngine graalVmNanoEngine) {
        this.context = prepareNewContext();
        this.ext = ext;
        this.graalVmNanoEngine = graalVmNanoEngine;

        log.info("CodeExecutor initialized");

        thread.setName("CodeExecutor-" + UUID.randomUUID());

        thread.start();
        isRunning = true;
    }

    private void run() {
        log.info("CodeExecutor thread started");
        while (isRunning) {
            try {
                Runnable runnable = taskQueue.take();
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("CodeExecutor thread stopped");
    }

    public void register(Code code) {
        if (!isRunning) {
            throw new IllegalStateException("CodeExecutor is not running");
        }

        executeInContextThread(() -> registerAsync(code));
    }

    public void executeInContextThread(Runnable runnable) {
        CountDownLatch latch = new CountDownLatch(1);

        AtomicReference<Throwable> throwable = new AtomicReference<>();

        taskQueue.add(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                throwable.set(t);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (throwable.get() != null) {
            handleException(throwable.get());
        }
    }

    private void handleException(Throwable throwable) {
        if (throwable instanceof PolyglotException) {
            PolyglotException polyglotException = (PolyglotException) throwable;

            if (polyglotException.isHostException()) {
                handleException(polyglotException.asHostException());
            } else if (polyglotException.isGuestException()) {
                throw new ApiException(Extension.Code.EXTERNAL_BACKEND_ERROR, polyglotException.getMessage());
            } else {
                throw polyglotException;
            }
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else {
            throw new RuntimeException(throwable);
        }
    }

    public void unRegister(Code code) {
        for (String operatorId : codeOperators.get(code.getName())) {
            ext.unRegisterOperator(operatorId);
        }
    }

    @SneakyThrows
    private void registerAsync(Code code) {
        log.debug("Registering code: " + code.getName());
        String content = new String(Base64.getDecoder().decode(code.getContent()));
        this.currentInitializingCode = code;
        codeOperators.put(code.getName(), new ArrayList<>());
        log.debug("Content:\n" + content + "\n");
        try {
            AtomicBoolean timeoutCancel = handleTimeout(code, context);

            log.debug("Begin executing code: " + code.getName());

            if (code.getLanguage() == Code.Language.JAVASCRIPT) {
                Source source = Source.newBuilder("js", content, code.getName() + "-" + UUID.randomUUID() + ".js")
                        .mimeType("application/javascript+module")
                        .build();

                context.eval(source);
            } else if (code.getLanguage() == Code.Language.PYTHON) {
                context.eval("python", content);
            }

            log.debug("end executing code: " + code.getName());
            timeoutCancel.set(true);
        } catch (Exception e) {
            log.error("Error while executing code: " + code.getName(), e);
            unRegister(code);
            throw e;
        } finally {
            currentInitializingCode = null;
            if (!codeOperators.get(code.getName()).isEmpty()) {
                this.ext.registerPendingItems();
            }
        }
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
        context.getBindings("python").putMember("console", new ConsoleProxy());

        context.getBindings("js").putMember("resource", new LoadResourceProxy(this));
        context.getBindings("js").putMember("console", new ConsoleProxy());
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
                isRunning = false;
            }
        });

        return isExecuted;
    }

    public void execute(Code code) {
        if (!isRunning) {
            throw new IllegalStateException("CodeExecutor is not running");
        }

        executeInContextThread(() -> registerAsync(code));
    }

    public void stop() {
        isRunning = false;
        thread.interrupt();
        context.close(true);
    }

    public void ensureInsideCodeInitializer() {
        if (currentInitializingCode == null) {
            throw new IllegalStateException("Not inside code initializer");
        }
    }

    public void registerOperatorId(String operatorId) {
        codeOperators.get(currentInitializingCode.getName()).add(operatorId);
    }

    public Handler<GenericRecord> locateHandler(Resource resource) {
        return this.graalVmNanoEngine.locateHandler(resource);
    }

    public Resource getResourceByName(String namespace, String resourceName) {
        return graalVmNanoEngine.getDataStore().getResourceByName(namespace, resourceName);
    }

    public Repository<GenericRecord> locateRepository(Resource resource) {
        return this.graalVmNanoEngine.locateRepository(resource);
    }
}
