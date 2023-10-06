package io.apibrew.faas;

import io.apibrew.client.Config;
import io.apibrew.faas.instance.InstanceClient;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Config.Server instanceConfig = new Config.Server();
        instanceConfig.setHost("http://localhost:9009");
        instanceConfig.setInsecure(true);
        Config.Authentication authentication = new Config.Authentication();
        authentication.setUsername("admin");
        authentication.setPassword("admin");
        instanceConfig.setAuthentication(authentication);

        InstanceClient instanceClient = new InstanceClient(instanceConfig);

        instanceClient.init();

//        instanceClient.stop();

        System.out.println("DONE!");
        Thread.sleep(1000000);

    }
}
