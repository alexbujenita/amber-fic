package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SmokeTest {

    @Test
    void mockTest() {
        int[] test = new int[] {1, 2, 3};

        assertThat(test).hasSize(3);
    }
}
