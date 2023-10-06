package io.apibrew.faas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apibrew.client.Client;
import io.apibrew.client.Repository;
import io.apibrew.common.ext.Condition;
import io.apibrew.common.ext.Handler;
import io.apibrew.common.impl.PollerExtensionService;
import io.apibrew.faas.instance.InstanceClient;
import io.apibrew.faas.model.Config;
import io.apibrew.faas.model.FaasInstance;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class Main {

    private final static Map<String, InstanceClient> instanceMap = new HashMap<String, InstanceClient>();

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length == 0) {
            System.out.println("Invalid args: --config <config location.json>");
            return;
        }

        if (args.length == 1) {
            args = args[0].split("=");
        }

        if (args.length != 2) {
            System.out.println("Invalid args: --config <config location.json>");
            return;
        }

        if (!args[0].equals("--config")) {
            System.out.println("Invalid args: --config <config location.json>");
            return;
        }

        // loading config

        Config config = new ObjectMapper().readValue(new java.io.File(args[1]), Config.class);

        log.info("Loaded config: " + config);

        if (config.getInstances() != null) {
            log.info("Starting preconfigured instances");
            config.getInstances().forEach(Main::startUpInstance);
        }

        // setup controller

        if (config.getController() != null) {
            log.info("Starting controller");
            startUpController(config.getController());
        } else {
            // wait indefinitely
            Thread.sleep(Long.MAX_VALUE);
        }

    }

    private static void startUpController(Config.ServerConfig controller) throws IOException {
        log.info("Starting controller: " + controller.getHost());

        io.apibrew.client.Config.Server controllerConfig = prepareConfigServer(controller);

        Client client = Client.newClientByServerConfig(controllerConfig);

        Repository<FaasInstance> instancesRepository = client.repository(FaasInstance.class);

        log.info("Starting controller instances");
        instancesRepository.list().getContent().forEach(Main::startUpInstance);

        log.info("Starting controller instance listener");

        PollerExtensionService extService = new PollerExtensionService(client, "faas-instance-poller");

        log.info("Started controller: " + controller.getHost());

        Handler<FaasInstance> faasInstanceHandler = extService.handler(FaasInstance.class);

        faasInstanceHandler.when(Condition.afterCreate())
                .when(Condition.async())
                .operate((event, instance) -> {
                    log.info("Creating instance: " + instance.getName());
                    startUpInstance(instance);
                    log.info("Created instance: " + instance.getName());

                    return instance;
                });

        faasInstanceHandler.when(Condition.afterUpdate())
                .when(Condition.async())
                .operate((event, instance) -> {
                    log.info("Updating instance: " + instance.getName());
                    destroyInstance(instance);
                    startUpInstance(instance);
                    log.info("Updated instance: " + instance.getName());

                    return instance;
                });

        faasInstanceHandler.when(Condition.afterDelete())
                .when(Condition.async())
                .operate((event, instance) -> {
                    log.info("Deleting instance: " + instance.getName());
                    destroyInstance(instance);
                    log.info("Deleted instance: " + instance.getName());

                    return instance;
                });

        extService.run();
    }

    private static void destroyInstance(FaasInstance instance) {
        if (!instanceMap.containsKey(instance.getName())) {
            log.error("Instance not started: " + instance.getName());
            return;
        }

        instanceMap.get(instance.getName()).stop();
        instanceMap.remove(instance.getName());
    }

    private static io.apibrew.client.Config.Server prepareConfigServer(Config.ServerConfig controller) {
        io.apibrew.client.Config.Server controllerConfig = new io.apibrew.client.Config.Server();
        controllerConfig.setHost(controller.getHost());
        controllerConfig.setInsecure(controller.getInsecure());
        io.apibrew.client.Config.Authentication authentication = new io.apibrew.client.Config.Authentication();
        authentication.setUsername(controller.getAuthentication().getUsername());
        authentication.setPassword(controller.getAuthentication().getPassword());
        authentication.setToken(controller.getAuthentication().getToken());
        controllerConfig.setAuthentication(authentication);
        return controllerConfig;
    }

    private static void startUpInstance(FaasInstance instance) {
        if (instanceMap.containsKey(instance.getName())) {
            log.error("Instance already started: " + instance.getName());
            return;
        }

        log.info("Starting instance: " + instance.getName());

        Thread thread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    InstanceClient instanceClient = new InstanceClient(instance);
                    instanceClient.init();
                    instanceMap.put(instance.getName(), instanceClient);
                    break;
                } catch (Exception e) {
                    log.error("Unable to start instance: " + instance.getName(), e);
                    try {
                        Thread.sleep(1000 * (i + 1));
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }

            log.info("Started instance: " + instance.getName());
        });

        thread.setName("faas-instance-startup[" + instance.getName() + "]");

        thread.start();
    }
}
