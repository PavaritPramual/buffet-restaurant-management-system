# Team Git audit evidence — 9 October 2026

## Current snapshot

This dated snapshot records fetched repository refs as inspected on 9 October 2026 at 10:31 ICT. `origin/develop` was `d84f071ee03b738d6a6dd2a899c5f6e6cc230d3f`, with 137 non-merge commits. The separate inventory contains commit SHA, Git author name, author date, subject, and changed paths. The branch inventory records each personal branch ref, candidate-author counts, and ahead/behind counts relative to `origin/develop`.

Sarun's remote branch ref at capture time was `aa5d453`. Subsequent commits in this documentation PR are after this snapshot and are not included in its branch-head row or counted as merged `develop` work.

Earlier snapshots in [`../sarun-git-audit-2026-10-08/`](../sarun-git-audit-2026-10-08/) remain unchanged and historical.

## Files

- `develop-non-merge-commits-d84f071.csv`: the 137 non-merge commits reachable from `origin/develop` at the recorded revision.
- `personal-branch-heads-d84f071.csv`: five personal-branch heads and their graph divergence from `origin/develop`.
- [`member-confirmations.csv`](../sarun-git-audit-2026-10-08/member-confirmations.csv): existing member confirmation register; all five remain pending.

## Method and limits

The inventory was generated from fetched Git refs using `git log --no-merges` and records changed paths for each commit. The branch comparison uses `git rev-parse`, `git log --no-merges`, and `git rev-list --left-right --count origin/develop...origin/<personal-branch>`. Email addresses are intentionally omitted.

Git author names are candidates, not proof of GitHub account control, identity, work ownership, meaningfulness, acceptable distribution over time, or course completion. The merged-develop author candidate totals are Pavarit 67, Sirapat 30, Sarun 11, Teeramet 18, and Methus 11. Meeting the numeric threshold is not a pass by itself; the five members must each confirm their own identity, meaningful merged work, ownership, distribution, and repository/evidence access. The existing confirmation register remains `PENDING` for all five. Do not count open-PR commits toward the requirement or create commits to inflate totals.

PR status and reviewer history are summarized in the [current Git audit](../../../doc/planning/step3-git-audit.md#pr-และ-reviewer-history). This Git snapshot does not establish public deployment revision or Final release acceptance.
