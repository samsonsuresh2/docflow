-- V3__add_doc_status_to_parent.sql
-- Adds document lifecycle status column to parent table
ALTER TABLE DOC_PARENT ADD (
    STATUS VARCHAR2(30) DEFAULT 'DRAFT'
);

COMMENT ON COLUMN DOC_PARENT.STATUS IS 'Current lifecycle state of the document (DRAFT/OPEN/REVIEW/APPROVED/REJECTED/COMPLETED)';
