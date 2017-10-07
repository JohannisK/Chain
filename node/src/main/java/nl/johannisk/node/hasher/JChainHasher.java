package nl.johannisk.node.hasher;

import nl.johannisk.node.service.model.Message;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;

public class JChainHasher {
    private JChainHasher() {
    }

    public static String hash(final String parentHash, final Set<Message> content, final String nonce) {
        final MessageDigest messageDigest = getMessageDigest();
        final String blockData = new StringBuilder()
                .append(parentHash)
                .append(content.toString())
                .append(nonce)
                .toString();
        messageDigest.update(blockData.getBytes());
        return new String(Base64.getEncoder().encode(messageDigest.digest()), StandardCharsets.UTF_8);
    }

    private static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            /*
             * This exception may never be thrown.
             *
             * Every implementation of the Java platform is required to support the following standard MessageDigest algorithms:
             * MD5
             * SHA-1
             * SHA-256
             */
            throw new RuntimeException("Java platform does not support standard encryption", e);
        }
    }

    public static boolean isValidHash(final String hash) {
        if (null == hash) {
            return false;
        }
        final int jIndex = hash.indexOf('J');
        final int cIndex = hash.indexOf('C');
        final int oIndex = hash.indexOf('o');
        final int rIndex = hash.indexOf('r');
        final int eIndex = hash.indexOf('e');
        final boolean didFindJcoreCharacters = jIndex != -1 && cIndex != -1 && oIndex != -1 && rIndex != -1 && eIndex != -1;
        final boolean jcoreCharactersInCorrectOrder = jIndex < cIndex && cIndex < oIndex && oIndex < rIndex && rIndex < eIndex;
        return didFindJcoreCharacters && jcoreCharactersInCorrectOrder;
    }
}
