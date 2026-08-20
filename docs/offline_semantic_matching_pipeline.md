# Offline Semantic Matching Pipeline
## High-Precision Model-Free Mapping of Arbitrary English Input to a Fixed Candidate Set

**Status:** Proposed implementation architecture  
**Primary runtime:** Java  
**Candidate universe:** ~60–70 predefined target words / phrases  
**Typical runtime input:** arbitrary English text, commonly ~100–150 words  
**Primary business goal:** maximize correct matches while keeping false positives below 10 per 100 inputs  
**Preferred error mode:** `UNKNOWN` is acceptable; a wrong match is worse than abstention  

---

# 1. Executive Summary

The system receives arbitrary English text and must map that text to one of approximately 60–70 known target terms or phrases.

The runtime text is uncontrolled. It may:

- use words never seen during development;
- express the same idea with synonyms or paraphrases;
- contain irrelevant background information;
- mention several competing concepts;
- contain the important evidence in one short section;
- scatter the important evidence across the entire 100–150-word input;
- never contain the exact canonical phrase.

The solution must run completely offline and may not use:

- LLMs;
- embedding models;
- sentence transformers;
- ML classifiers;
- neural inference;
- external HTTP/API calls.

The solution **may** use local English-language knowledge resources and deterministic/statistical algorithms, including:

- WordNet / Open English WordNet;
- Wiktionary-derived lexical data;
- offline Wikipedia-derived corpora/indexes;
- offline ConceptNet subsets;
- Lucene;
- BM25;
- TF-IDF;
- graph traversal;
- edit distance;
- n-grams;
- deterministic word-sense disambiguation;
- corpus co-occurrence statistics.

The recommended design is not a single similarity algorithm. It is a **high-precision semantic retrieval and evidence-fusion pipeline**.

The final architecture is:

```text
                       INPUT: 100–150 WORDS
                               |
                               v
                     Text Normalization
                               |
                               v
                  Sentence / Clause Segmentation
                               |
                               v
                     Multi-Scale Windows
                               |
          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
      Lexical /             WordNet /          Offline Corpus /
      Phrase Engine         OEWN Engine         ESA/BM25 Engine
          |                    |                    |
          +--------------------+--------------------+
                               |
                               v
                    Candidate Evidence Graph
                               |
               +---------------+----------------+
               |                                |
               v                                v
        Local Evidence                   Distributed Evidence
               |                                |
               +---------------+----------------+
                               |
                               v
                  Contrastive Candidate Scoring
                               |
                               v
                     Evidence Fusion Layer
                               |
                               v
                 Confidence / Stability Gates
                               |
                        +------+------+
                        |             |
                        v             v
                      MATCH         UNKNOWN
```

The design target for an initial mature version should be approximately:

```json
{
  "total_inputs": 100,
  "correct_matches": 70,
  "false_positives": 6,
  "unknown": 24,
  "accepted_predictions": 76,
  "accepted_precision_percent": 92.1,
  "overall_correct_coverage_percent": 70.0
}
```

This is an **engineering target**, not a guaranteed benchmark.

A useful expected operating range is:

| Metric per 100 inputs | Conservative | Target | Stretch |
|---|---:|---:|---:|
| Correct matches | 62 | 70 | 74 |
| False positives | 5 | 6 | 7 |
| UNKNOWN | 33 | 24 | 19 |
| Accepted precision | 92.5% | 92.1% | 91.4% |

The system must always be calibrated using real labelled production-like inputs before these numbers are treated as operational expectations.

---

# 2. Problem Definition

## 2.1 Known side

The application has approximately:

```text
60–70 canonical targets
```

Examples:

```text
PAYMENT
PAYMENT_STATUS
PAYMENT_INVESTIGATION
CANCEL_PAYMENT
REFUND
BENEFICIARY
ACCOUNT_DETAILS
ADDRESS_CHANGE
...
```

The implementation should not depend on manually maintained synonym lists for each target.

---

## 2.2 Runtime side

Runtime input is unrestricted English.

Typical size:

```text
100–150 words
```

Example:

```text
The customer contacted us regarding an instruction sent yesterday.
The debit was completed from the source account.
The receiving party has advised that nothing has arrived.
The original instruction shows as completed in the system.
The customer wants us to establish where the funds are.
```

Desired output:

```text
PAYMENT_INVESTIGATION
```

The exact phrase `payment investigation` may never appear.

---

## 2.3 Output contract

The system must return either:

```text
MATCH
```

with:

```json
{
  "candidate": "PAYMENT_INVESTIGATION",
  "confidence": 0.91,
  "second_best": "PAYMENT_STATUS",
  "second_best_score": 0.63,
  "margin": 0.28,
  "evidence": [
    "receiving party has advised that nothing has arrived",
    "customer wants us to establish where the funds are"
  ]
}
```

or:

```json
{
  "result": "UNKNOWN"
}
```

`UNKNOWN` is not a failure. It is an intentional safety outcome.

---

# 3. Business Objective

The key objective is **not maximum classification rate**.

The priority order is:

```text
1. Correct match
2. UNKNOWN
3. Wrong match
```

Therefore:

```text
precision > recall
```

is the correct optimization strategy.

The explicit target is:

```text
>= 70 correct predictions per 100 representative inputs
< 10 false positives per 100 representative inputs
```

A preferred working point is:

```text
70 correct
6 false positives
24 UNKNOWN
```

---

# 4. Why Plain Cosine Similarity Is Not Enough

Cosine similarity only compares vectors.

It does not itself know that:

```text
pay
payment
remit
send money
transfer funds
```

may refer to related ideas.

If vectors are built only from raw words:

```text
payment
```

versus:

```text
send the money
```

may have zero token overlap.

Therefore the core problem is not:

```text
Which similarity formula should be used?
```

The core problem is:

```text
How do we construct semantic evidence without an ML model?
```

Cosine may still be used inside one component, but it should not be treated as the architecture.

---

# 5. Core Design Principle: Import Knowledge, Not a Model

The runtime is prohibited from calling an LLM or ML model.

That does **not** mean the application must operate without language knowledge.

Instead, semantic knowledge should come from static offline resources:

```text
WordNet / Open English WordNet
Wikipedia
Wiktionary
ConceptNet
English corpora
```

These provide:

- synonyms;
- lexical senses;
- hypernyms;
- hyponyms;
- derivational relationships;
- definitions;
- concept relationships;
- corpus context;
- co-occurrence;
- multi-word expressions.

The application then uses deterministic algorithms to extract and score evidence.

---

# 6. Pipeline Overview

The final pipeline contains ten major stages.

```text
1. Normalize input
2. Segment into sentences and clauses
3. Build multiple text windows
4. Extract lexical / phrase evidence
5. Extract WordNet semantic evidence
6. Extract offline-corpus semantic evidence
7. Aggregate local and distributed evidence
8. Score candidates contrastively
9. Fuse independent evidence channels
10. Apply precision gates and return MATCH / UNKNOWN
```

---

# 7. Stage 1 — Text Normalization

Input normalization should include:

```text
Unicode normalization
lowercasing
punctuation normalization
sentence boundary detection
tokenization
lemmatization
light stop-word filtering
contraction normalization
negation preservation
number normalization where relevant
```

Example:

```text
"The customer doesn't want the transfer to proceed."
```

should become conceptually:

```text
customer
NOT
want
transfer
proceed
```

Do not remove negation words.

Important tokens:

```text
not
never
without
cannot
don't
isn't
failed
declined
missing
```

may completely reverse meaning.

---

# 8. Stage 2 — Sentence and Clause Segmentation

A 100–150-word input must never be treated only as one document.

Split into:

```text
sentences
clauses
```

Example:

```text
The payment was processed yesterday, but the customer says the beneficiary has not received it.
```

should ideally expose:

```text
payment was processed yesterday
beneficiary has not received it
```

because the second clause may contain the actual intent signal.

---

# 9. Stage 3 — Multi-Scale Windows

Build overlapping windows.

Recommended units:

```text
individual clause
individual sentence
2 adjacent sentences
3 adjacent sentences
paragraph
whole document
```

Example:

```text
S1
S2
S3
S4
S5
```

Generate:

```text
S1
S2
S3
S4
S5

S1+S2
S2+S3
S3+S4
S4+S5

S1+S2+S3
S2+S3+S4
S3+S4+S5

whole document
```

Why this matters:

The input may contain a historical mention of `payment` but the real request may be `address change`.

Whole-document scoring alone can over-weight historical/background concepts.

Multi-scale scoring helps distinguish:

```text
background mention
```

from:

```text
local concentrated intent
```

---

# 10. Stage 4 — Lexical and Phrase Evidence Engine

This engine should be the highest-precision channel.

Use:

```text
exact token match
lemma match
stem match
multi-word phrase match
character n-grams
edit distance
Jaro-Winkler
token overlap
BM25 lexical retrieval
```

Examples:

```text
payment
payments
paymnt
payment request
```

should produce strong lexical evidence.

Phrase generation should include:

```text
unigrams
bigrams
trigrams
```

Example:

```text
send money
stop payment
receive funds
change address
account status
```

Phrase-level evidence is much stronger than treating the words independently.

---

# 11. Stage 5 — WordNet / Open English WordNet Engine

This component provides lexical-semantic knowledge.

For every candidate and every meaningful runtime token/phrase, inspect:

```text
synsets
synonyms
hypernyms
hyponyms
derivationally related forms
similar-to relationships
verb/noun relationships
antonyms
definitions / glosses
usage examples
```

Example:

```text
payment
    ->
pay
    ->
remit
```

may create a meaningful semantic path.

Relationship types must be weighted differently.

Suggested starting weights:

```text
exact lemma                     1.00
same synset                     0.98
direct synonym                  0.95
derivational relation           0.92
similar-to                      0.87
direct hypernym/hyponym         0.78
2-hop semantic relation         0.62
3-hop semantic relation         0.40
weak graph relation             0.20
```

These are calibration starting points only.

---

# 12. Word-Sense Disambiguation

Blindly expanding every WordNet sense can produce major false positives.

Example:

```text
bank
```

could mean:

```text
financial institution
river bank
aircraft banking
```

Use a deterministic Lesk-style algorithm.

For each possible sense:

1. retrieve its definition;
2. retrieve examples;
3. retrieve neighboring synsets;
4. compare them against surrounding words in the input;
5. select or weight the sense with the strongest contextual overlap.

Example input:

```text
The customer deposited money into the bank account.
```

Context:

```text
customer
deposit
money
account
```

should strongly favor the financial sense.

---

# 13. Stage 6 — Gloss / Definition Matching

Definitions are valuable because they can bridge paraphrases.

Example canonical target:

```text
REFUND
```

Input:

```text
The customer wants the money given back.
```

Even if direct synonym links are weak, dictionary definitions around:

```text
return
money
repay
repayment
```

may overlap with the runtime input.

Create a `gloss profile` for each target using:

```text
definitions
examples
definitions of related synsets
```

Then score input tokens against this profile using:

```text
BM25
TF-IDF
weighted token overlap
```

---

# 14. Stage 7 — Offline Corpus Semantic Engine

This is the biggest improvement beyond a WordNet-only solution.

WordNet is excellent for lexical relations but weaker for broader conceptual paraphrases.

Example:

```text
PAYMENT
```

versus:

```text
send funds to the receiving party
```

The words may not be direct synonyms.

A large English corpus can supply contextual relationships.

Recommended offline corpus sources:

```text
Wikipedia
Wiktionary
selected general-English corpus
```

Two useful techniques are recommended.

---

## 14.1 Explicit Semantic Analysis Style Index

Build an offline inverted index where English concepts/articles become semantic dimensions.

Conceptually:

```text
payment
 ->
financial transaction
money
banking
fund transfer
settlement
remittance
```

Input:

```text
send the funds to another account
```

may activate similar corpus concepts.

A candidate and runtime window can therefore be compared in **concept space**, not merely word space.

Possible implementation:

```text
Wikipedia dump
    ->
preprocess
    ->
Lucene index
    ->
candidate query
    ->
top concept/article IDs
```

Represent each target and runtime window as:

```text
{concept_id -> relevance_score}
```

Similarity can then be computed through:

```text
weighted overlap
cosine
Jaccard
rank-biased overlap
```

Cosine is acceptable here because the vectors now contain semantic corpus information.

---

## 14.2 Second-Order Co-Occurrence

Another useful signal:

Two words can be considered related if they occur in similar contexts even when they rarely occur together.

Example:

```text
payment context:
money
account
bank
transaction
amount
transfer

remittance context:
money
bank
transfer
amount
recipient
funds
```

Their context neighborhoods overlap.

Precompute:

```text
term -> top contextual neighbors
```

from the offline corpus.

At runtime compare:

```text
context-neighborhood(candidate)
```

against:

```text
context-neighborhood(input terms)
```

This can bridge vocabulary gaps without neural embeddings.

---

# 15. Stage 8 — Optional ConceptNet Evidence

ConceptNet may be bundled as an offline English-only graph.

Use it as an **additional evidence source**, not the dominant one.

It is especially useful for broader conceptual relationships.

Recommended role:

```text
WordNet       -> lexical semantics
Wikipedia     -> corpus semantics
ConceptNet    -> common-sense relationship support
```

ConceptNet evidence should generally have lower weight than exact lexical or strong WordNet evidence because broad graph connections can increase false positives.

---

# 16. Stage 9 — Local Evidence vs Distributed Evidence

This is critical for long inputs.

The system should calculate two separate evidence structures.

---

## 16.1 Local Evidence

Question:

```text
Is there one local region of the text that strongly supports this candidate?
```

Calculate:

```text
best_clause_score
best_sentence_score
best_2_sentence_score
best_3_sentence_score
```

Example:

```text
Candidate: ADDRESS_CHANGE

S1 score: 0.08
S2 score: 0.12
S3 score: 0.18
S4 score: 0.91
S5 score: 0.88
```

This is strong local evidence.

---

## 16.2 Distributed Evidence

Some intentions are expressed across distant sentences.

Example:

```text
S1: money was sent yesterday
S4: receiving party has not received it
S8: instruction shows completed
S11: customer wants us to find where it went
```

No individual window may fully express:

```text
PAYMENT_INVESTIGATION
```

Create a document-wide evidence map:

```text
payment / transfer evidence
non-receipt evidence
completed instruction evidence
investigation / locate evidence
```

Then combine these non-contiguous signals.

This is **not** the same as whole-document cosine.

It is structured evidence aggregation.

---

# 17. Major Enhancement #1 — Candidate Semantic Profiles

Do not store each target as just:

```text
PAYMENT_INVESTIGATION
```

Automatically build a semantic profile at startup.

Example conceptual profile:

```json
{
  "candidate": "PAYMENT_INVESTIGATION",
  "lexical": [],
  "wordnet": [],
  "gloss_terms": [],
  "corpus_concepts": [],
  "cooccurrence_neighbors": [],
  "conceptnet_neighbors": []
}
```

The content is generated from offline language resources, not manually curated synonyms.

This turns each of the 60–70 known outputs into a rich semantic object.

---

# 18. Major Enhancement #2 — Contrastive Candidate Profiles

This is one of the strongest upgrades for reducing false positives while increasing correct classifications.

Do not ask only:

```text
How similar is the input to PAYMENT_STATUS?
```

Also ask:

```text
What distinguishes PAYMENT_STATUS from its nearest competing labels?
```

Suppose:

```text
PAYMENT_STATUS
PAYMENT_INVESTIGATION
PAYMENT_FAILURE
```

are highly related.

Automatically identify confusing candidate pairs using their offline semantic profiles.

For each candidate, create:

```text
positive semantic evidence
confusable neighbors
discriminating evidence
```

Example:

```text
PAYMENT_STATUS
    shared:
        payment
        transaction
        check

    distinguishing:
        status
        current state
        progress
        whether processed

PAYMENT_INVESTIGATION
    shared:
        payment
        transaction
        check

    distinguishing:
        locate
        trace
        missing
        non-receipt
        investigate
```

These distinguishing concepts can be generated from differential corpus statistics rather than manually authored.

For candidate C:

```text
positive_score(C)
-
max(confusable_competitor_support)
```

becomes an important signal.

This helps prevent broad semantic similarity from causing wrong classifications.

---

# 19. Major Enhancement #3 — Information-Gain Weighting

Common concepts should carry less weight.

For example:

```text
customer
account
request
transaction
system
```

may appear everywhere.

Rare discriminating concepts should carry more weight.

Across the 60–70 candidate profiles compute:

```text
candidate_document_frequency(term)
```

and apply an IDF-like factor:

```text
discrimination_weight(term)
=
log(total_candidates / candidates_containing_term)
```

Thus:

```text
transaction
```

may have low value.

But:

```text
beneficiary
chargeback
refund
trace
```

may carry much more discriminative value.

This is especially useful because the candidate universe is small and fixed.

---

# 20. Major Enhancement #4 — Intent-Focused Weighting

Long text often contains:

```text
background
history
current observation
actual request
```

Without an ML model, the system can still use deterministic discourse cues.

Examples of high-value request cues:

```text
wants
needs
requests
asking
please
would like
needs us to
wants to know
trying to
requires
```

Examples of historical cues:

```text
previously
yesterday
earlier
last week
had
was
already
```

Do not hardcode domain synonyms here.

These are generic English discourse cues.

Weight clauses near request-intent markers more strongly.

Example:

```text
Payment succeeded yesterday.
The customer now wants to change the registered address.
```

`change registered address` should dominate the historical `payment` mention.

A suggested initial positional weighting:

```text
explicit request clause                  x1.35
clause after request marker              x1.20
neutral factual clause                   x1.00
strong historical/background clause      x0.75
```

This should be validated and tuned.

---

# 21. Major Enhancement #5 — Candidate Coverage Structure

Some target phrases contain multiple semantic components.

Example:

```text
PAYMENT_INVESTIGATION
```

may conceptually require:

```text
PAYMENT concept
+
INVESTIGATION concept
```

Instead of accepting a match based only on a strong `PAYMENT` signal, require semantic coverage across candidate components.

Automatically derive components from:

```text
candidate phrase tokens
WordNet senses
compound decomposition
corpus profile clusters
```

Example:

```text
PAYMENT_INVESTIGATION score:
payment_component = 0.92
investigation_component = 0.84

good
```

versus:

```text
payment_component = 0.95
investigation_component = 0.17

reject / downgrade
```

This greatly helps with closely related target phrases.

---

# 22. Major Enhancement #6 — Evidence Diversity

A candidate should not be accepted solely because one engine produced a high score.

Define evidence channels:

```text
L = lexical
W = WordNet
G = gloss
C = corpus semantic
O = co-occurrence
N = ConceptNet
D = distributed evidence
X = contrastive evidence
```

For a strong match, require support from at least two reasonably independent channels.

Example:

```text
WordNet = strong
Corpus  = strong
Lexical = weak
```

can still be accepted.

But:

```text
ConceptNet = very strong
everything else = weak
```

should usually remain `UNKNOWN`.

---

# 23. Major Enhancement #7 — Winner Stability

A genuine candidate should generally remain the winner under small changes in the text view.

Run scoring against:

```text
best local window
whole document
request-focused clauses
distributed evidence
document with strongest irrelevant sentence removed
```

If the winner changes dramatically between views, confidence should drop.

Example:

```text
full document       -> PAYMENT
request clause      -> ADDRESS_CHANGE
best 2 sentences    -> ADDRESS_CHANGE
distributed score   -> ADDRESS_CHANGE
```

This indicates `PAYMENT` was likely background noise.

Winner stability therefore acts as a false-positive filter.

---

# 24. Major Enhancement #8 — Negative Evidence / Contradictions

Semantic similarity often ignores contradiction.

Examples:

```text
enable
disable

received
not received

successful
failed

approved
rejected
```

Use:

```text
negation windows
WordNet antonyms
contradiction pairs from lexical resources
```

to subtract evidence.

A simple rule:

```text
positive semantic match
+
strong antonym / negation evidence
=
candidate penalty
```

Negative evidence should receive strong weight because it is highly discriminative.

---

# 25. Major Enhancement #9 — Adaptive Candidate-Specific Thresholds

Do not use one universal threshold.

Some candidates are easy to distinguish.

Example:

```text
ADDRESS_CHANGE
```

may be relatively unique.

Others:

```text
PAYMENT_STATUS
PAYMENT_INVESTIGATION
PAYMENT_FAILURE
```

may be highly confusable.

Each candidate should have:

```text
min_accept_score
min_margin
min_evidence_channels
```

derived from validation data.

Example:

```json
{
  "PAYMENT": {
    "min_score": 0.78,
    "min_margin": 0.10
  },
  "PAYMENT_INVESTIGATION": {
    "min_score": 0.87,
    "min_margin": 0.18
  }
}
```

This is calibration, not an ML model.

---

# 26. Major Enhancement #10 — Pairwise Confusion Gates

During validation, build a confusion matrix.

Example:

```text
PAYMENT_STATUS -> often confused with PAYMENT_INVESTIGATION
REFUND         -> often confused with CANCEL_PAYMENT
```

For commonly confused pairs, apply an additional discriminator.

Example:

```text
if top1 = PAYMENT_STATUS
and top2 = PAYMENT_INVESTIGATION
and margin < pair_specific_margin:
    UNKNOWN
```

Pair-specific gates can reduce false positives without raising all thresholds globally.

---

# 27. Candidate Scoring Model

A practical starting score can be:

```text
candidate_score =
    0.22 * lexical_score
  + 0.20 * wordnet_score
  + 0.10 * gloss_score
  + 0.20 * corpus_semantic_score
  + 0.08 * cooccurrence_score
  + 0.05 * conceptnet_score
  + 0.10 * distributed_evidence_score
  + 0.05 * intent_focus_score
```

Then apply modifiers:

```text
+ discriminative_term_bonus
+ component_coverage_bonus
+ winner_stability_bonus
+ contrastive_margin_bonus

- negation_penalty
- contradiction_penalty
- ambiguity_penalty
- background_only_penalty
```

The numeric weights above are starting values only.

---

# 28. Better Fusion Than a Pure Weighted Average

A simple weighted average can hide weak evidence.

Recommended structure:

```text
base_score
+
agreement_bonus
-
risk_penalties
```

Example:

```text
base_score = weighted engine average

agreement_bonus:
    +0.04 if >= 2 strong channels
    +0.07 if >= 3 strong channels

risk penalties:
    -0.08 if winner unstable
    -0.10 if strong contradiction
    -0.06 if candidate component incomplete
    -0.05 if top-two ambiguity high
```

This makes the scoring explainable.

---

# 29. Acceptance Gates

A candidate should be returned only if all required gates pass.

## Gate 1 — Absolute score

```text
best_score >= candidate.min_score
```

## Gate 2 — Winner margin

```text
best_score - second_best_score >= candidate.min_margin
```

## Gate 3 — Evidence diversity

Example:

```text
at least 2 independent channels >= strong_evidence_threshold
```

## Gate 4 — Semantic component coverage

For multi-part targets:

```text
all required semantic components have meaningful support
```

## Gate 5 — Contradiction

```text
no dominant contradiction / antonym evidence
```

## Gate 6 — Winner stability

```text
candidate remains winner across enough document views
```

## Gate 7 — Pairwise confusion rule

```text
specific top1/top2 ambiguity rule passes
```

If any mandatory gate fails:

```text
UNKNOWN
```

---

# 30. Suggested Confidence Levels

Instead of pretending the score is a probability, expose operational confidence bands.

Example:

```text
>= 0.90     VERY_HIGH
0.84–0.89   HIGH
0.78–0.83   MODERATE
< 0.78      LOW / UNKNOWN
```

Actual boundaries should be candidate-specific and derived from validation.

---

# 31. Expected Performance Target

The original simpler architecture was estimated around:

```json
{
  "correct": 53,
  "unknown": 43,
  "false_positive": 4
}
```

The enhanced pipeline aims to recover more correct matches by adding:

```text
multi-scale analysis
distributed evidence
offline corpus semantics
contrastive profiles
component coverage
intent weighting
winner stability
candidate-specific thresholds
pairwise confusion gates
```

The recommended engineering target is:

```json
{
  "total_inputs": 100,
  "correct_matches": 70,
  "false_positives": 6,
  "unknown": 24
}
```

Equivalent metrics:

```text
overall correct coverage = 70%
accepted predictions     = 76%
accepted precision       = 70 / 76 = 92.1%
false positive rate among accepted = 7.9%
```

This satisfies:

```text
>= 70 correct
< 10 false positives
```

if the candidate set is reasonably distinguishable and thresholds are calibrated using representative data.

---

# 32. Why 70 Correct Is a Reasonable Target but Not Guaranteed

The final result depends heavily on target separability.

## Easier candidate universe

```text
PAYMENT
ADDRESS_CHANGE
PASSWORD_RESET
REFUND
BENEFICIARY
STATEMENT
LOAN
```

These are semantically distinct.

Expected performance can be strong.

## Hard candidate universe

```text
PAYMENT_STATUS
PAYMENT_TRACKING
PAYMENT_INVESTIGATION
PAYMENT_CONFIRMATION
PAYMENT_FAILURE
PAYMENT_ENQUIRY
```

These overlap heavily.

The semantic engine may understand the text but still struggle to select the exact business label.

In this case, contrastive scoring and component coverage become essential.

---

# 33. Evaluation Strategy

Do not evaluate on artificially generated one-line examples only.

Build a labelled evaluation set containing real production-like text.

Recommended minimum:

```text
500 examples
```

Preferred:

```text
1,000–3,000 examples
```

Include:

```text
direct lexical matches
synonyms
paraphrases
long background-heavy inputs
scattered evidence
negation
multiple competing concepts
out-of-scope inputs
ambiguous inputs
typos
poor grammar
very short inputs
```

---

# 34. Required Metrics

Track:

```text
correct
false_positive
unknown
precision among accepted
coverage
per-candidate precision
per-candidate recall
top-2 confusion
winner margin distribution
```

Primary business metric:

```text
accepted_precision
```

Secondary metric:

```text
correct_coverage
```

Target:

```text
accepted_precision >= 90%
correct_coverage >= 70%
```

---

# 35. Calibration Procedure

For each validation example:

1. execute all scorers;
2. record every candidate score;
3. record winning candidate;
4. record second-best candidate;
5. record evidence channels;
6. compare with ground truth.

Then tune:

```text
candidate thresholds
candidate margins
pairwise confusion margins
channel weights
agreement bonuses
risk penalties
```

Tune toward:

```text
false positives < 10
```

first.

Then relax safe candidates to increase coverage toward:

```text
70 correct
```

---

# 36. Threshold Optimization Strategy

For each candidate:

```text
Sort predictions by candidate score descending.
```

Find the lowest threshold that maintains:

```text
precision >= target_precision
```

Example:

```text
threshold 0.70
accepted 120
correct 102
precision 85%

threshold 0.79
accepted 96
correct 88
precision 91.7%

threshold 0.86
accepted 74
correct 71
precision 95.9%
```

Candidate threshold could initially be:

```text
0.79
```

if the system-wide false-positive budget allows it.

---

# 37. Offline Resource Stack

Recommended starting stack:

```text
Open English WordNet
Lucene
offline Wikipedia index
Wiktionary lexical extracts
```

Optional later additions:

```text
ConceptNet English subset
larger corpus co-occurrence index
```

Do not begin with every resource simultaneously.

Recommended sequence:

```text
V1 = WordNet + lexical + gloss + windows
V2 = offline Wikipedia semantic index
V3 = distributed evidence + contrastive profiles
V4 = ConceptNet / additional corpus evidence if useful
```

---

# 38. Java-Oriented Component Structure

Suggested packages:

```text
semanticmatcher/
    normalize/
    segment/
    phrase/
    lexical/
    wordnet/
    corpus/
    conceptnet/
    candidate/
    evidence/
    scoring/
    calibration/
    evaluation/
    api/
```

Key services:

```java
TextNormalizer
SentenceSegmenter
WindowGenerator
PhraseExtractor
LexicalScorer
WordNetService
SenseDisambiguator
GlossScorer
CorpusSemanticIndex
CooccurrenceService
ConceptGraphService
CandidateProfileBuilder
DistributedEvidenceAggregator
ContrastiveScorer
EvidenceFusionEngine
AcceptanceGate
SemanticMatcher
```

---

# 39. Core Data Structures

## CandidateProfile

```java
class CandidateProfile {
    String id;
    String canonicalText;

    Set<String> lemmas;
    Set<String> phrases;

    Map<String, Double> wordNetConcepts;
    Map<String, Double> glossTerms;
    Map<String, Double> corpusConcepts;
    Map<String, Double> cooccurrenceTerms;
    Map<String, Double> conceptGraphTerms;

    Set<String> confusableCandidates;
    Map<String, Double> discriminativeTerms;

    double minScore;
    double minMargin;
}
```

---

## Evidence

```java
class Evidence {
    String candidateId;
    String source;
    String textSpan;
    double score;
    String explanation;
}
```

---

## CandidateScore

```java
class CandidateScore {
    String candidateId;

    double lexical;
    double wordNet;
    double gloss;
    double corpus;
    double cooccurrence;
    double conceptNet;
    double distributed;
    double intentFocus;

    double contrastivePenalty;
    double contradictionPenalty;
    double stabilityScore;

    double finalScore;
}
```

---

# 40. Runtime Algorithm

High-level pseudocode:

```text
function match(document):

    normalized = normalize(document)

    clauses = segmentIntoClauses(normalized)
    sentences = segmentIntoSentences(normalized)

    windows = createWindows(
        clauses,
        sentences,
        2_sentence_windows,
        3_sentence_windows,
        whole_document
    )

    for each window:
        lexicalEvidence  = lexicalEngine(window)
        wordnetEvidence  = wordnetEngine(window)
        glossEvidence    = glossEngine(window)
        corpusEvidence   = corpusEngine(window)
        contextEvidence  = cooccurrenceEngine(window)

    distributedEvidence =
        aggregateEvidenceAcrossDocument(allWindowEvidence)

    for each candidate:
        localScore =
            scoreBestRelevantWindows(candidate)

        distributedScore =
            scoreDistributedEvidence(candidate)

        componentCoverage =
            calculateComponentCoverage(candidate)

        contrastiveScore =
            compareAgainstConfusableCandidates(candidate)

        stability =
            evaluateWinnerStability(candidate)

        finalScore =
            fuse(
                localScore,
                distributedScore,
                componentCoverage,
                contrastiveScore,
                stability
            )

    rank candidates

    best = rank[0]
    second = rank[1]

    if !absoluteThreshold(best):
        return UNKNOWN

    if !marginThreshold(best, second):
        return UNKNOWN

    if !evidenceDiversity(best):
        return UNKNOWN

    if !componentCoverage(best):
        return UNKNOWN

    if contradictionDetected(best):
        return UNKNOWN

    if !winnerStable(best):
        return UNKNOWN

    if !pairwiseConfusionGate(best, second):
        return UNKNOWN

    return MATCH(best)
```

---

# 41. Explainability Requirement

Every accepted match should contain evidence.

Example:

```json
{
  "candidate": "PAYMENT_INVESTIGATION",
  "score": 0.91,
  "margin": 0.24,
  "supporting_evidence": [
    {
      "text": "the receiving party has advised that nothing has arrived",
      "source": "corpus+wordnet",
      "score": 0.84
    },
    {
      "text": "the customer wants us to establish where the funds are",
      "source": "distributed_evidence",
      "score": 0.89
    }
  ]
}
```

This is important for:

```text
debugging
threshold tuning
business review
false-positive analysis
auditability
```

---

# 42. False-Positive Controls

The most important controls are:

```text
1. UNKNOWN option
2. candidate-specific thresholds
3. winner margin
4. evidence diversity
5. contrastive candidate scoring
6. semantic component coverage
7. contradiction penalties
8. winner stability
9. pairwise confusion gates
10. explainable supporting evidence
```

No single similarity score should bypass these controls.

---

# 43. Most Likely False-Positive Patterns

## 43.1 Historical concept dominates current request

Mitigation:

```text
intent-focused weighting
multi-scale windows
winner stability
```

## 43.2 Broad WordNet relation

Mitigation:

```text
relation weighting
word-sense disambiguation
multi-channel evidence
```

## 43.3 Corpus concept too broad

Mitigation:

```text
contrastive profiles
candidate-IDF
component coverage
```

## 43.4 Closely related candidate labels

Mitigation:

```text
pairwise confusion gates
candidate-specific margins
contrastive scoring
```

## 43.5 Negation reverses meaning

Mitigation:

```text
negation scope
antonym graph
contradiction penalties
```

---

# 44. Performance and Runtime Characteristics

Only ~60–70 targets exist.

This is very small.

Therefore substantial precomputation is possible.

At startup:

```text
load WordNet
load candidate profiles
load corpus index handles
load candidate thresholds
load confusion metadata
```

Candidate profile expansion can be done once and cached.

Runtime work is mostly:

```text
normalize one input
generate windows
query local indexes
score ~70 candidates
```

This should be practical on a normal application server if indexes are designed correctly.

---

# 45. Precomputation Strategy

Precompute for every candidate:

```text
WordNet relations to limited depth
definitions/gloss vectors
top Wikipedia concepts
top corpus neighbors
discriminative terms
confusable candidates
candidate-specific thresholds
```

Store:

```text
JSON
binary serialized data
Lucene index
embedded DB
```

depending on deployment requirements.

---

# 46. Recommended Implementation Phases

## Phase 1 — Baseline

Implement:

```text
normalization
sentence/clause segmentation
multi-scale windows
lexical scoring
WordNet
gloss matching
UNKNOWN threshold
winner margin
```

Goal:

```text
establish baseline precision
```

---

## Phase 2 — Corpus Semantics

Add:

```text
offline Wikipedia/Wiktionary index
ESA-style concept retrieval
co-occurrence neighborhoods
```

Goal:

```text
recover paraphrases missed by WordNet
```

---

## Phase 3 — Long-Text Intelligence

Add:

```text
distributed evidence
intent-focused weighting
winner stability
```

Goal:

```text
handle 100–150-word inputs safely
```

---

## Phase 4 — Confusion Reduction

Add:

```text
contrastive candidate profiles
component coverage
candidate IDF
pairwise confusion gates
```

Goal:

```text
increase correct coverage while maintaining <10 FP
```

---

## Phase 5 — Calibration

Using real labelled examples:

```text
candidate thresholds
candidate margins
engine weights
confusion pair rules
```

Goal:

```text
>=70 correct
<10 false positives
```

---

# 47. Recommended First Experiment

Before building every component, run a controlled benchmark.

Dataset:

```text
500–1,000 labelled real examples
```

Compare:

```text
A. lexical only
B. lexical + WordNet
C. + gloss
D. + Wikipedia corpus semantics
E. + multi-scale windows
F. + distributed evidence
G. + contrastive profiles
H. + stability / safety gates
```

Record after every step:

```text
correct
false positive
unknown
precision
coverage
```

This produces an ablation study and proves which components actually add value.

---

# 48. Success Criteria

The project should be considered successful when a representative held-out dataset achieves approximately:

```json
{
  "correct_matches": ">= 70%",
  "false_positives": "< 10%",
  "accepted_precision": ">= 90%"
}
```

Preferably:

```json
{
  "correct_matches": "70-74%",
  "false_positives": "4-7%",
  "unknown": "19-26%"
}
```

---

# 49. Important Limitation

No symbolic/corpus system can perfectly reproduce learned neural semantics.

Some inputs may be extremely implicit.

Example:

```text
Canonical target:
REFUND

Runtime:
"The customer changed his mind and wants what he spent yesterday."
```

The system may not reliably infer the exact intent.

That is acceptable under this architecture.

The correct result in uncertain cases is:

```text
UNKNOWN
```

The design intentionally avoids pretending to understand text where semantic evidence is weak.

---

# 50. Final Recommendation

The strongest solution within the stated constraints is:

```text
Offline English lexical knowledge
        +
offline corpus semantic knowledge
        +
multi-scale long-text analysis
        +
distributed evidence
        +
contrastive candidate scoring
        +
candidate component coverage
        +
precision-first acceptance gates
        +
UNKNOWN
```

Do **not** build the solution around one cosine-similarity score.

Do **not** rely only on WordNet.

Do **not** force the closest candidate to win.

The architecture should behave as a **closed-set semantic retrieval system with abstention**, not as a generic classifier.

Its primary engineering objective should be:

```text
Find strong, explainable evidence that candidate X is the correct candidate
AND
prove that the nearest competing candidates are materially less supported.
```

That contrastive requirement is the key upgrade that gives the system a realistic path toward:

```text
~70 correct matches
<10 false positives
```

without LLMs, ML inference, embeddings, or runtime network calls.

---

# 51. Target Output Snapshot

Recommended planning JSON:

```json
{
  "total_inputs": 100,
  "correct_matches_target": 70,
  "false_positives_target": 6,
  "maximum_false_positives_allowed": 9,
  "unknown_target": 24,
  "accepted_predictions_target": 76,
  "accepted_precision_target_percent": 92.1,
  "primary_optimization": "precision_first",
  "fallback_behavior": "UNKNOWN"
}
```

---

# 52. Handoff Notes for Another Engineering Agent

When continuing this design:

1. Do not replace the symbolic/corpus constraints with embeddings unless the architectural constraints change.
2. Keep `UNKNOWN` as a mandatory result.
3. Treat false-positive reduction as a first-class requirement.
4. Benchmark every newly added semantic source independently.
5. Prefer measurable gains over theoretical complexity.
6. Build candidate confusion analysis early.
7. Use real 100–150-word examples, not only short synthetic sentences.
8. Expose evidence for every accepted match.
9. Keep all semantic resources local and versioned.
10. Target `>=70 correct / 100` only after precision is safely above 90%.
