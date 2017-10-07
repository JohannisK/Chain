package nl.johannisk.node.service;

import nl.johannisk.node.hasher.*;
import nl.johannisk.node.service.model.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.function.*;

@Service
public class BlockCreatorService {

    public enum State {
        READY,
        RUNNING,
        CANCELLED
    }

    final Random random;
    State state;

    public BlockCreatorService() {
        random = new Random();
        state = State.READY;
    }

    @Async
    void createBlock(Block parentBlock, Set<Message> messages, Consumer<Block> consumer) {
        state = State.RUNNING;
        String hash;
        String parentHash = parentBlock.getHash();
        long nonce = random.nextLong();
        do {
            hash = JChainHasher.hash(parentHash, messages, Long.toString(++nonce));
            try {
                Thread.sleep(6);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (!JChainHasher.isValidHash(hash) && state.equals(State.RUNNING));

        if (state.equals(State.RUNNING)) {
            consumer.accept(new Block(hash, parentHash, messages, Long.toString(nonce)));
        }
        state = State.READY;
    }
}
