package ravzeno.droppers_service.commons.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHasherTest {
    @Test
    void producesTheSha256HexDigest() {
        assertThat(TokenHasher.sha256("droppers"))
                .isEqualTo("cd3450dba5ef1c7d24bb2e9fd66c0fa3cdb71645bd2ae8c63ea581bb3bbf56ba");
    }
}
