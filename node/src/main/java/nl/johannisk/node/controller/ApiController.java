package nl.johannisk.node.controller;

import com.netflix.discovery.EurekaClient;
import nl.johannisk.node.service.BlockChainService;
import nl.johannisk.node.service.model.Block;
import nl.johannisk.node.service.model.BlockChain;
import nl.johannisk.node.service.model.Message;
import nl.johannisk.node.service.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/api")
public class ApiController {

    private BlockChainService blockChainService;
    private EurekaClient eurekaClient;

    @Autowired
    public ApiController(BlockChainService blockChainService, EurekaClient eurekaClient) {
        this.blockChainService = blockChainService;
        this.eurekaClient = eurekaClient;
    }


    @RequestMapping(path = "/chain")
    public List<Block> chain() {
        BlockChain blockChain = blockChainService.getChain();
        List<Block> mainChain = new LinkedList<>();
        TreeNode<Block> b = blockChain.getEndBlock();
        do {
            mainChain.add(b.getData());
            b = b.getParent();
        } while (b != null);
        Collections.reverse(mainChain);
        return mainChain;
    }

    @RequestMapping(path = "/orphaned")
    public List<Block> orphaned() {
        return blockChainService.getChain().getOrphanedBlocks();
    }

    @RequestMapping(path = "/messages")
    public Set<Message> messages() {
        return blockChainService.getUnprocessedMessages();
    }
}
