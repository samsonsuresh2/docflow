-- Example baseline rules (can be edited by admin later)

INSERT INTO DOC_RULES (RULE_TYPE, TARGET_ENTITY, EXPRESSION, DESCRIPTION)
VALUES ('CHILD_STATUS', 'DOC_CHILD', 'status = ''APPROVED''', 'All child records must be approved');

INSERT INTO DOC_RULES (RULE_TYPE, TARGET_ENTITY, EXPRESSION, DESCRIPTION)
VALUES ('DATE_CHECK', 'DOC_CHILD', 'expiry_date < SYSDATE', 'All child expiry dates must be in the past');

COMMIT;
