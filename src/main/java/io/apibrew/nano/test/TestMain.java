package io.apibrew.nano.test;

import io.apibrew.nano.instance.AllowGuestAccess;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class TestMain {

    private static final ExecutorService es = Executors.newFixedThreadPool(10);

    static class TestProxy implements ProxyExecutable {

        @Override
        public Object execute(Value... arguments) {
            System.out.println("TestProxy.execute called");
            es.execute(() -> {
                System.out.println("TestProxy.execute called inside virtual thread");
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                arguments[0].execute(123);
                System.out.println("TestProxy.execute called inside virtual thread done");
            });

            System.out.println("TestProxy.execute called done");
            return null;
        }

        void then(Value resolve, Value reject) {
            try {
                Object someResult = 123;
                resolve.executeVoid(someResult);
            } catch (Throwable t) {
                reject.executeVoid(t);
            }
        }
    }

    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    private static String code = """
            import asyncio
                        
            async def async_wrapper(func):
                loop = asyncio.get_event_loop()

                def callback(result):
                    print('callback called')
                    nonlocal loop, future
                    future.get_loop().call_soon_threadsafe(future.set_result, result)
                    print('callback called 2')

                future = loop.create_future()
                print('call func')
                func(callback)
                print('func called')

                return await future

            def main():
                asyncio.run(async_wrapper(test1))
                print('main done')

                                      
                                    """;

    private static String code2 = """
                       
            async function asyncWrapper(func) {
              return new Promise(async (resolve, reject) => {
                const callback = (result) => {
                  console.log('callback called');
                  resolve(result);
                  console.log('callback called 2');
                };

                console.log('call func');
                func(callback);
                console.log('func called');
              });
            }

            async function main() {
              try {
                console.log('start main');
                const result = await asyncWrapper(test1);
                console.log('main done');
                console.log('Result:', result);
              } catch (error) {
                console.error('Error:', error);
              }
            }
                                                  
                                                """;

    public static void main(String[] args) {
        try (Context context = Context.newBuilder("js", "python")
                .allowExperimentalOptions(true)
                .resourceLimits(ResourceLimits.newBuilder().build())
                .allowHostAccess(HostAccess.newBuilder()
                        .allowAccessAnnotatedBy(AllowGuestAccess.class)
                        .build())
                .build()) {
            blockingQueue.add(() -> TestMain.prepareStart2(context));

            Runnable runnable;
            int i = 0;
            while ((runnable = blockingQueue.poll()) != null) {
                System.out.println("executing runnable: " + ++i);
                runnable.run();
                System.out.println("executed runnable: " + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void prepareStart(Context context) {
        context.getBindings("python").putMember("test1", new TestProxy());

        System.out.println("Start evl");
        context.eval("python", code);
        System.out.println("End evl");

        context.getBindings("python").getMember("main").execute();
    }

    private static void prepareStart2(Context context) {
        context.getBindings("js").putMember("test1", new TestProxy());

        System.out.println("Start evl");
        context.eval("js", code2);
        System.out.println("End evl");

        CountDownLatch latch = new CountDownLatch(1);

        Value then = context.getBindings("js").getMember("main").execute().getMember("then");

        System.out.println(then);

        then.executeVoid((Consumer) (result) -> {
            System.out.println("then called");
            System.out.println(result);
            latch.countDown();
        }, (Consumer) (error) -> {
            System.out.println("then error called");
            System.out.println(error);
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
