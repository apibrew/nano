package io.apibrew.nano;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apibrew.client.Client;
import io.apibrew.client.controller.Controller;
import io.apibrew.client.controller.InstanceClient;
import io.apibrew.nano.instance.NanoInstanceClient;
import io.apibrew.nano.model.Config;
import io.apibrew.nano.model.NanoInstance;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Arrays;

@Log4j2
public class Main {


    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Args: " + Arrays.asList(args));
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

        Controller<NanoInstance> controller = new Controller<>(NanoInstance.class, Main::newInstance);


        if (config.getInstances() != null) {
            log.info("Starting preconfigured instances");
            config.getInstances().forEach(controller::startUpInstance);
        }

        // setup controller
        if (config.getController() != null) {
            log.info("Starting controller");
            controller.startUpController(new NanoInstance.ServerConfig()
                    .withHost("manager-apibrew-server")
                    .withPort(9009)
                    .withHttpPort(9009)
                    .withInsecure(true)
                    .withAuthentication(new NanoInstance.ServerConfigAuthentication()
                            .withUsername(config.getController().getAuthentication().getUsername())
                            .withPassword(config.getController().getAuthentication().getPassword())
                            .withToken(config.getController().getAuthentication().getToken())
                    ));
        } else {
            // wait indefinitely
            Thread.sleep(Long.MAX_VALUE);
        }

    }

    private static InstanceClient newInstance(Client client, NanoInstance controllerInstance) {
        return new NanoInstanceClient(client, controllerInstance);
    }
}
