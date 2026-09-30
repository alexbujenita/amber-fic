package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HasherTest {

    private static final String STRING_TO_HASH = "abc";
    private static final String HASHED_STRING_512 =
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f";

    public static Stream<Arguments> provideParams() {
        return Stream.of(
                arguments(
                        HashAlgorithm.SHA256,
                        STRING_TO_HASH,
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
                arguments(HashAlgorithm.SHA256, "", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                arguments(HashAlgorithm.SHA512, STRING_TO_HASH, HASHED_STRING_512),
                arguments(
                        HashAlgorithm.SHA512,
                        "",
                        "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"));
    }

    @ParameterizedTest
    @MethodSource("provideParams")
    void shouldCorrectlyHash(HashAlgorithm hashAlgorithm, String toHash, String expectedHash) throws IOException {
        // given an input stream and a hasher instance
        final InputStream streamToHash = new ByteArrayInputStream(toHash.getBytes(StandardCharsets.UTF_8));
        final Hasher hasher = new Hasher(hashAlgorithm);

        // when we call the hash method
        final String hash = hasher.hash(streamToHash);

        // then the computed hash is correct
        assertThat(hash).isEqualTo(expectedHash);
    }

    @Test
    void defaultsTo512() throws IOException {
        // given a no arg instance
        final Hasher hasher = new Hasher();

        // when called it produces a 512 hash
        assertThat(hasher.hash(new ByteArrayInputStream(STRING_TO_HASH.getBytes(StandardCharsets.UTF_8))))
                .isEqualTo(HASHED_STRING_512);
    }

    @Test
    void shouldThrowWhenHasherIsInstantiatedWithNullAlgorithm() {
        assertThatThrownBy(() -> new Hasher(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Algorithm cannot be null.");
    }
}
