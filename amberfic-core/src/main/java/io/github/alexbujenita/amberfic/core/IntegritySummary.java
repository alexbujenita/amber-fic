package io.github.alexbujenita.amberfic.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class IntegritySummary {
    private final EnumMap<SummaryOutcome, Long> summary;

    private IntegritySummary(EnumMap<SummaryOutcome, Long> summary) {
        this.summary = summary;
    }

    static IntegritySummary of(List<IntegrityCheckResult> results) {
        final EnumMap<SummaryOutcome, Long> summary = new EnumMap<>(SummaryOutcome.class);

        for (SummaryOutcome summaryOutcome : SummaryOutcome.values()) {
            summary.put(summaryOutcome, 0L);
        }

        results.forEach(result -> {
            switch (result) {
                case IntegrityCheckResult.Passed _ -> summary.merge(SummaryOutcome.PASSED, 1L, Long::sum);
                case IntegrityCheckResult.Failed _ -> summary.merge(SummaryOutcome.FAILED, 1L, Long::sum);
                case IntegrityCheckResult.MissingFromManifest _ ->
                    summary.merge(SummaryOutcome.MISSING_FROM_MANIFEST, 1L, Long::sum);
                case IntegrityCheckResult.MissingFromScan _ ->
                    summary.merge(SummaryOutcome.MISSING_FROM_SCAN, 1L, Long::sum);
                case IntegrityCheckResult.Unreadable _ -> summary.merge(SummaryOutcome.UNREADABLE, 1L, Long::sum);
            }
        });
        return new IntegritySummary(summary);
    }

    Map<SummaryOutcome, Long> counts() {
        return Collections.unmodifiableMap(summary);
    }
}
