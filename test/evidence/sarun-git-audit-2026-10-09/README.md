# Team Git audit evidence — 9 October 2026

## Current snapshot

This directory retains multiple point-in-time snapshots. The original `d84f071` files are historical: `origin/develop` was `d84f071ee03b738d6a6dd2a899c5f6e6cc230d3f` with 137 non-merge commits when captured at 10:31 ICT. The refreshed `a6da906` files record refs fetched on 9 October 2026 at 12:51 ICT, after PR #36 merged; `origin/develop` was `a6da906bfb7d3c2d7a9bb3e8b5601a7886219bcc` with 142 non-merge commits. Each inventory contains commit SHA, Git author name, author date, subject, and changed paths. The corresponding branch inventory records all five personal branch refs, candidate-author counts, and graph ahead/behind relative to the same `origin/develop`.

Sarun's remote branch ref in the refreshed snapshot was `4da632a`; it predates the subsequent merge of latest `develop` into PR #37. Open PR #37 commits are not counted as merged `develop` work.

Earlier snapshots in [`../sarun-git-audit-2026-10-08/`](../sarun-git-audit-2026-10-08/) remain unchanged and historical.

## Files

- `develop-non-merge-commits-a6da906.csv`: the current 142 non-merge commits reachable from `origin/develop a6da906`.
- `personal-branch-heads-a6da906.csv`: five fetched personal-branch heads and their graph divergence from the same `origin/develop`.
- `develop-non-merge-commits-d84f071.csv` and `personal-branch-heads-d84f071.csv`: historical pre-PR #36 snapshot; retained unchanged.
- [`member-confirmations.csv`](../sarun-git-audit-2026-10-08/member-confirmations.csv): existing member confirmation register; all five remain pending.

## Method and limits

The inventory was generated from fetched Git refs using `git log --no-merges` and records changed paths for each commit with rename detection. The branch comparison uses `git rev-parse`, `git log --no-merges`, and `git rev-list --left-right --count origin/develop...origin/<personal-branch>`. Email addresses are intentionally omitted.

Git author names are candidates, not proof of GitHub account control, identity, work ownership, meaningfulness, acceptable distribution over time, or course completion. For `a6da906`, merged-develop author candidates are Pavarit 67, Sirapat 35, Sarun 11, Teeramet 18, and Methus 11. Meeting the numeric threshold is not a pass by itself; the five members must each confirm their own identity, meaningful merged work, ownership, distribution, and repository/evidence access. The existing confirmation register remains `PENDING` for all five. Do not count open-PR commits toward the requirement or create commits to inflate totals.

PR status and reviewer history are summarized in the [current Git audit](../../../doc/planning/step3-git-audit.md#pr-และ-reviewer-history). This Git snapshot does not establish public deployment revision or Final release acceptance.
