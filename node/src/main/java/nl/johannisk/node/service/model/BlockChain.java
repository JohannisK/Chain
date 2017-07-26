package nl.johannisk.node.service.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockChain {

    private final TreeNode<Block> root;
    private final Map<String, TreeNode<Block>> blocks;
    private TreeNode<Block> endBlock;
    private int maxDepth = 0;

    public BlockChain() {
        root = new TreeNode<>(Block.ZERO);
        blocks = new HashMap<>();
        blocks.put("0", root);
        endBlock = root;
        maxDepth = 0;
    }

    public boolean addBlock(Block block) {
        if(blocks.containsKey(block.getParentHash())) {
            TreeNode<Block> parentBlock = blocks.get(block.getParentHash());
            TreeNode<Block> newNode = parentBlock.addChild(block);
            if (newNode.getDepth() > maxDepth) {
                maxDepth = newNode.getDepth();
                endBlock = newNode;
            }
            blocks.put(block.getHash(), newNode);
            return true;
        }
        return false;
    }

    public synchronized TreeNode<Block> getEndBlock() {
        return endBlock;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(System.lineSeparator());
        b.append("CHAIN").append(System.lineSeparator());
        TreeNode<Block> block = endBlock;
        do {
            b.append("  >> HASH:        ").append(block.getData().getHash()).append(System.lineSeparator());
            b.append("  >> PARENT HASH: ").append(block.getData().getParentHash()).append(System.lineSeparator());
            b.append("  >> NONCE:       ").append(block.getData().getNonce()).append(System.lineSeparator());
            b.append("  >> CONTENT:     ").append(block.getData().getContent()).append(System.lineSeparator());
            b.append(System.lineSeparator());
        } while ((block = block.getParent()) != null);

        return b.toString();
    }

    public boolean contains(Block b) {
        return blocks.containsKey(b.getHash());
    }

    public List<Block> getOrphanedBlocks() {
        List<Block> blocksInChain = new ArrayList<>();
        TreeNode<Block> b = endBlock;
        do {
            blocksInChain.add(b.getData());
            b = b.getParent();
        } while (b != null);
        return blocks.values().stream().map(t -> t.getData()).filter(block -> !blocksInChain.contains(block)).collect(Collectors.toList());
    }
}
