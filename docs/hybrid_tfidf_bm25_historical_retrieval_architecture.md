# Hybrid Historical Retrieval Architecture
## Combining TF-IDF and BM25 for Robust Historical-Case Prediction

**Document status:** Proposed technical architecture  
**Purpose:** Improve historical-case prediction across datasets without overfitting retriever weights or score thresholds to limited test samples.  
**Core principle:** Treat TF-IDF and BM25 as independent retrieval judges, combine their rankings and evidence patterns, and optimize for reliable prediction rather than raw score magnitude.

---

# 1. Executive Summary

This architecture proposes a hybrid historical retrieval design using both **TF-IDF + cosine similarity** and **BM25**.

The objective is not to decide which method is universally better. The two retrievers capture different forms of textual similarity and can behave differently across datasets. The architecture therefore uses both as independent evidence sources.

The design is:

```text
New input
   |
Multiple text views
   |
+-------------------+
|                   |
TF-IDF              BM25
|                   |
Historical ranking  Historical ranking
|                   |
+---------+---------+
          |
   Label-level fusion
          |
 Cross-view agreement
          |
 Evidence stability
          |
+---------+---------+
|                   |
PREDICT             ABSTAIN
```

Key principles:

- Do not choose between TF-IDF and BM25 prematurely.
- Do not add their raw scores directly.
- Fuse rankings rather than score magnitudes.
- Aggregate evidence at output-label level.
- Use multiple message fragments, not only whole-message similarity.
- Treat agreement as positive evidence.
- Treat disagreement as uncertainty.
- Optimize for robustness across datasets, not best performance on one dataset.

---

# 2. Why Both Retrievers Should Coexist

TF-IDF with cosine similarity emphasizes weighted term overlap and sparse-vector similarity. BM25 emphasizes term importance, term-frequency saturation, document-length normalization, and retrieval relevance.

Because they score text differently, one can outperform the other on a particular dataset without either being universally superior. Their disagreement is therefore useful information rather than a reason to immediately discard one method.

---

# 3. Core Architectural Decision

Do **not** create a final score such as:

```text
TF-IDF score * weight A
+
BM25 score * weight B
```

The two scores are on different scales. A manually tuned blend can easily overfit limited regression data.

Instead combine:

```text
rank
agreement
label support
historical-neighbor purity
input-fragment coverage
winner/runner-up separation
cross-view stability
```

---

# 4. Multi-View Input Representation

A runtime message should be represented through multiple views:

```text
whole message
individual sentences
clauses
two-sentence windows
three-sentence windows
selected short phrase / shingle views
```

Both TF-IDF and BM25 operate on these views. The architecture then asks whether the same output continues to appear across independent representations of the input.

---

# 5. Independent Historical Retrieval

TF-IDF and BM25 independently retrieve similar resolved historical cases.

Each path produces its own ranked historical neighborhood. If multiple fragments from the same historical case are retrieved, they must be collapsed back to the parent case so that one case cannot create several artificial votes.

The unit of evidence is therefore a **distinct historical case**, not a raw fragment.

---

# 6. Aggregate at Label Level

The objective is not to identify the single closest historical message. It is to determine which known output label has the strongest historical neighborhood.

Example:

```text
Case 17 -> LABEL_X
Case 35 -> LABEL_X
Case 91 -> LABEL_X
Case 44 -> LABEL_Y
Case 72 -> LABEL_X
Case 83 -> LABEL_Z
```

The useful result is:

```text
LABEL_X -> 4 distinct supporting cases
LABEL_Y -> 1
LABEL_Z -> 1
```

TF-IDF and BM25 should each independently convert historical-case rankings into output-label rankings before fusion.

---

# 7. Rank Fusion Instead of Score Fusion

Because TF-IDF cosine and BM25 scores are not directly comparable, the architecture should use ranking-based fusion such as **Reciprocal Rank Fusion (RRF)**.

The purpose is straightforward: labels that consistently rank well across independent retrievers should rise, without requiring arbitrary score normalization or hand-selected blending weights.

---

# 8. Three Decision Lanes

## 8.1 Strong Consensus

Both retrievers independently select the same label, multiple distinct historical cases support it, multiple runtime fragments support it, and the runner-up is clearly weaker.

This is the safest prediction population and should be optimized primarily for minimizing wrong predictions.

## 8.2 One Strong, One Neutral

One retriever strongly supports a label while the other is mixed or weak, but does not strongly support a competing label.

This is materially different from true disagreement and can later become a valid prediction lane if regression demonstrates that the pattern is reliable.

## 8.3 True Conflict

TF-IDF strongly supports one label while BM25 strongly supports another, with both having substantial historical evidence.

This should be treated as unstable evidence. The default decision should be **ABSTAIN** unless regression proves a dependable resolution pattern.

---

# 9. Multi-Fragment Agreement

Whole-message disagreement should not automatically determine the result.

Example:

```text
                    TF-IDF       BM25
Whole message          A           B
Sentence 2             A           A
Sentence 2+3           A           A
Sentence 3             A           A
Sentence 3+4           A           C
```

The overall evidence is strongly tilted toward A because multiple independent fragments and both retrievers repeatedly support it.

If the winners change repeatedly across message views, the evidence is unstable and the system should abstain.

---

# 10. Historical Neighbor Purity

For each retriever, measure how consistently the strongest historical neighborhood supports the same label.

Example:

```text
TF-IDF top 10: 8 A, 1 B, 1 C
BM25 top 10:  7 A, 2 B, 1 C
```

This is stronger evidence than simply observing that both top-1 results are A.

Purity can be evaluated across top-5, top-10, and top-20 neighborhoods to understand stability.

---

# 11. Candidate Separation

For both retrievers and for the fused label ranking, compare the winner against the runner-up using:

```text
distinct historical-case support
neighbor purity
fragment coverage
rank-fusion position
cross-view agreement
```

A clearly separated winner is safer than a marginal winner.

---

# 12. Confidence Should Be Evidence-Based

Do not define confidence using a rule such as:

```text
cosine >= 0.90
BM25 >= X
```

Instead classify the evidence state:

```text
STRONG CONSENSUS
MODERATE CONSENSUS
ONE-STRONG-ONE-NEUTRAL
TRUE CONFLICT
SPARSE HISTORY
UNSTABLE ACROSS WINDOWS
```

As regression data grows, measure the real correctness of each evidence state and convert that into empirical reliability.

Prediction and confidence therefore remain separate concepts: a label may be the best-ranked candidate without being safe enough to automate.

---

# 13. Relationship with the Existing Semantic Pipeline

The historical hybrid should first be proven independently.

Recommended evaluation order:

```text
A. TF-IDF historical retrieval
B. BM25 historical retrieval
C. TF-IDF + BM25 hybrid historical retrieval
D. Later: hybrid historical + semantic corroboration
```

The general semantic pipeline should initially remain outside this comparison. Once the historical hybrid is understood, semantics can be reintroduced as another independent corroborating signal rather than as a score that automatically dilutes the historical result.

---

# 14. Evaluation Metrics

Every frozen configuration should be run unchanged across every available dataset and measured using:

```text
correct predictions
wrong predictions
abstained cases
prediction coverage
prediction precision
manual interventions after prediction
net manual-intervention reduction
```

The business metric is more important than raw top-1 accuracy.

---

# 15. Optimize for Cross-Dataset Robustness

Do not select a retriever because it wins one dataset.

Evaluate:

```text
average performance
worst-case dataset performance
variance across datasets
wrong-prediction stability
coverage stability
manual-intervention reduction stability
```

A hybrid that is not the best method on any one dataset can still be the preferred production architecture if it materially improves the worst-case result and reduces variance.

---

# 16. Avoid Dataset-Specific Overfitting

The architecture should deliberately avoid:

```text
per-dataset score thresholds
aggressive TF-IDF/BM25 weighting
continuous parameter changes after each smoke test
manual blend ratios chosen from one small regression set
```

Prefer structural signals that are more likely to generalize:

```text
rank fusion
retriever agreement
label purity
distinct-case support
cross-window stability
winner/runner-up separation
```

---

# 17. Class Imbalance and Duplicate Controls

Historical labels may have very different numbers of examples. Raw vote counts can therefore be biased toward large classes.

The design should control for:

```text
per-label imbalance
duplicate historical messages
repeated templates
fragment-count inflation
```

Useful controls include normalization, neighborhood purity, duplicate clustering, and contribution caps for near-identical cases.

Independent support is more valuable than repeated copies.

---

# 18. Cross-Dataset Regression Strategy

Every candidate architecture should be frozen and run unchanged across fixed regression datasets.

Do not tune TF-IDF after observing one dataset and BM25 after observing another and then compare those tuned versions.

Recommended ablation sequence:

```text
1. TF-IDF whole-message only
2. BM25 whole-message only
3. TF-IDF multi-view
4. BM25 multi-view
5. TF-IDF + BM25 rank fusion
6. + distinct-case collapse
7. + label purity
8. + runtime-region coverage
9. + cross-view stability
10. + near-duplicate channel
11. later: + semantic corroboration
```

This shows which architectural additions genuinely improve robustness.

---

# 19. Decision-State Analysis

Instead of analysing only numeric score bands, analyse structural evidence states:

```text
both retrievers agree strongly
both agree weakly
TF-IDF strong / BM25 neutral
BM25 strong / TF-IDF neutral
strong disagreement
sparse historical support
multiple windows agree
multiple windows disagree
```

Then determine which states remain reliable across datasets.

---

# 20. Recommended Architecture

```text
                    INPUT
                      |
              MULTIPLE TEXT VIEWS
                      |
          +-----------+-----------+
          |                       |
      TF-IDF                    BM25
          |                       |
          v                       v
   Historical cases       Historical cases
          |                       |
          v                       v
     Label ranking           Label ranking
          |                       |
          +-----------+-----------+
                      |
                RANK FUSION
                      |
              LABEL CONSENSUS
                      |
             CROSS-VIEW STABILITY
                      |
          DISTINCT-CASE VALIDATION
                      |
               PURITY / MARGIN
                      |
               +------+------+
               |             |
            PREDICT        ABSTAIN
```

---

# 21. Final Architectural Position

The system should not ask:

> Which is better, TF-IDF or BM25?

It should ask:

> **When two different retrieval methods examine the same historical knowledge, how consistently do they arrive at the same output?**

Their agreement becomes evidence. Their disagreement becomes uncertainty.

The goal is to create the largest prediction population that remains robust across datasets while keeping wrong predictions controlled.

---

# 22. Recommended Next Step

Freeze and compare:

```text
TF-IDF historical
BM25 historical
hybrid TF-IDF + BM25 historical
```

across all available datasets using identical evaluation logic.

Judge the hybrid primarily on:

```text
worst-dataset performance
wrong-prediction stability
manual-intervention reduction
coverage stability
```

Only after that baseline is established should the existing semantic predictor be added back as another independent signal.

---

# 23. One-Sentence Architecture Summary

> **Run TF-IDF and BM25 as independent historical retrievers over multiple views of the runtime message, aggregate each retriever's evidence at label level, combine rankings through consensus rather than raw-score blending, and predict only where historical support remains stable across retrievers, message fragments, and competing labels.**
