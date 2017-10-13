package nl.johannisk.node.controller;

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

    private final BlockChainService blockChainService;

    @Autowired
    public ApiController(final BlockChainService blockChainService) {
        this.blockChainService = blockChainService;
    }


    @RequestMapping(path = "/chain")
    public List<Block> chain() {
        final BlockChain blockChain = blockChainService.getChain();
        final List<Block> mainChain = new LinkedList<>();
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
