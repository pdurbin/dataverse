ALTER TABLE datafile ADD COLUMN checksumtype character varying(255);
ALTER TABLE datafile ALTER COLUMN checksumtype SET NOT NULL;
UPDATE datafile SET checksumtype = 'MD5';
-- alternate statement for sbgrid.org and others interested in SHA1
--UPDATE datafile SET checksumtype = 'SHA1';
ALTER TABLE datafile RENAME md5 TO checksumvalue;
