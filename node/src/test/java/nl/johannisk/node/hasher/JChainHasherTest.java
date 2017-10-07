package nl.johannisk.node.hasher;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.codec.binary.Base64;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class JChainHasherTest {
    @Test
    public void testIsValidHashWithInvalidHashes() {
        assertFalse(JChainHasher.isValidHash(null));
        assertFalse(JChainHasher.isValidHash(""));
        assertFalse(JChainHasher.isValidHash("foo"));
    }

    @Test
    public void testIsValidHashWithInvalidlyCasedHashes() {
        assertFalse(JChainHasher.isValidHash("JCoer"));
        assertFalse(JChainHasher.isValidHash("jcore"));
        assertFalse(JChainHasher.isValidHash("JCoRe"));
    }

    @Test
    public void testIsValidHashWithInvalidlyOrderedCharacterHashes() {
        assertFalse(JChainHasher.isValidHash("JCoe JCore"));
    }

    @Test
    public void testIsValidHashWithValidHashes() {
        assertTrue(JChainHasher.isValidHash("JCore"));
        assertTrue(JChainHasher.isValidHash("JCor Core"));
        assertTrue(JChainHasher.isValidHash("JCor Core"));
        assertTrue(JChainHasher.isValidHash("f456J7yhgtC567uhogfdr4567e8ui"));
    }

    @Test
    public void testHashProducesNonEmptyString() {
        final String hash = JChainHasher.hash(null, ImmutableSet.of(), null);
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    public void testHashProducesBase64() {
        final String hash = JChainHasher.hash(null, ImmutableSet.of(), null);
        assertNotNull(hash);
            assertTrue(Base64.isBase64(hash));
    }
}
