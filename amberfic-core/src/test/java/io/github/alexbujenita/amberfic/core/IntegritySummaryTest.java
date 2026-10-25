package io.github.alexbujenita.amberfic.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IntegritySummaryTest {

    @Test
    void countsEachStatus() {
        // given a mixed results
        final List<IntegrityCheckResult> results = List.of(
                new IntegrityCheckResult.Failed("a.png"),
                new IntegrityCheckResult.Passed("b.png"),
                new IntegrityCheckResult.MissingFromManifest("c.png", "Irrelevant_Hash"),
                new IntegrityCheckResult.Passed("d.png"),
                new IntegrityCheckResult.Unreadable("aa.png"),
                new IntegrityCheckResult.Passed("ab.png"),
                new IntegrityCheckResult.Passed("ac.png"),
                new IntegrityCheckResult.Failed("ad.png"),
                new IntegrityCheckResult.Unreadable("x.png"),
                new IntegrityCheckResult.Unreadable("xyz.png"));

        final Map<SummaryOutcome, Long> expected = Map.of(
                SummaryOutcome.PASSED, 4L,
                SummaryOutcome.FAILED, 2L,
                SummaryOutcome.MISSING_FROM_MANIFEST, 1L,
                SummaryOutcome.MISSING_FROM_SCAN, 0L,
                SummaryOutcome.UNREADABLE, 3L);

        // when we get the summary
        final IntegritySummary integritySummary = IntegritySummary.of(results);

        // then it matches the expected
        assertThat(integritySummary.counts()).isEqualTo(expected);
    }
}
