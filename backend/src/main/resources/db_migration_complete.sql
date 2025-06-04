ALTER TABLE flashcardprogress ALTER COLUMN learningstage TYPE integer USING learningstage::integer;

UPDATE flashcardprogress SET learningstage = 3 WHERE learningstage IS NULL;

ALTER TABLE flashcardprogress ALTER COLUMN learningstage SET NOT NULL;
