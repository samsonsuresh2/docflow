-- ⚙️ Add new columns to store upload metadata (non-breaking; all nullable)
ALTER TABLE DOC_PARENT ADD (
    MIME_TYPE       VARCHAR2(255 CHAR),
    FILE_SIZE       NUMBER(19,0),
    STORAGE_PATH    VARCHAR2(1000 CHAR),
    
);

COMMENT ON COLUMN DOCPARENT.MIME_TYPE    IS 'MIME type of uploaded file (e.g., application/vnd.openxmlformats-officedocument.wordprocessingml.document)';
COMMENT ON COLUMN DOCPARENT.FILE_SIZE    IS 'Size of uploaded file in bytes';
COMMENT ON COLUMN DOCPARENT.STORAGE_PATH IS 'Filesystem path where document is stored';
COMMENT ON COLUMN DOCPARENT.UPLOADED_BY  IS 'Username of uploader';
COMMENT ON COLUMN DOCPARENT.UPLOADED_AT  IS 'Timestamp when upload occurred';