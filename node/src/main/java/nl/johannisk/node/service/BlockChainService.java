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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class BlockChainService {

    @Value("{eureka.instance.instanceId}")
    String instanceId;

    final List<Message> unhandledMessages;
    final List<Message> handledMessages;
    final BlockChain chain;
    private final EurekaClient eurekaClient;
    private final TaskExecutor executor;
    BlockCreatorTask blockCreator;

    @Autowired
    public BlockChainService(EurekaClient eurekaClient, TaskExecutor executor) {
        this.eurekaClient = eurekaClient;
        this.executor = executor;
        unhandledMessages = new ArrayList<>();
        handledMessages = new ArrayList<>();
        chain = new BlockChain();
    }

    public void addMessage(Message m) {
        if(!handledMessages.contains(m) && !unhandledMessages.contains(m)) {
            unhandledMessages.add(m);
            if(unhandledMessages.size() >= 5) {
                List<Message> blockContent = pickMessagesForPotentialBlock();
                if(blockCreator == null) {
                    createBlock(blockContent);
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
                if(blockCreator != null) {
                    blockCreator.cancel();
                }
                unhandledMessages.addAll(handledMessages);
                handledMessages.clear();
                TreeNode<Block> block = chain.getEndBlock();
                do {
                    for(Message m : block.getData().getContent()) {
                        unhandledMessages.remove(m);
                        handledMessages.add(m);
                    }
                } while ((block = block.getParent()) != null);
                if(unhandledMessages.size() >= 5) {
                    List<Message> messageForNextBlock = new ArrayList<>();
                    for(int i = 4; i >= 0; i--) {
                        messageForNextBlock.add(unhandledMessages.remove(i));
                    }
                    handledMessages.addAll(messageForNextBlock);
                    createBlock(messageForNextBlock);
                }
            }
        }
    }

    private List<Message> pickMessagesForPotentialBlock() {
        List<Message> messageForNextBlock = new ArrayList<>();
        for(int i = 4; i >= 0; i--) {
            messageForNextBlock.add(unhandledMessages.remove(i));
        }
        handledMessages.addAll(messageForNextBlock);
        return messageForNextBlock;
    }

    private void createBlock(List<Message> messages) {
        blockCreator = new BlockCreatorTask(chain.getEndBlock().getData().getHash(), messages);
        executor.execute(blockCreator);
    }



    public BlockChain getChain() {
        return chain;
    }

    public List<Message> getUnprocessedMessages() {
        return unhandledMessages;
    }

    private class BlockCreatorTask implements Runnable {

        Random random;
        String parentHash;
        Set<Message> messages;
        long nonce;
        private boolean cancelled = false;

        public BlockCreatorTask(String parentHash, List<Message> messages) {
            Collections.sort(messages);
            this.parentHash = parentHash;
            this.messages = new LinkedHashSet<>(messages);
            random = new Random();
            nonce = random.nextLong();
        }

        public void run() {
            String hash;
            Block block;
            do {
                hash = JChainHasher.hash(parentHash, messages, Long.toString(++nonce));
                try {
                    Thread.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(!JChainHasher.isValidHash(hash) && !cancelled);

            if(!cancelled && chain.getEndBlock().getData().getHash().equals(parentHash)) {
                block = new Block(hash, parentHash, messages, Long.toString(nonce));
                chain.addBlock(block);
                System.out.println("Created block: " + block);
                System.out.println("Chain: " + chain);
                Application application = eurekaClient.getApplication("jchain-node");
                List<InstanceInfo> instanceInfo = application.getInstances();
                for(InstanceInfo info : instanceInfo) {
                    if(info.getInstanceId().equals(instanceId)) continue;
                    executor.execute(new NodeInformerTask(Integer.toString(info.getPort()), block));
                }
            }
            blockCreator = null;
        }

        public void cancel() {
            this.cancelled = true;
        }
    }

    private class NodeInformerTask implements Runnable {

        private final String host;
        private final Block block;
        private final int delay;

        public NodeInformerTask(String host, Block block) {
            Random random = new Random();
            this.host = host;
            this.block = block;
            this.delay = random.nextInt(10000) + 3000;
        }

        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject("http://localhost:" + host + "/node/block", block, Block.class);
        }
    }
}
