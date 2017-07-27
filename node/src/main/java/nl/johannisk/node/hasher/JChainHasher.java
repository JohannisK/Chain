package nl.johannisk.node.hasher;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import nl.johannisk.node.service.model.Message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class JChainHasher {

    private JChainHasher() {

    }

    public static String hash(final String parentHash, final Set<Message> content, final String nonce) {
        StringBuilder b = new StringBuilder();
        b.append(parentHash);
        b.append(content.toString());
        b.append(nonce);
        String blockData = b.toString();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            /*
             * This excpetion may never be thrown.
             *
             * Every implementation of the Java platform is required to support the following standard MessageDigest algorithms:
             * MD5
             * SHA-1
             * SHA-256
             */
            throw new RuntimeException("Java platform does not support standard encryption", e);
        }
        messageDigest.update(blockData.getBytes());
        return Base64.encode(messageDigest.digest());
    }

    public static boolean isValidHash(final String hash) {
        String lowered = hash;
        int j = lowered.indexOf('J');
        int c = lowered.indexOf('C');
        int o = lowered.indexOf('o');
        int r = lowered.indexOf('r');
        int e = lowered.indexOf('e');
        return ((j != -1 && c != -1 && o != -1 && r != -1 && e != -1) && (j < c &&
                c < o &&
                o < r &&
                r < e));
    }
}
