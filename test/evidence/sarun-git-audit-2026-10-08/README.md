# Team Git audit evidence — 8 October 2026

This folder preserves the original `adc5798` audit and stores the later `e6172b2` refresh separately; do not overwrite the earlier evidence.

## Historical snapshot — `adc5798`

Snapshot time: 2026-10-08 22:05 ICT. Source checkout was `origin/develop` at `adc5798b05279840dc6178f4291278467c929791`. Its 121 non-merge commits and five personal-branch refs are preserved in `develop-non-merge-commits.csv` and `personal-branch-heads.csv`.

## Latest snapshot — `e6172b2`

Fetched 2026-10-08 22:46 ICT. `origin/develop` is now at `e6172b20096f7fb5487418ce278928a82b3f59ee` after PR #33 merged. Its 123 non-merge commits are recorded separately in `develop-non-merge-commits-e6172b2.csv`; refreshed branch refs/divergence are in `personal-branch-heads-e6172b2.csv`. Git author candidates in merged `develop` are Pavarit 67, Sirapat 30, Sarun 6, Teeramet 9, and Methus 11. These counts do not certify meaningfulness, identity, ownership, or member confirmation. Refresh this snapshot before Final release; retain both earlier inventories unchanged.

## Files

- `develop-non-merge-commits.csv` and `personal-branch-heads.csv`: historical `adc5798` snapshot; retained unchanged.
- `develop-non-merge-commits-e6172b2.csv` and `personal-branch-heads-e6172b2.csv`: separate refreshed snapshot after PR #33 merged.
- `member-confirmations.csv`: current confirmation register. It is intentionally pending until each member makes their own statement.

## Method and limits

Each commit inventory was generated from its corresponding fetched `origin/develop` revision:

```text
git log origin/develop --no-merges --format='@@%H%x09%an%x09%aI%x09%s' --name-only
```

The branch inventory uses the fetched `origin/<personal-branch>` refs, `git rev-parse`, `git log --no-merges`, and `git rev-list --left-right --count origin/develop...origin/<personal-branch>`. No email addresses are included.

Git author names are mapped to candidate GitHub logins using observed PR/branch authorship. This does not prove account control, individual ownership, meaningfulness, or an acceptable distribution of work over time. Candidate counts are not a pass/fail determination for the course requirement. Each member must personally confirm identity, owned work, and at least fifteen meaningful commits that have merged into `develop`, plus dates/distribution and access to the repository/evidence. Do not count commits that remain only on personal branches or open PRs toward this confirmation. The confirmation register remains `PENDING` for all members until each person provides their own statement; candidate counts and reviewer actions do not constitute confirmation.

The latest GitHub PR states and reviews are summarized in [the Git audit](../../../doc/planning/step3-git-audit.md#pr-และ-reviewer-history). PR #33 is merged; PR #34 remains open and awaiting re-review. Reviewer approval is scoped to the PR diff and does not substitute for all-member confirmation.
