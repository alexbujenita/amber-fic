package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HasherTest {

    private Hasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new Hasher();
    }

    @Test
    void shouldCorrectlyHashUsingSHA512() throws IOException {
        // given an input stream and an expected outcome
        final String toBeHashed = "abc";
        final String expectedHash =
                "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f";
        final InputStream streamToHash = new ByteArrayInputStream(toBeHashed.getBytes(StandardCharsets.UTF_8));

        // when we call the hash method
        final String hash = hasher.hash(streamToHash);

        // then the computed hash is correct

        assertThat(hash).isEqualTo(expectedHash);
    }

    @Test
    void shouldCorrectlyHashEmpty() throws IOException {
        // given an empty input stream and an expected outcome
        final String toBeHashed = "";
        final String expectedHash =
                "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";
        final InputStream streamToHash = new ByteArrayInputStream(toBeHashed.getBytes(StandardCharsets.UTF_8));

        // when we call the hash method
        final String hash = hasher.hash(streamToHash);

        // then the computed hash is correct

        assertThat(hash).isEqualTo(expectedHash);
    }
}
