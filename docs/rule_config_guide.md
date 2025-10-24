# DocFlow Rule Configuration Guide

## Overview
This document explains how DocFlow manages configurable validation rules for document closure. These rules allow administrators to enforce organization-specific policies without modifying any Java code.

---

## Table: `DOC_RULES`

| Column | Description | Example Value |
|:--|:--|:--|
| `RULE_ID` | Auto-generated primary key | 1 |
| `RULE_TYPE` | Logical name of the rule | `CHILD_STATUS_CHECK` |
| `DESCRIPTION` | Description of the rule purpose | "Ensures all child docs are terminal (approved/rejected) before parent closure." |
| `ENABLED_FLAG` | Y/N toggle to activate the rule | `Y` |
| `DATE_CHECK_FLAG` | Y/N toggle for date-based rules | `N` |
| `EXPRESSION` | SQL predicate used during rule evaluation | `STATUS IN ('APPROVED','REJECTED')` |
| `CREATED_ON` | Creation timestamp | `SYSDATE` |

---

## Base Inserts (Default Configuration)

```sql
-- Rule 1: All child docs must be terminal (APPROVED or REJECTED)
INSERT INTO DOC_RULES (RULE_ID, RULE_TYPE, DESCRIPTION, ENABLED_FLAG, DATE_CHECK_FLAG, EXPRESSION, CREATED_ON)
VALUES (1, 'CHILD_STATUS_CHECK',
        'Ensures all child docs are terminal (approved or rejected) before allowing parent closure.',
        'Y', 'N', 'STATUS IN (''APPROVED'',''REJECTED'')', SYSDATE);

-- Rule 2: Child date validation (example rule)
INSERT INTO DOC_RULES (RULE_ID, RULE_TYPE, DESCRIPTION, ENABLED_FLAG, DATE_CHECK_FLAG, EXPRESSION, CREATED_ON)
VALUES (2, 'DATE_VALIDATION',
        'Ensures all child records have effective dates not in the future before closure.',
        'Y', 'Y', 'EFFECTIVE_DATE <= SYSDATE', SYSDATE);
```

---

## How to Modify Rules

| Requirement | SQL Action |
|--------------|------------|
| Disable a rule temporarily | `UPDATE DOC_RULES SET ENABLED_FLAG = 'N' WHERE RULE_TYPE = 'X';` |
| Change logic to support new statuses | `UPDATE DOC_RULES SET EXPRESSION = 'STATUS IN (''APPROVED'',''REJECTED'',''CLOSED'')' WHERE RULE_TYPE = 'CHILD_STATUS_CHECK';` |
| Add a new validation | Insert a new row with unique `RULE_TYPE` |
| Disable all rules (debug/testing) | `UPDATE DOC_RULES SET ENABLED_FLAG = 'N';` |

---

## Evaluation Rules

- Rules are executed **in sequence**.
- Evaluation stops **at the first failure** (short-circuit logic).
- Behavior is controlled in configuration:

```yaml
docflow:
  rules:
    shortcircuit: true
```

---

## Example Workflow

1. Admin inserts/updates configuration in `DOC_RULES` table.
2. When the user triggers closure validation (e.g., via `GET /api/rules/evaluate/{docId}`), DocFlow reads all active rules.
3. The engine executes each expression against `DOC_CHILD` data.
4. If any rule fails, evaluation stops and reasons are returned in the response JSON.
5. If all rules pass, the document can move to `COMPLETED` status.

---

## Example Response JSON
```json
{
  "docId": 1,
  "canClose": false,
  "reasons": [
    "Child date in future violates DATE_VALIDATION rule"
  ]
}
```

---

## Notes for Client Admins
- All logic is **DB-driven**; no Java code changes are needed.
- Rule expressions can use any valid SQL predicate syntax.
- The system logs evaluated rule outcomes for audit.
- Clients may extend these rules per their compliance framework.

---

**End of Document**

