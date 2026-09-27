package io.github.alexbujenita.amberfic.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CliSmokeTest {

    @Test
    void mockTest() {
        int[] test = new int[] {1, 2, 3, 4};

        assertThat(test).hasSize(4);
    }
}
