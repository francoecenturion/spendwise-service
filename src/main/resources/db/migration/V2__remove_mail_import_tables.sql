-- Remove the deprecated Gmail/IMAP mail-import tables from databases that
-- were created with an earlier version of V1.

DROP TABLE IF EXISTS spendwise.mail_import;
DROP TABLE IF EXISTS spendwise.gmail_credential;
