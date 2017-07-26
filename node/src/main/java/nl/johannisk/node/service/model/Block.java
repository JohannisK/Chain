package nl.johannisk.node.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;

public class Block {
    public static Block ZERO;
    static {
        ZERO = new Block("0", "0", new LinkedHashSet<>(), "0");
    }

    private final String hash;
    private final String parentHash;
    private final Set<Message> contents;
    private final String nonce;

    public Block(@JsonProperty("hash") String hash, @JsonProperty("parentHash") String parentHash, @JsonProperty("content") Set<Message> content, @JsonProperty("nonce") String nonce) {
        this.hash = hash;
        this.parentHash = parentHash;
        this.contents = new LinkedHashSet<>(content);
        this.nonce = nonce;
    }

    public String getHash() {
        return hash;
    }

    public String getParentHash() {
        return parentHash;
    }

    public Set<Message> getContent() {
        return new LinkedHashSet<Message>(contents);
    }

    public String getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", contents=" + contents +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
