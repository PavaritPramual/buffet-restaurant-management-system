# Team Git audit evidence — 8 October 2026

Snapshot time: 2026-10-08 21:55 ICT. Source checkout is `origin/develop` at `adc5798b05279840dc6178f4291278467c929791`; personal branch refs were fetched from `origin` at the same audit session. The branch head SHAs and graph divergence are recorded in `personal-branch-heads.csv`.

## Files

- `develop-non-merge-commits.csv`: all 121 non-merge commits reachable from `origin/develop` with changed paths, full SHA, Git author name, author date, subject, and changed path list.
- `personal-branch-heads.csv`: five named personal branch heads, latest commit metadata, ahead/behind counts against `origin/develop`, and author-name candidate counts on each branch and on develop.
- `member-confirmations.csv`: confirmation register. It is intentionally pending until each member makes their own statement.

## Method and limits

The commit inventory was generated from:

```text
git log origin/develop --no-merges --format='@@%H%x09%an%x09%aI%x09%s' --name-only
```

The branch inventory uses the fetched `origin/<personal-branch>` refs, `git rev-parse`, `git log --no-merges`, and `git rev-list --left-right --count origin/develop...origin/<personal-branch>`. No email addresses are included.

Git author names are mapped to candidate GitHub logins using observed PR/branch authorship. This does not prove account control, individual ownership, meaningfulness, or an acceptable distribution of work over time. Candidate counts are not a pass/fail determination for the course requirement. Each member must confirm identity, owned work, at least five meaningful commits, dates/distribution, and access to the repository/evidence; their confirmations have not been inferred from commit metadata or reviewer actions.

The GitHub PR states and reviews current at snapshot time are summarized in [the Git audit](../../../doc/planning/step3-git-audit.md#pr-และ-reviewer-history). PR #33 and #34 were open; their reviews are scoped to their respective diff and do not substitute for all-member confirmation.
