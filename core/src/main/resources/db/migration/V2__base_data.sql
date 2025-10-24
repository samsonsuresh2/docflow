-- =====================================================================
--  DocFlow : Base Reference Data (Version 2)
--  Inserts sample/seed records to validate schema connectivity.
-- =====================================================================

-- Insert a reference document record for smoke testing.
INSERT INTO DOC_METADATA (DOC_NAME, DOC_TYPE, UPLOADED_BY, STATUS)
VALUES ('Sample Template', 'POLICY', 'system', 'APPROVED');

COMMIT;

-- =====================================================================
-- END OF V2__base_data.sql
-- =====================================================================
