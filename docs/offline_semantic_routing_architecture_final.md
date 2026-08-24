# Offline Semantic Routing Architecture
## High-Precision Mapping of Uncontrolled User Text to a Small Fixed Output Set — Without LLM/ML at Runtime

**Document status:** Proposed reference architecture  
**Primary runtime:** Java  
**Runtime network calls:** None  
**Runtime LLM usage:** None  
**Runtime ML model usage:** None  
**Primary objective:** Reduce manual intervention without increasing user effort through false routing  
**Preferred failure mode:** `UNKNOWN / MANUAL` is better than an incorrect automated prediction  

---

# 1. Executive Summary

The system receives a free-form user message and must choose exactly one output from a small fixed set of known outcomes.

### Known output universe

- Approximately **50–60 possible outputs**
- Each output is only **2–4 words / keywords**
- Example style:
  - `ORDER STATUS`
  - `ROUTING FAILURE`
  - `TRACK DELIVERY`
  - `UPDATE DESTINATION`
  - `MISSING EVENT`
  - `FLOW INVESTIGATION`

The examples above are illustrative only. The architecture is domain-independent.

### Runtime input

- Uncontrolled English text
- Usually around **50–60 words**
- Can be as long as **~150 words**
- May contain:
  - background history;
  - irrelevant context;
  - multiple concepts;
  - indirect wording;
  - synonyms;
  - text that never mentions the canonical output words;
  - important clues scattered across multiple sentences.

### Required output

The system may return:

1. a **safe automated prediction**, or
2. `UNKNOWN / MANUAL`.

The system must **not** force a prediction when evidence is weak.

### Why this matters

Current workflow:

```text
2 manual interventions per message
```

If prediction is correct:

```text
1 manual intervention
```

If prediction is wrong and downstream automation follows the wrong route:

```text
4 manual interventions
```

Therefore:

```text
Each correct auto-prediction saves 1 manual intervention.
Each false auto-prediction costs 2 extra interventions relative to baseline.
```

So the architecture is not optimized for “maximum number of predictions”.

It is optimized for:

```text
Maximum SAFE automation
subject to a very low false-positive rate.
```

---

# 2. Final Architecture Decision

The final system should be treated as a:

> **Closed-set semantic retrieval and verification engine with abstention**

It should **not** be treated as a traditional classifier.

The system does not ask only:

```text
Which label is most similar to the message?
```

It asks:

```text
1. Which labels have evidence?
2. Which label has the strongest evidence?
3. Why is it stronger than its closest competitors?
4. Do several independent evidence sources agree?
5. Does the same winner survive different views of the input?
6. Do historical resolved cases support this decision?
7. Is the decision safe enough to automate?
```

Only after these checks does the system allow downstream automation.

---

# 3. High-Level Architecture

```text
                       NEW USER MESSAGE
                       50–150 WORDS
                              |
                              v
                    TEXT NORMALIZATION
                              |
                              v
               SENTENCE / CLAUSE SEGMENTATION
                              |
                              v
                    MULTI-SCALE WINDOWS
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
     LEXICAL ENGINE       WORDNET ENGINE      CORPUS ENGINE
    exact / fuzzy /       lexical graph        offline semantic
    BM25 / phrases        relationships        concept retrieval
          |                   |                   |
          +-------------------+-------------------+
                              |
                              v
                 HISTORICAL CASE RETRIEVAL
                              |
                              v
              CANDIDATE PROFILE / CASE EVIDENCE
                              |
                 +------------+------------+
                 |                         |
                 v                         v
          LOCAL EVIDENCE             DISTRIBUTED EVIDENCE
                 |                         |
                 +------------+------------+
                              |
                              v
                  CONTRASTIVE VERIFICATION
                "WHY A AND NOT B / C?"
                              |
                              v
                     EVIDENCE FUSION
                              |
                              v
                 SAFETY / CONFIDENCE GATES
                              |
                 +------------+------------+
                 |                         |
                 v                         v
          SAFE AUTO-PREDICT            UNKNOWN
           downstream path             manual path
```

---

# 4. Core Design Principle

The biggest challenge is structural:

```text
Runtime message = 50–150 words
Known output    = only 2–4 words
```

A direct comparison between these two objects is weak.

Therefore the architecture must **enrich the short output set automatically**.

The enrichment should come from three sources:

```text
A. General English knowledge
B. General corpus knowledge
C. Real historical examples from this application
```

These three together replace the missing semantic richness of the short labels.

---

# 5. Knowledge Sources

## 5.1 Canonical Output Set

The application begins with approximately:

```text
50–60 canonical outputs
```

Each output contains:

```text
2–4 words
```

Example:

```text
ORDER STATUS
FLOW INVESTIGATION
DESTINATION UPDATE
MISSING EVENT
ROUTING FAILURE
```

No manually maintained synonym set should be required.

---

## 5.2 WordNet / Open English WordNet

Purpose:

```text
Lexical semantic relationships
```

Useful relationships include:

- synonym;
- same synset;
- derivationally related word;
- hypernym;
- hyponym;
- antonym;
- definition / gloss;
- example usage;
- related noun / verb forms.

Example conceptually:

```text
track
tracking
trace
follow
```

may have useful lexical relationships even when the exact canonical word is absent.

WordNet is not used as the final decision engine.

It is one evidence source.

---

## 5.3 Offline General-English Corpus

Recommended sources:

```text
Wikipedia
Wiktionary
other approved English corpus
```

Purpose:

> Find broader conceptual relationships that a dictionary graph does not express well.

Example:

```text
canonical:
MISSING EVENT

runtime message:
"The expected downstream step never appeared after the router completed."
```

Direct word overlap may be poor.

A corpus index can connect words such as:

```text
expected
step
appeared
event
sequence
missing
```

through shared context.

Possible deterministic techniques:

```text
BM25
TF-IDF
Explicit Semantic Analysis style concept retrieval
second-order co-occurrence
sparse concept vectors
```

No neural embeddings are required.

---

## 5.4 Historical Resolved Case Library

This is the most important domain-specific component.

Assume historical records exist like:

```text
historical user message
        ->
correct final output
```

Example:

```text
"The order left the first service but no corresponding event
appears in the downstream application."

        -> MISSING EVENT
```

A new user message can be compared against these **real previously solved messages**.

This is much stronger than comparing only against:

```text
MISSING EVENT
```

because the historical messages contain actual user language.

### Important constraint

The case library should contain only:

```text
verified / correctly resolved cases
```

Bad historical labels must not silently become knowledge.

---

# 6. Why Historical Case Retrieval Is Critical

The canonical output:

```text
ROUTING FAILURE
```

contains only two words.

But 100 past cases mapped to that output may contain language such as:

```text
route stopped
next system never received it
router rejected destination
flow ended unexpectedly
routing decision not produced
handoff failed
no route generated
```

Nobody manually configures these as synonyms.

The case library naturally captures how real users describe the same concept.

This gives the application **domain language without maintaining a domain thesaurus**.

---

# 7. No Runtime ML Means the Historical Layer Must Stay Retrieval-Based

Do not train:

```text
neural classifier
sentence transformer
embedding model
SVM
random forest
gradient boosting
```

Instead use deterministic retrieval/statistics:

```text
BM25 nearest resolved messages
TF-IDF sparse vectors
phrase overlap
WordNet-expanded overlap
corpus-concept overlap
class-level term statistics
```

The system retrieves historical cases.

It does not infer using a trained predictive model.

---

# 8. Offline Build / Startup Phase

Before runtime prediction, build semantic profiles for all outputs.

For each canonical output:

```text
1. tokenize the 2–4 words
2. normalize morphology
3. retrieve WordNet concepts
4. retrieve definitions / glosses
5. retrieve related lexical concepts
6. query the offline corpus
7. retrieve top general-English concepts
8. retrieve verified historical examples
9. calculate discriminative terms against competing outputs
10. identify likely confusion candidates
```

Result:

```text
CandidateProfile
```

Example shape:

```json
{
  "candidate": "FLOW_INVESTIGATION",
  "canonical_terms": [],
  "wordnet_concepts": [],
  "gloss_terms": [],
  "corpus_concepts": [],
  "historical_case_ids": [],
  "historical_discriminators": [],
  "confusable_candidates": []
}
```

---

# 9. Text Normalization

Runtime normalization should remain deterministic.

Recommended:

```text
Unicode normalization
lowercase conversion
punctuation normalization
sentence splitting
clause splitting
tokenization
WordNet morphology / lemmatization
contraction normalization
light stop-word filtering
preserve negation
```

Avoid removing words such as:

```text
not
never
missing
without
failed
stopped
rejected
```

because they may reverse or define the meaning.

---

# 10. Sentence and Clause Segmentation

Do not process a 100-word message as a single bag of words only.

Example:

```text
The order was accepted yesterday.
The earlier issue was already resolved.
Today the user is asking why the downstream event is missing.
```

Whole-message similarity may incorrectly overweight:

```text
accepted
resolved
issue
```

The third sentence is the actual request.

Therefore generate:

```text
sentences
clauses
```

as separate semantic units.

---

# 11. Multi-Scale Window Generation

Generate several representations.

Example message:

```text
S1
S2
S3
S4
S5
```

Create:

```text
Individual clauses
Individual sentences

S1 + S2
S2 + S3
S3 + S4
S4 + S5

S1 + S2 + S3
S2 + S3 + S4
S3 + S4 + S5

Whole message
```

This allows the system to detect both:

```text
concentrated evidence
```

and:

```text
distributed evidence
```

---

# 12. Evidence Engine A — Exact and Lexical Retrieval

Highest precision evidence.

Use:

```text
exact phrase
exact token
lemma
morphological root
character n-gram
edit distance
Jaro-Winkler
token overlap
BM25
```

Examples:

```text
"routing failed"
"route failure"
"routing failur"
```

should produce very strong evidence for:

```text
ROUTING FAILURE
```

This channel should have high weight because it is easy to explain and comparatively safe.

---

# 13. Evidence Engine B — WordNet Semantic Graph

For each meaningful runtime token / phrase:

```text
retrieve possible senses
retrieve related synsets
measure graph distance
inspect definitions
inspect derivational relationships
inspect antonyms
```

Relationship strengths should decay with semantic distance.

Illustrative weighting:

```text
exact lemma                       1.00
same synset                       0.98
direct synonym                    0.95
derivationally related            0.90
similar-to                        0.85
direct hypernym / hyponym         0.75
2-hop graph relation              0.55
3-hop graph relation              0.30
```

These are starting values only.

They must be validated.

---

# 14. Deterministic Word-Sense Disambiguation

WordNet words can have multiple senses.

Example:

```text
node
route
flow
branch
```

may have several general-English meanings.

Blind expansion increases false positives.

Use a deterministic contextual disambiguation approach:

```text
candidate sense definition
+
neighbor definitions
+
surrounding runtime terms
```

Measure overlap.

Prefer the sense that best matches surrounding context.

If sense confidence is low:

```text
reduce WordNet contribution
```

rather than forcing a sense.

---

# 15. Evidence Engine C — Gloss / Definition Retrieval

Canonical labels are short.

Definitions enrich them.

Example:

```text
TRACK ORDER
```

can be expanded through dictionary glosses involving ideas such as:

```text
follow
progress
location
status
movement
sequence
```

Runtime text may contain those conceptual terms even when `track` is absent.

Use:

```text
BM25
TF-IDF
weighted token overlap
```

between runtime windows and candidate gloss profiles.

---

# 16. Evidence Engine D — Offline Corpus Semantic Retrieval

This engine captures broader context.

### Option 1 — ESA-style concept index

Build an offline index where corpus documents / concepts represent semantic dimensions.

For each candidate:

```text
candidate text
+
WordNet gloss
+
historical profile
```

retrieve top corpus concepts.

For runtime windows:

```text
retrieve top corpus concepts
```

Then compare sparse concept sets.

Possible similarity:

```text
weighted overlap
cosine on sparse concept vectors
rank overlap
```

Cosine is acceptable here because the vectors are no longer raw words.

They contain corpus-derived semantic context.

---

# 17. Evidence Engine E — Second-Order Co-Occurrence

Two words can be related because they appear in similar environments.

Example:

```text
tracking
monitoring
following
```

may share contextual neighbors such as:

```text
status
progress
location
event
sequence
```

Precompute:

```text
term -> top contextual neighbors
```

from the offline corpus.

Compare these neighborhoods at runtime.

This helps with unseen paraphrases.

---

# 18. Evidence Engine F — Historical Case Retrieval

Index every verified historical message.

Recommended fields:

```text
full normalized text
sentence windows
phrases
canonical output
timestamp
quality flag
```

At runtime:

```text
retrieve top historical messages similar to the new message
```

Do not simply use one nearest case.

Use a small retrieval neighborhood.

Example:

```text
Top retrieved cases:

case 812 -> MISSING EVENT        score 14.2
case 104 -> MISSING EVENT        score 13.7
case 921 -> MISSING EVENT        score 13.1
case 337 -> ROUTING FAILURE      score  9.5
case 401 -> ORDER STATUS         score  8.8
```

This is strong evidence for:

```text
MISSING EVENT
```

because multiple high-quality previous cases agree.

---

# 19. Case Agreement Signal

Define a deterministic case-agreement measure.

Example:

```text
top 5 historical cases
```

Weights based on retrieval score:

```text
case1  1.00
case2  0.85
case3  0.70
case4  0.55
case5  0.40
```

Aggregate by output label.

Example:

```text
MISSING EVENT        2.55
ROUTING FAILURE      0.55
ORDER STATUS         0.40
```

Normalize to create:

```text
historical_case_agreement
```

This is not treated as a trained classifier.

It is evidence from retrieved resolved cases.

---

# 20. Historical Class Profiles

Individual case retrieval is useful but can be noisy.

Also build an aggregated sparse profile per candidate using its historical messages.

For each candidate derive:

```text
frequent terms
frequent phrases
rare discriminative terms
common corpus concepts
common WordNet concepts
```

Use IDF-style weighting across candidates.

Example:

```text
"system"
```

may occur everywhere and should have low value.

But:

```text
"downstream event"
"missing sequence"
"routing destination"
```

may strongly distinguish specific outputs.

---

# 21. Candidate Discriminative Weighting

For every semantic term T:

```text
candidate_frequency(T)
```

means:

```text
How many output classes use this concept strongly?
```

Use:

```text
discrimination_weight(T)
=
log(total_candidates / candidate_frequency(T))
```

Common generic concepts get low weight.

Rare distinguishing concepts get high weight.

This is essential because the output set is small and fixed.

---

# 22. Local Evidence

Question:

> Is there one part of the message that strongly supports candidate X?

For each candidate calculate:

```text
best clause score
best sentence score
best 2-sentence score
best 3-sentence score
```

Example:

```text
Candidate: UPDATE DESTINATION

S1    0.08
S2    0.11
S3    0.16
S4    0.89
S5    0.84
```

This indicates strong local evidence.

---

# 23. Distributed Evidence

Some requests cannot be recognized from one sentence.

Example:

```text
S1: The order entered service A successfully.
S4: No message appears in service C.
S7: The expected transition through service B is present.
S9: The user wants us to identify where the sequence stopped.
```

Evidence is distributed.

Build a document-wide evidence structure:

```text
order / flow concept
missing downstream concept
transition concept
investigation concept
```

Aggregate non-contiguous evidence without treating the whole document as a simple bag of words.

---

# 24. Request-Focused Weighting

Long user messages often contain:

```text
history
background
current state
actual request
```

Generic English discourse markers can help identify likely request clauses.

Examples:

```text
want
need
asking
trying to
would like
looking for
why
where
which
can you
```

Background indicators:

```text
previously
earlier
yesterday
already
last time
before
```

These are not domain synonyms.

They are generic discourse cues.

Suggested initial multiplier:

```text
explicit request clause        x1.30
adjacent supporting clause     x1.15
neutral clause                 x1.00
strong history/background      x0.75
```

These weights must be validated, not assumed.

---

# 25. Candidate Component Coverage

A 2–4-word output may contain multiple semantic components.

Example:

```text
FLOW INVESTIGATION
```

contains:

```text
FLOW
INVESTIGATION
```

A message containing only:

```text
flow
```

should not automatically win.

Require evidence coverage for both components.

Example:

```text
flow_component          0.93
investigation_component 0.82
```

strong.

Versus:

```text
flow_component          0.95
investigation_component 0.18
```

weak / unsafe.

This is especially important for closely related outputs.

---

# 26. Contrastive Candidate Profiles

The system should explicitly learn which candidates are easily confused.

Example:

```text
ORDER STATUS
TRACK ORDER
ORDER INVESTIGATION
```

All may share:

```text
order
status
track
progress
```

The decision engine must ask:

```text
What evidence supports candidate A
that does NOT support candidate B?
```

Automatically calculate differential terms from:

```text
historical class profiles
WordNet concepts
corpus concepts
```

Example:

```text
ORDER STATUS
  distinguishing:
    current state
    latest status
    completion state

TRACK ORDER
  distinguishing:
    location
    movement
    progress path

ORDER INVESTIGATION
  distinguishing:
    missing
    unexpected
    trace
    identify problem
```

Do not manually author these unless business rules later justify doing so.

---

# 27. Pairwise Confusion Map

During evaluation construct:

```text
Candidate A -> Candidate B confusion frequency
```

Example:

```text
TRACK ORDER -> ORDER STATUS        high
MISSING EVENT -> ROUTING FAILURE   medium
UPDATE DESTINATION -> ORDER EDIT   low
```

Use this to create pair-specific safety margins.

This is better than raising the threshold globally.

---

# 28. Negative Evidence and Contradiction

Semantic similarity alone can be dangerous.

Examples:

```text
exists vs missing
success vs failure
start vs stop
received vs not received
enable vs disable
```

Use:

```text
negation scope
WordNet antonyms
candidate contrastive concepts
```

to subtract evidence.

Strong contradiction should be able to veto a prediction.

---

# 29. Winner Stability

A reliable prediction should remain reasonably stable across multiple input views.

Evaluate the winner against:

```text
whole message
best local window
request-focused clauses
distributed evidence
message with strongest background sentence removed
historical case retrieval only
semantic resource evidence only
```

If the winner changes repeatedly:

```text
confidence decreases
```

Example:

```text
Whole message        -> ORDER STATUS
Request clause       -> UPDATE DESTINATION
Best local window    -> UPDATE DESTINATION
Case retrieval       -> UPDATE_DESTINATION
```

The whole-message result is probably background contamination.

---

# 30. Evidence Diversity

Do not auto-route because one engine is confident.

Define channels:

```text
LEXICAL
WORDNET
GLOSS
CORPUS
CO_OCCURRENCE
HISTORICAL_CASE
DISTRIBUTED
CONTRASTIVE
```

A safe automated prediction should usually require at least:

```text
2 independent strong channels
```

Preferably:

```text
3
```

for highly confusable outputs.

Example:

```text
Historical case agreement   strong
Corpus semantics            strong
WordNet                     medium
```

is much safer than:

```text
Concept graph only          very strong
everything else             weak
```

---

# 31. Evidence Fusion

Do not use a single cosine score.

Use a transparent composite.

Illustrative starting weights:

```text
lexical / phrase                15%
WordNet                         12%
gloss                           8%
offline corpus semantic         15%
co-occurrence                    5%
historical case retrieval       25%
historical class profile        10%
distributed / request evidence  10%
```

Then apply modifiers:

```text
+ discriminative evidence bonus
+ candidate component coverage bonus
+ cross-view stability bonus
+ multi-engine agreement bonus

- contradiction penalty
- ambiguity penalty
- background-only penalty
- pairwise confusion penalty
```

The exact percentages should be calibrated from real data.

---

# 32. Raw Score Is Not the Production Confidence

This is a critical design rule.

The semantic engine may produce:

```text
raw score = 0.84
```

That number is **not automatically an 84% probability**.

Instead, production confidence should be empirically calibrated using held-out historical data.

Example:

```text
Among past cases with:
raw score 0.82–0.86
margin > 0.18
3 evidence channels
stable winner

97 of 100 were correct.
```

Then operational reliability may be:

```text
~0.97
```

This is much more meaningful.

---

# 33. Confidence Calibration Without ML

Calibration can remain deterministic.

Build bins such as:

```text
score band
margin band
evidence channel count
stability flag
candidate ID
top1/top2 pair
```

For each bin, calculate:

```text
observed precision
```

Preferably also calculate a conservative lower confidence bound.

The production confidence is then a lookup / rule result.

No ML inference is required.

---

# 34. Candidate-Specific Safety Policies

Do not use one global threshold.

Example:

```text
MISSING EVENT
```

may be easy to distinguish.

But:

```text
ORDER STATUS
TRACK ORDER
```

may be highly confusable.

Each candidate should have:

```json
{
  "candidate": "TRACK_ORDER",
  "min_raw_score": 0.0,
  "min_margin": 0.0,
  "min_evidence_channels": 0,
  "min_empirical_precision": 0.0
}
```

Values should be derived from regression data.

---

# 35. Pair-Specific Safety Policies

For confusing pairs:

```text
top1 = TRACK_ORDER
top2 = ORDER_STATUS
```

require a stronger margin than for:

```text
top1 = UPDATE_DESTINATION
top2 = MISSING_EVENT
```

This preserves more safe automation while controlling false positives.

---

# 36. Final Runtime Decision States

Internally the engine may calculate many scores.

Externally use only two operational states:

```text
SAFE_AUTO
MANUAL
```

Optional UI/debug state:

```text
SUGGESTION_ONLY
```

But suggestion-only should not trigger downstream automation.

Recommended semantics:

```text
SAFE_AUTO:
measured precision for this decision pattern is high enough

SUGGESTION_ONLY:
prediction exists but is not safe enough for downstream action

MANUAL:
insufficient / contradictory / ambiguous evidence
```

---

# 37. Output Contract

Recommended output:

```json
{
  "result": "SAFE_AUTO",
  "predicted_output": "FLOW_INVESTIGATION",
  "confidence": 0.97,
  "raw_score": 0.84,
  "second_best": "MISSING_EVENT",
  "second_best_score": 0.61,
  "margin": 0.23,
  "evidence_channel_count": 4,
  "winner_stable": true,
  "historical_case_agreement": 0.88,
  "supporting_spans": [
    "the expected downstream event is absent",
    "the user wants to identify where the flow stopped"
  ]
}
```

For unsafe cases:

```json
{
  "result": "MANUAL",
  "predicted_output": "FLOW_INVESTIGATION",
  "confidence": 0.71,
  "auto_process": false
}
```

The predicted output may still be retained for analytics/debugging.

---

# 38. Manual Intervention Cost Model

For every 100 messages:

```text
Current system:
2 manual interventions each
```

Therefore:

```text
baseline = 200 interventions
```

Let:

```text
C = correct auto-predictions
F = false auto-predictions
U = manual / unknown
```

where:

```text
C + F + U = 100
```

New manual intervention count:

```text
C * 1
+
F * 4
+
U * 2
```

So:

```text
New interventions = C + 4F + 2U
```

Substituting:

```text
U = 100 - C - F
```

gives:

```text
New interventions = 200 - C + 2F
```

Therefore:

```text
Interventions saved = C - 2F
```

This is the core optimization formula.

---

# 39. Break-Even Precision

For automated predictions alone:

```text
correct saves 1
false costs 2
```

Break-even occurs when:

```text
C = 2F
```

Thus accepted precision at break-even is:

```text
C / (C + F)
=
2 / 3
=
66.7%
```

But production should never run near this point.

Recommended target:

```text
96–98% precision among SAFE_AUTO predictions
```

because false routing has a much worse user experience than abstention.

---

# 40. Planning Numbers — Final Challenged View

These numbers are **not benchmark claims**.

They are engineering planning assumptions based on:

```text
50–60 output candidates
2–4 words per candidate
50–150-word uncontrolled runtime text
offline lexical/corpus knowledge
verified historical examples
strict abstention
false-positive-sensitive automation
```

---

## 40.1 Weak-History Case

Assumptions:

```text
few verified historical messages
highly overlapping candidate outputs
limited case coverage
```

Planning:

```json
{
  "total": 100,
  "correct_auto": 42,
  "false_auto": 2,
  "manual": 56,
  "auto_precision_percent": 95.5
}
```

Manual interventions:

```text
42 + 8 + 112 = 162
```

Reduction:

```text
200 -> 162
19%
```

---

## 40.2 Conservative Production Case

```json
{
  "total": 100,
  "correct_auto": 50,
  "false_auto": 2,
  "manual": 48,
  "auto_precision_percent": 96.2
}
```

Manual interventions:

```text
50 + 8 + 96 = 154
```

Reduction:

```text
23%
```

---

## 40.3 Base Planning Case

This is the number the project should plan around **if a useful verified history exists**.

```json
{
  "total": 100,
  "correct_auto": 57,
  "false_auto": 2,
  "manual": 41,
  "accepted_predictions": 59,
  "auto_precision_percent": 96.6,
  "manual_interventions_before": 200,
  "manual_interventions_after": 147,
  "manual_interventions_saved": 53,
  "manual_intervention_reduction_percent": 26.5
}
```

---

## 40.4 Strong Result

Assumptions:

```text
good historical coverage
candidate outputs reasonably separable
stable vocabulary
good calibration
```

```json
{
  "total": 100,
  "correct_auto": 62,
  "false_auto": 2,
  "manual": 36,
  "auto_precision_percent": 96.9
}
```

Manual interventions:

```text
62 + 8 + 72 = 142
```

Reduction:

```text
29%
```

---

## 40.5 Stretch Result

This should not be committed as the business case.

```json
{
  "total": 100,
  "correct_auto": 65,
  "false_auto": 2,
  "manual": 33,
  "auto_precision_percent": 97.0
}
```

Reduction:

```text
200 -> 139
30.5%
```

Treat this as upside.

---

# 41. Final Recommended Planning Target

Use:

```json
{
  "messages": 100,
  "correct_auto_predictions": 57,
  "false_auto_predictions": 2,
  "manual_or_abstained": 41,
  "safe_automation_rate_percent": 59,
  "safe_auto_precision_percent": 96.6,
  "manual_intervention_reduction_percent": 26.5
}
```

Do not use:

```text
70 correct
```

as the base project commitment.

That should be considered an aggressive stretch scenario until measured.

---

# 42. What Could Make the Numbers Better?

Correct automation can exceed the base case if:

```text
candidate outputs are semantically distinct
historical examples are plentiful
historical labels are clean
many outputs have repeated user-language patterns
candidate confusion is low
input language is stable over time
```

---

# 43. What Could Make the Numbers Worse?

Coverage can fall significantly if:

```text
many outputs differ only subtly
historical labels are inconsistent
many messages contain multiple intents
message wording changes significantly over time
historical examples are sparse for many outputs
short labels are ambiguous even to humans
user text contains little evidence of the correct output
```

If humans frequently disagree on the correct output from the same message, the symbolic engine will also struggle.

---

# 44. Historical Dataset Requirements

There is no absolute minimum, but useful planning guidance is:

```text
< 5 examples per candidate:
case retrieval contributes little

10–20 examples per candidate:
initial useful signal

30–50 examples per candidate:
good retrieval coverage

100+ examples per candidate:
strong opportunity for robust case profiles
```

Distribution matters more than raw total.

A dataset of:

```text
5,000 cases
```

is not useful if one output owns:

```text
4,500
```

and many outputs have almost none.

---

# 45. Data Quality Rules

Historical cases must have:

```text
verified final output
clean message text
no obviously wrong old labels
stable candidate IDs
timestamp
```

Recommended fields:

```text
message_id
timestamp
raw_text
normalized_text
final_output
verification_source
quality_flag
```

Exclude:

```text
known mistakes
temporary workaround labels
test data
synthetic garbage
```

from the production case library.

---

# 46. Time-Aware Case Retrieval

Language changes over time.

Prefer newer cases slightly when two historical matches are otherwise equal.

Example:

```text
retrieval_score
*
recency_weight
```

Do not let recency dominate semantics.

Suggested light decay only.

---

# 47. Candidate Drift Monitoring

Track every month / release:

```text
safe auto rate
safe auto precision
false auto count
unknown rate
top confusion pairs
candidate-level precision
```

If a candidate’s precision deteriorates:

```text
automatically tighten its acceptance policy
```

until investigation is complete.

---

# 48. Shadow Mode Before Automation

The system should first run without changing the existing workflow.

For each production message:

```text
predict
record confidence
record evidence
do NOT automate
wait for actual manual resolution
compare prediction vs truth
```

This produces real production calibration.

Recommended shadow period:

```text
enough messages to obtain meaningful examples
for all major candidates
```

Do not choose a fixed calendar duration if volume varies.

---

# 49. Chronological Regression Design

Avoid random leakage from future cases.

Recommended:

```text
Oldest 60–70%
    -> historical case library

Next 15–20%
    -> threshold / calibration dataset

Newest 15–20%
    -> untouched final regression
```

Why:

At runtime, future cases do not exist.

Chronological testing better simulates reality.

---

# 50. Regression Categories

Every regression suite should intentionally include:

```text
exact canonical wording
close synonym wording
indirect paraphrase
long background-heavy text
important clue at beginning
important clue at end
scattered evidence
negative statement
contradictory statement
two competing concepts
multi-intent message
typos
poor grammar
very short message
completely out-of-scope message
highly confusable candidate pair
```

---

# 51. Synthetic Regression Examples

These examples are technical and non-domain-specific.

---

## Case A — Exact-like

Candidate:

```text
MISSING EVENT
```

Message:

```text
The downstream event is missing after the process completed.
```

Expected:

```text
SAFE_AUTO
```

Reason:

```text
strong lexical + semantic + case agreement
```

---

## Case B — Paraphrase

Candidate:

```text
MISSING EVENT
```

Message:

```text
The upstream service completed normally, but the next expected
step never appeared in the target system.
```

Expected:

```text
SAFE_AUTO if corpus + historical cases agree strongly
```

---

## Case C — Background Contamination

Message:

```text
The order status was checked yesterday and everything looked normal.
Today the user wants the destination updated before the next routing step.
```

Expected:

```text
UPDATE DESTINATION
```

not:

```text
ORDER STATUS
```

Reason:

```text
request-focused clause should dominate historical background
```

---

## Case D — Scattered Evidence

Message:

```text
The record entered application A successfully.
The downstream application has no corresponding event.
Intermediate processing appears complete.
The user wants us to determine where the sequence stopped.
```

Expected:

```text
FLOW INVESTIGATION
```

Reason:

```text
distributed evidence
```

---

## Case E — Ambiguous

Message:

```text
The user says the flow does not look right and wants someone to check it.
```

Possible candidates:

```text
FLOW INVESTIGATION
MISSING EVENT
ROUTING FAILURE
```

Expected:

```text
MANUAL
```

unless historical evidence creates a very clear winner.

---

## Case F — Contradiction

Message:

```text
The downstream event is present.
The user is only asking for the current order status.
```

Expected:

```text
ORDER STATUS
```

Strong contradiction should suppress:

```text
MISSING EVENT
```

---

# 52. Regression Truth Table

Maintain a table:

| Test ID | Expected output | Actual top1 | SAFE_AUTO? | Correct? | False auto? | Evidence issue |
|---|---|---|---|---|---|---|
| T001 | MISSING_EVENT | MISSING_EVENT | Yes | Yes | No | — |
| T002 | UPDATE_DESTINATION | ORDER_STATUS | No | No | No | abstained safely |
| T003 | FLOW_INVESTIGATION | FLOW_INVESTIGATION | Yes | Yes | No | distributed evidence |
| T004 | MANUAL | ROUTING_FAILURE | No | — | No | insufficient margin |

The important distinction is:

```text
wrong top1 + MANUAL
```

is not a false automated prediction.

Only:

```text
wrong top1 + SAFE_AUTO
```

counts as a false auto.

---

# 53. Primary Evaluation Metrics

Track:

```text
Correct SAFE_AUTO
False SAFE_AUTO
Manual / abstained
SAFE_AUTO precision
SAFE_AUTO coverage
manual interventions saved
manual intervention reduction %
```

Secondary:

```text
top1 raw accuracy
top2 recall
candidate-level precision
candidate-level coverage
confusion pairs
unknown rate
```

---

# 54. Most Important Metric

The most useful business metric is:

```text
manual interventions saved
=
correct_auto - 2 * false_auto
```

Example:

```text
57 - 2*2
=
53
```

So:

```text
53 manual interventions saved per 100 messages
```

---

# 55. Optimization Strategy

Do not optimize directly for:

```text
top1 accuracy
```

Instead:

```text
Step 1:
minimize false SAFE_AUTO

Step 2:
maximize correct SAFE_AUTO while keeping false SAFE_AUTO within budget

Step 3:
optimize candidate-specific thresholds

Step 4:
optimize confusion-pair policies
```

---

# 56. False-Auto Budget

Recommended initial production objective:

```text
<= 2–3 false SAFE_AUTO predictions per 100 messages
```

This is significantly stricter than the earlier:

```text
< 10
```

because the downstream economics make false routing expensive.

---

# 57. Optional Downstream Pre-Execution Validation

If the downstream flow exposes read-only checks, use them.

Example:

```text
Prediction:
MISSING EVENT
```

Before automation:

```text
Can the system verify that the expected event is actually absent?
```

Or:

```text
Prediction:
UPDATE DESTINATION
```

Check:

```text
Is a destination update operation applicable to this record?
```

This is domain-specific and optional.

If available, it can greatly reduce false automation.

Treat it as:

```text
last safety gate
```

not the semantic matcher itself.

---

# 58. Runtime Performance

Only:

```text
50–60 candidates
```

exist.

Therefore the system can afford richer deterministic scoring.

Most expensive resources should be precomputed.

At runtime:

```text
normalize message
generate windows
query local indexes
retrieve historical cases
score 50–60 candidates
run safety gates
```

This should be practical on a normal application server with well-designed local indexes.

---

# 59. Java-Oriented Component Design

Suggested packages:

```text
semanticrouting/
    normalize/
    segment/
    window/
    lexical/
    wordnet/
    corpus/
    history/
    profile/
    evidence/
    contrast/
    score/
    confidence/
    gate/
    evaluation/
    api/
```

Services:

```java
TextNormalizer
SentenceSegmenter
ClauseSegmenter
WindowGenerator

LexicalEvidenceService
WordNetService
SenseDisambiguator
GlossEvidenceService
CorpusSemanticService
CooccurrenceService

HistoricalCaseIndex
HistoricalCaseRetriever
HistoricalProfileBuilder

CandidateProfileBuilder
CandidateDiscriminator
DistributedEvidenceAggregator
WinnerStabilityEvaluator

EvidenceFusionEngine
ConfidenceCalibrationService
CandidateAcceptancePolicy
PairwiseConfusionPolicy

SemanticRoutingEngine
```

---

# 60. Candidate Profile Model

```java
class CandidateProfile {
    String candidateId;
    String canonicalText;

    Set<String> normalizedTerms;
    Set<String> canonicalPhrases;

    Map<String, Double> wordNetConcepts;
    Map<String, Double> glossTerms;
    Map<String, Double> corpusConcepts;
    Map<String, Double> cooccurrenceTerms;

    List<String> historicalCaseIds;
    Map<String, Double> historicalDiscriminators;

    Set<String> confusableCandidates;
    Map<String, Double> componentWeights;

    AcceptancePolicy acceptancePolicy;
}
```

---

# 61. Runtime Candidate Score

```java
class CandidateScore {
    String candidateId;

    double lexicalScore;
    double wordNetScore;
    double glossScore;
    double corpusScore;
    double cooccurrenceScore;

    double historicalCaseScore;
    double historicalProfileScore;

    double localEvidenceScore;
    double distributedEvidenceScore;
    double componentCoverageScore;
    double contrastiveScore;
    double winnerStabilityScore;

    double contradictionPenalty;
    double ambiguityPenalty;

    double rawScore;
    double empiricalConfidence;
}
```

---

# 62. Prediction Response

```java
class SemanticPrediction {
    Decision decision; // SAFE_AUTO or MANUAL

    String predictedCandidate;
    double empiricalConfidence;

    String secondBestCandidate;
    double margin;

    int strongEvidenceChannels;
    boolean winnerStable;

    List<EvidenceSpan> supportingEvidence;
}
```

---

# 63. High-Level Runtime Algorithm

```text
function predict(message):

    normalized = normalize(message)

    sentences = sentenceSplit(normalized)
    clauses = clauseSplit(normalized)

    windows = buildMultiScaleWindows(
        clauses,
        sentences,
        wholeMessage
    )

    lexicalEvidence = lexicalEngine(windows)
    wordnetEvidence = wordnetEngine(windows)
    glossEvidence = glossEngine(windows)
    corpusEvidence = corpusEngine(windows)

    historicalCases = historicalCaseRetriever(message)
    historicalEvidence = aggregateHistoricalCases(historicalCases)

    distributedEvidence =
        aggregateDistributedEvidence(windows)

    for each candidate:

        score candidate from:
            lexical
            WordNet
            gloss
            corpus
            historical case retrieval
            historical profile
            local evidence
            distributed evidence

        calculate:
            component coverage
            contrastive evidence
            contradiction
            stability

        fuse into raw score

    rank candidates

    top1 = candidate[0]
    top2 = candidate[1]

    confidence =
        lookupEmpiricalConfidence(
            candidate=top1,
            rawScore,
            margin,
            evidenceAgreement,
            stability,
            top2Pair
        )

    if candidatePolicyFails:
        return MANUAL

    if pairwiseConfusionGateFails:
        return MANUAL

    if empiricalPrecisionBelowRequired:
        return MANUAL

    if contradictionTooStrong:
        return MANUAL

    return SAFE_AUTO
```

---

# 64. Implementation Phases

## Phase 1 — Baseline Retrieval

Build:

```text
normalization
sentence/clause windows
exact / fuzzy
BM25
WordNet
gloss
```

Measure:

```text
top1 correctness
false SAFE_AUTO potential
```

Do not automate yet.

---

## Phase 2 — Historical Case Layer

Add:

```text
verified historical index
case retrieval agreement
candidate historical profiles
candidate IDF
```

Expected effect:

```text
largest increase in domain-specific semantic coverage
```

---

## Phase 3 — Offline Corpus Semantics

Add:

```text
Wikipedia/Wiktionary corpus index
ESA-style concept retrieval
second-order co-occurrence
```

Expected effect:

```text
better unseen paraphrase handling
```

---

## Phase 4 — Long-Message Handling

Add:

```text
multi-scale scoring
distributed evidence
request-focused weighting
```

Expected effect:

```text
less contamination from background text
```

---

## Phase 5 — False-Positive Control

Add:

```text
component coverage
contrastive candidate profiles
winner stability
candidate-specific thresholds
pairwise confusion policies
contradiction veto
```

Expected effect:

```text
reduce false SAFE_AUTO
```

---

## Phase 6 — Calibration and Shadow Mode

Use real production outcomes.

Derive:

```text
empirical confidence
candidate-specific policies
pair-specific policies
```

Goal:

```text
96–98% precision in SAFE_AUTO bucket
```

---

# 65. Go-Live Criteria

Do not enable downstream automation until the held-out / shadow dataset demonstrates:

```text
SAFE_AUTO precision >= 96%
false SAFE_AUTO <= agreed budget
manual intervention reduction is positive
no major candidate has unacceptable precision
```

Recommended initial false budget:

```text
<= 2–3 per 100
```

---

# 66. Progressive Rollout

Recommended rollout:

```text
Stage 1:
shadow only

Stage 2:
automate only easiest / highest-precision candidates

Stage 3:
add more candidates after candidate-level validation

Stage 4:
expand coverage as confidence calibration improves
```

There is no requirement to automate all 50–60 outputs from day one.

This can materially improve safety.

---

# 67. Candidate Tiering

After regression, divide candidates into:

```text
Tier A:
highly separable
safe automation early

Tier B:
moderately confusable
stricter gates

Tier C:
highly ambiguous
manual by default
```

This can create better total business value than forcing one system-wide policy.

---

# 68. Recommended Operational Philosophy

The system should behave like:

> “I will automate only when I can defend the prediction.”

Not:

> “I must always choose the closest output.”

This distinction is the core architectural decision.

---

# 69. Final Technical Recommendation

The recommended production solution is:

```text
Short canonical outputs
        +
WordNet / OEWN
        +
offline English corpus
        +
verified historical resolved messages
        +
BM25 / sparse retrieval
        +
multi-scale message windows
        +
distributed evidence
        +
contrastive candidate scoring
        +
candidate component coverage
        +
winner stability
        +
empirical confidence calibration
        +
candidate / pair-specific safety gates
        +
abstention
```

No component alone is expected to solve the problem.

The value comes from:

```text
independent evidence + historical domain language + strict verification
```

---

# 70. Final Planning Position

For a realistic implementation with useful verified historical data:

```json
{
  "total_messages": 100,
  "correct_safe_auto_predictions": 57,
  "false_safe_auto_predictions": 2,
  "manual_or_abstained": 41,
  "safe_auto_rate_percent": 59,
  "safe_auto_precision_percent": 96.6,
  "baseline_manual_interventions": 200,
  "projected_manual_interventions": 147,
  "projected_manual_interventions_saved": 53,
  "projected_manual_reduction_percent": 26.5
}
```

### Confidence in these planning numbers

Interpretation:

```text
Not a promise.
Not a published benchmark.
Not a mathematical guarantee.
```

It is the **base engineering hypothesis** to validate.

The most important falsification test is:

> On chronologically held-out historical messages, can the system achieve approximately 96–98% precision while safely auto-routing roughly half or more of all messages?

If yes:

```text
the solution has strong operational value.
```

If safe automation falls below roughly:

```text
35–40%
```

the business value should be recalculated before further investment.

If false SAFE_AUTO remains above:

```text
3–4%
```

after candidate-specific calibration:

```text
do not expand automation;
tighten the acceptance layer or limit automation to Tier A candidates.
```

---

# 71. One-Sentence Architecture Summary

> **Retrieve semantic evidence from offline English knowledge and real historical resolved cases, let the 50–60 short outputs compete, verify the winner against its nearest competitors, and automate only when historical evidence shows that this exact decision pattern is reliably correct.**
