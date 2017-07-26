package nl.johannisk.node.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import nl.johannisk.node.hasher.JChainHasher;
import nl.johannisk.node.service.model.Block;
import nl.johannisk.node.service.model.BlockChain;
import nl.johannisk.node.service.model.Message;
import nl.johannisk.node.service.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import static com.netflix.appinfo.AmazonInfo.MetaDataKey.instanceId;

@Service
public class BlockChainService {

    @Value("${eureka.instance.instanceId}")
    String instanceId;

    final Set<Message> unhandledMessages;
    final Set<Message> handledMessages;
    final BlockChain chain;
    private BlockCreatorService blockCreatorService;
    private final EurekaClient eurekaClient;
    private final TaskExecutor executor;
    Random random;

    @Autowired
    public BlockChainService(BlockCreatorService blockCreatorService, EurekaClient eurekaClient, TaskExecutor executor) {
        this.blockCreatorService = blockCreatorService;
        this.eurekaClient = eurekaClient;
        this.executor = executor;
        unhandledMessages = new HashSet<>();
        handledMessages = new HashSet<>();
        chain = new BlockChain();
        random = new Random();
    }

    public BlockChain getChain() {
        return chain;
    }

    public Set<Message> getUnprocessedMessages() {
        return unhandledMessages;
    }

    public void addMessage(Message m) {
        if(!handledMessages.contains(m) && !unhandledMessages.contains(m)) {
            unhandledMessages.add(m);
            if(unhandledMessages.size() >= 5) {
                if(blockCreatorService.state == BlockCreatorService.State.READY) {
                    Set<Message> blockContent = pickMessagesForPotentialBlock();
                    blockCreatorService.createBlock(chain.getEndBlock().getData(), blockContent, this::addCreatedBlock);
                }
            }
        }
    }

    public void addBlock(Block b) {
        String hash = JChainHasher.hash(b.getParentHash(), b.getContent(), b.getNonce());
        if(JChainHasher.isValidHash(hash) && b.getHash().equals(hash) && !chain.contains(b)) {
            String lastBlockHash = chain.getEndBlock().getData().getHash();
            chain.addBlock(b);
            if(!chain.getEndBlock().getData().getHash().equals(lastBlockHash)) {
                if(blockCreatorService.state == BlockCreatorService.State.RUNNING) {
                    blockCreatorService.state = BlockCreatorService.State.CANCELLED;
                }
                resetMessagesAccordingToChain();
                if(unhandledMessages.size() >= 5) {
                    if(blockCreatorService.state == BlockCreatorService.State.READY) {
                        Set<Message> blockContent = pickMessagesForPotentialBlock();
                        blockCreatorService.createBlock(chain.getEndBlock().getData(), blockContent, this::addCreatedBlock);
                    }
                }
            }
        }
    }

    public void addCreatedBlock(Block block){
        if(chain.getEndBlock().getData().getHash().equals(block.getParentHash())) {
            chain.addBlock(block);
            Application application = eurekaClient.getApplication("jchain-node");
            List<InstanceInfo> instanceInfo = application.getInstances();
            for(InstanceInfo info : instanceInfo) {
                if(info.getInstanceId().equals(instanceId)) {
                    continue;
                }
                informNodeOfNewBlock(Integer.toString(info.getPort()), block);
            }
        }
    }

    private void resetMessagesAccordingToChain() {
        unhandledMessages.addAll(handledMessages);
        handledMessages.clear();
        TreeNode<Block> block = chain.getEndBlock();
        do {
            for(Message m : block.getData().getContent()) {
                unhandledMessages.remove(m);
                handledMessages.add(m);
            }
        } while ((block = block.getParent()) != null);
    }

    private Set<Message> pickMessagesForPotentialBlock() {
        Set<Message> messageForNextBlock = unhandledMessages.stream()
                .limit(5)
                .collect(Collectors.toSet());
        unhandledMessages.removeAll(messageForNextBlock);
        handledMessages.addAll(messageForNextBlock);
        return messageForNextBlock;
    }

    @Async
    private void informNodeOfNewBlock(String host, Block block) {
        int delay = random.nextInt(10000) + 3000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject("http://localhost:" + host + "/node/block", block, Block.class);
    }
}
