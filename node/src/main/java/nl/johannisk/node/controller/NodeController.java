package nl.johannisk.node.controller;

import com.netflix.discovery.EurekaClient;
import nl.johannisk.node.service.BlockChainService;
import nl.johannisk.node.service.model.Block;
import nl.johannisk.node.service.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/node")
public class NodeController {

    private final BlockChainService blockChain;

    @Autowired
    public NodeController(final BlockChainService blockChain) {
        this.blockChain = blockChain;
    }


    @PostMapping (path = "/message")
    public void message(@RequestBody final Message message) {
        blockChain.addMessage(message);
    }

    @PostMapping (path = "/block")
    public void message(@RequestBody final Block block) {
        blockChain.addBlock(block);
    }
}
