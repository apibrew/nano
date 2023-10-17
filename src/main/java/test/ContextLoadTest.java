package test;

import org.graalvm.polyglot.Context;

import java.util.ArrayList;
import java.util.List;

public class ContextLoadTest {

    public static void main(String[] args) throws InterruptedException {
        new ContextLoadTest().run();
    }

    private void run() throws InterruptedException {
        List<Context> contexes = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            contexes.add(Context.newBuilder("js").allowExperimentalOptions(true).build());
        }

        System.out.println("Contexes created");
        while (true) {
            Thread.sleep(1000);

            for (Context context : contexes) {
                context.eval("js", "console.log('Hello world!');");
            }
        }
    }
}
