package nl.johannisk.node.service;

import com.netflix.appinfo.*;
import com.netflix.discovery.*;
import com.netflix.discovery.shared.*;
import nl.johannisk.node.hasher.*;
import nl.johannisk.node.service.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import java.util.*;
import java.util.stream.*;

@Service
public class BlockChainService {

    @Value("${eureka.instance.instanceId}")
    private String instanceId;

    private final Set<Message> unhandledMessages;
    private final Set<Message> handledMessages;
    private final BlockChain chain;
    private BlockCreatorService blockCreatorService;
    private final EurekaClient eurekaClient;
    private Random random;

    @Autowired
    public BlockChainService(final BlockCreatorService blockCreatorService, final EurekaClient eurekaClient) {
        this.blockCreatorService = blockCreatorService;
        this.eurekaClient = eurekaClient;
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

    public void addMessage(final Message m) {
        if (!handledMessages.contains(m) && !unhandledMessages.contains(m)) {
            unhandledMessages.add(m);
            if (unhandledMessages.size() >= 5 && blockCreatorService.state == BlockCreatorService.State.READY) {
                Set<Message> blockContent = pickMessagesForPotentialBlock();
                blockCreatorService.createBlock(chain.getEndBlock().getData(), blockContent, this::addCreatedBlock);
            }

        }
    }

    public void addBlock(final Block block) {
        String hash = JChainHasher.hash(block.getParentHash(), block.getContent(), block.getNonce());
        if (JChainHasher.isValidHash(hash) && block.getHash().equals(hash) && !chain.containsBlock(block)) {
            String lastBlockHash = chain.getEndBlock().getData().getHash();
            chain.addBlock(block);
            if (!chain.getEndBlock().getData().getHash().equals(lastBlockHash)) {
                if (blockCreatorService.state == BlockCreatorService.State.RUNNING) {
                    blockCreatorService.state = BlockCreatorService.State.CANCELLED;
                }
                resetMessagesAccordingToChain();
                if (unhandledMessages.size() >= 5 && blockCreatorService.state == BlockCreatorService.State.READY) {
                    Set<Message> blockContent = pickMessagesForPotentialBlock();
                    blockCreatorService.createBlock(chain.getEndBlock().getData(), blockContent, this::addCreatedBlock);
                }

            }
        }
    }

    private void addCreatedBlock(final Block block) {
        if (chain.getEndBlock().getData().getHash().equals(block.getParentHash())) {
            chain.addBlock(block);
            Application application = eurekaClient.getApplication("jchain-node");
            List<InstanceInfo> instanceInfo = application.getInstances();
            for (InstanceInfo info : instanceInfo) {
                if (info.getInstanceId().equals(instanceId)) {
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
            for (Message m : block.getData().getContent()) {
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
    private void informNodeOfNewBlock(final String host, final Block block) {
        int delay = random.nextInt(10000) + 3000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject("http://localhost:" + host + "/node/block", block, Block.class);
    }
}
