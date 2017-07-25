package nl.johannisk.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Node {

    public static void main(String[] args) {
        SpringApplication.run(Node.class, args);
    }
}