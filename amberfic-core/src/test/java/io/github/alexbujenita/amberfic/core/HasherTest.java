package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.github.alexbujenita.amberfic.core.util.ShortReadInputStream;
import io.github.alexbujenita.amberfic.core.util.ZeroInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HasherTest {

    private static final String STRING_TO_HASH = "abc";
    private static final String HASHED_STRING_512 =
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f";
    private static final long SIZE_50MB = 50L * 1024 * 1024;

    public static Stream<Arguments> provideParams() {
        return Stream.of(
                arguments(
                        "simple 256",
                        HashAlgorithm.SHA256,
                        STRING_TO_HASH,
                        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
                arguments(
                        "empty 256",
                        HashAlgorithm.SHA256,
                        "",
                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                arguments(
                        "a x 1_000_000 256",
                        HashAlgorithm.SHA256,
                        "a".repeat(1_000_000),
                        "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0"),
                arguments("simple 512", HashAlgorithm.SHA512, STRING_TO_HASH, HASHED_STRING_512),
                arguments(
                        "a x 1_000_000 512",
                        HashAlgorithm.SHA512,
                        "a".repeat(1_000_000),
                        "e718483d0ce769644e2e42c7bc15b4638e1f98b13b2044285632a803afa973ebde0ff244877ea60a4cb0432ce577c31beb009c5c2c49aa2e4eadb217ad8cc09b"),
                arguments(
                        "empty 512",
                        HashAlgorithm.SHA512,
                        "",
                        "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"));
    }

    public static Stream<Arguments> provideBigStreams() {
        return Stream.of(
                arguments(
                        "big stream 512",
                        HashAlgorithm.SHA512,
                        SIZE_50MB,
                        "50173936f83822c28812ea23f7f843a27e8b85f65626cbc339cea0e63aa770809f932343af9e6a0cd26bfa490d6d1928f19cf529ebd1e76ef74264fda961f810"),
                arguments(
                        "big stream 256",
                        HashAlgorithm.SHA256,
                        SIZE_50MB,
                        "8565a714dca840f8652c5bae9249ab05f5fb5a4f9f13fbe23304b10f68252da2"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideParams")
    void shouldCorrectlyHash(String name, HashAlgorithm hashAlgorithm, String toHash, String expectedHash)
            throws IOException {
        // given an input stream and a hasher instance
        final InputStream streamToHash = new ByteArrayInputStream(toHash.getBytes(StandardCharsets.UTF_8));
        final Hasher hasher = new Hasher(hashAlgorithm);

        // when we call the hash method
        final String hash = hasher.hash(streamToHash);

        // then the computed hash is correct
        assertThat(hash).isEqualTo(expectedHash);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBigStreams")
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void shouldCorrectlyHashBigStreams(String name, HashAlgorithm hashAlgorithm, long size, String expectedHash)
            throws IOException {
        // given a hasher instance
        final Hasher hasher = new Hasher(hashAlgorithm);

        // when we call the hash method
        final String hash = hasher.hash(new ZeroInputStream(size));

        // then the computed hash is correct
        assertThat(hash).isEqualTo(expectedHash);
    }

    @Test
    void shouldCorrectlyHashWhenReadsAreShort() throws IOException {
        // given the hash fo three zero byte
        final String hash512 =
                "6d518f8b31d1882feace10a9215f5d8cf5afe037652a1d11d9c1408d988c2a4f71a5edfc85d0712fa3f4e21b2c0a244c8c0d333bab454311e24067d2a83e5e59";
        final Hasher hasher = new Hasher();

        // when the hash method is called with size 3
        final String hash = hasher.hash(new ShortReadInputStream(3));

        // then the resulting hash is correct
        assertThat(hash).isEqualTo(hash512);
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
