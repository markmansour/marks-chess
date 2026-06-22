# Performance Testing

Performance work is tracked with two kinds of signal, kept deliberately separate because they have
very different reliability:

| Signal | Example | Reliability | How it is used |
|--------|---------|-------------|----------------|
| **Node counts** (nodes to reach a depth, perft counts) | hash-move ordering, perft correctness | Deterministic, machine-independent | **Strict gate** in the normal test suite |
| **Throughput / time** (nodes/second) | perft nodes/sec | Noisy, machine-dependent | **Trend with a soft alert**, never a hard gate |

The search is deterministic: move ordering is deterministic, evaluation is pure integer arithmetic,
and the only randomness (tie-breaking among equal root moves) is applied to the move *returned*,
after the search, so it never changes the tree. Therefore node counts depend only on the code, not
on the JDK or CPU. That makes them ideal for exact regression gates that run anywhere with no
flakiness. Wall-clock throughput is the opposite and is only meaningful as a trend on a stable
machine.

## What runs where

Performance code is tagged `@Tag("PerformanceTest")` and is **excluded from the normal unit run**.
The default `mvn test` runs only `@Tag("UnitTest")`. Override the group to run performance tests:

```bash
mvn test -Dtest.groups=PerformanceTest -Djacoco.skip=true
```

(`-Djacoco.skip=true` avoids a JaCoCo instrumentation failure under newer JDKs;
CI runs on JDK 17 where it is not needed.)

### Deterministic gates (run on every PR)

- **`HashMoveOrderingRegressionTest`** (`UnitTest`) — reads the committed baseline and fails if the
  search visits more than 2% more nodes than the baseline for any position. It never writes the
  baseline. Improvements pass; re-record to lock them in. Runs in the normal suite (~1s, no
  flakiness) so every PR is gated by `maven.yml`.
- **`DepthTest`** (`PerformanceTest`) — perft correctness against a 126-position reference suite.

### Recorders and benchmarks (run on demand)

- **`HashMoveOrderingBenchmark`** (`PerformanceTest`) — A/B measurement of hash-move ordering
  (issue #6). Runs each position with ordering on and off in one process and writes:
  - `perf-results/baseline.csv` (**committed**): `position,depth,nodes_on,nodes_off`. Deterministic,
    so re-recording on unchanged code reproduces it byte-for-byte — no churn. Its git history is the
    performance trend.
  - `perf-results/history.csv` (gitignored): timestamped node counts and timings.

  ```bash
  mvn test -Dtest.groups=PerformanceTest -Dtest=HashMoveOrderingBenchmark -Djacoco.skip=true
  ```

- **`PerftBenchmark`** (`PerformanceTest`) — move-generation throughput. Asserts the exact perft
  counts (correctness) and measures nodes/second, writing `perf-results/perft-bench.json`
  (gitignored) in github-action-benchmark format.

  ```bash
  mvn test -Dtest.groups=PerformanceTest -Dtest=PerftBenchmark -Djacoco.skip=true
  ```

## Measuring an optimization A/B

`AlphaBetaPlayerWithTT.setHashMoveOrdering(boolean)` (default on) lets you run the same search with
an optimization on and off **in one JVM**, which cancels cross-build and cross-JVM noise. This is
how `HashMoveOrderingBenchmark` quantifies the gain. Use the same pattern for future ordering or
pruning experiments: add a toggle, A/B it, compare node counts.

## Updating a baseline after an intentional change

When you intentionally change search behaviour and node counts move:

1. Re-run the recorder: `mvn test -Dtest.groups=PerformanceTest -Dtest=HashMoveOrderingBenchmark -Djacoco.skip=true`
2. Review the diff: `git diff perf-results/baseline.csv` — confirm the change is expected.
3. Commit it. The commit *is* the record of "this change moved node counts by N".

`git log -p perf-results/baseline.csv` shows the full history.

## CI

- **`maven.yml`** — builds, runs the unit suite (including the node-count regression gate), and
  publishes the coverage badge. Gates every push and PR to `main`.
- **`perf.yml`** — runs `PerftBenchmark` on pushes to `main` (and on demand via *Run workflow*) and
  feeds the throughput JSON to
  [github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark). Results
  are stored on the `gh-pages` branch and charted at
  `https://markmansour.github.io/marks-chess/dev/bench/`. A throughput drop below ~67% of the prior
  result posts an alert comment; it does not fail the build, because GitHub-hosted runners are too
  noisy to gate on wall-clock time.

The dashboard appears after the first successful `perf.yml` run on `main`.
