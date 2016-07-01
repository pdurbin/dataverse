ALTER TABLE authenticateduser ADD COLUMN lockeduntil timestamp without time zone;
ALTER TABLE builtinuser ADD COLUMN badlogins integer;
