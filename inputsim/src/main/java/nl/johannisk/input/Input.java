package nl.johannisk.input;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Input {

    public static void main(String[] args) {
        SpringApplication.run(Input.class, args);
    }

}