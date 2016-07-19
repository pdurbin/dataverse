ALTER TABLE authenticateduser ADD COLUMN lockeduntil timestamp without time zone;
ALTER TABLE authenticateduser ADD COLUMN badlogins integer;
