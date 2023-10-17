package test;

import org.graalvm.polyglot.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContextMultiThreadingTest {

    public static void main(String[] args) throws InterruptedException {
        new ContextMultiThreadingTest().run();
    }

    private void run() throws InterruptedException {
        Context context = Context.newBuilder("js").allowExperimentalOptions(true).build();

        int count = 100;

        CountDownLatch latch = new CountDownLatch(count);
        context.eval("js", "console.log('Hello world!');");
        context.eval("js", "console.log('Hello world!');");
        context.eval("js", "console.log('Hello world!');");

        try (ExecutorService executorService = Executors.newFixedThreadPool(200)) {
            for (int i = 0; i < count; i++) {
                executorService.submit(() -> {
                    try {
                        System.out.println("Started on: " + Thread.currentThread().getName());
                        context.eval("js", "console.log('Hello world!');");
                        System.out.println("Done on: " + Thread.currentThread().getName());
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        latch.await();
    }
}
