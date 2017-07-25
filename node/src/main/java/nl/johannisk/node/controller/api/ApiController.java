package nl.johannisk.node.controller.api;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api")
public class ApiController {

    private EurekaClient eurekaClient;

    @Autowired
    public ApiController(EurekaClient eurekaClient) {

        this.eurekaClient = eurekaClient;
    }


    @RequestMapping(path = "/newMessage")
    public void newMessage() {
        System.out.println("message received");
        Application application = eurekaClient.getApplication("jchain-node");
        List<InstanceInfo> instanceInfo = application.getInstances();
        for(InstanceInfo info : instanceInfo) {
            System.out.println("-" + info.getHomePageUrl());
        }
    }

    @RequestMapping(path = "/propegatedMessage")
    public void propegatedMessage() {
        System.out.println("message received");
        Application application = eurekaClient.getApplication("jchain-node");
        List<InstanceInfo> instanceInfo = application.getInstances();
        for(InstanceInfo info : instanceInfo) {
            System.out.println("-" + info.getHomePageUrl());
        }
    }
}
