package nl.johannisk.node.hasher;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import nl.johannisk.node.service.model.Message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;

public class JChainHasher {

    private JChainHasher() {

    }

    public static String hash(String parentHash, Set<Message> content, String nonce) {
        StringBuilder b = new StringBuilder();
        b.append(parentHash);
        b.append(content.toString());
        b.append(nonce);
        String blockData = b.toString();
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(blockData.getBytes());
        return Base64.encode(messageDigest.digest());
    }

    public static boolean isValidHash(String hash) {
        String lowered = hash;//.toLowerCase();
        int j = lowered.indexOf("J"),
                c = lowered.indexOf("C"),
                o = lowered.indexOf("o"),
                r = lowered.indexOf("r"),
                e = lowered.indexOf("e");
        return ((j != -1 && c != -1 && o != -1 && r != -1 && e != -1) && (j < c &&
                c < o &&
                o < r &&
                r < e));
    }

    private static String setToContentString(Set<Message> content) {
        return content.stream().map(m -> m.getIndex() + ":" + m.getText() + ";").collect(Collectors.joining());
    }
}
