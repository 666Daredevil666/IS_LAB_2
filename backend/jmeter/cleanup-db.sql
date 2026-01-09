TRUNCATE import_operation, outbox_event, delivery_log, dead_letter_queue RESTART IDENTITY CASCADE;

DELETE
FROM music_band
WHERE id NOT IN (SELECT id
                 FROM music_band
                 ORDER BY id LIMIT 10
    );

DELETE
FROM album
WHERE id NOT IN (SELECT DISTINCT best_album_id
                 FROM music_band
                 WHERE best_album_id IS NOT NULL
                 UNION
                 SELECT 1);

DELETE
FROM person
WHERE id NOT IN (SELECT DISTINCT front_man_id
                 FROM music_band
                 WHERE front_man_id IS NOT NULL
                 UNION
                 SELECT 1);

DELETE
FROM location
WHERE id NOT IN (SELECT DISTINCT location_id
                 FROM person
                 WHERE location_id IS NOT NULL
                 UNION
                 SELECT 1);

SELECT setval('musicband_seq', (SELECT COALESCE(MAX(id), 1) FROM music_band), true);
SELECT setval('album_id_seq', (SELECT COALESCE(MAX(id), 1) FROM album), true);
SELECT setval('person_id_seq', (SELECT COALESCE(MAX(id), 1) FROM person), true);
SELECT setval('location_id_seq', (SELECT COALESCE(MAX(id), 1) FROM location), true);

SELECT (SELECT COUNT(*) FROM music_band) as bands,
       (SELECT COUNT(*) FROM album)      as albums,
       (SELECT COUNT(*) FROM person)     as persons,
       (SELECT COUNT(*) FROM location)   as locations;

