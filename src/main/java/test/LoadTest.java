package test;

import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.client.model.logic.Function;
import io.apibrew.client.model.logic.FunctionExecution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class LoadTest {

    public static void main(String[] args) throws InterruptedException {

        Client client = Client.newClientByServerName("77fbc4-http");
        Repository<FunctionExecution> repo = client.repository(FunctionExecution.class);

        Semaphore semaphore = new Semaphore(5);

        try (ExecutorService executorService = Executors.newFixedThreadPool(200)) {
            for (int i = 0; i < 200; i++) {
                System.out.println("Starting thread: " + i);
                semaphore.acquire();
                executorService.submit(() -> {
                    try {
                        System.out.println("Executing function");

                        FunctionExecution functionExecution = new FunctionExecution();
                        functionExecution.setFunction(new Function().withPackage("test").withName("function-1"));


                        functionExecution = repo.create(functionExecution);

                        System.out.println(functionExecution.getError());
                        System.out.println(functionExecution.getOutput());
                        System.out.println("Responded");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Unable to process event");
                    } finally {
                        semaphore.release();
                    }
                });
            }
        }

    }
}
