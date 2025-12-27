TRUNCATE music_band, album, person, location, import_operation, outbox_event, delivery_log RESTART IDENTITY CASCADE;

INSERT INTO location (id, x, y, name)
VALUES (1, 10, 20.5, 'BaseLocation');
INSERT INTO person (id, name, eye_color, hair_color, nationality, weight, birthday, location_id)
VALUES (1, 'BaseFrontman', 'GREEN', 'WHITE', 'UNITED_KINGDOM', 70, '1980-01-01', 1);
INSERT INTO album (id, name, sales)
VALUES (1, 'BaseAlbum', 50000);

SELECT setval('location_id_seq', 1, true);
SELECT setval('person_id_seq', 1, true);
SELECT setval('album_id_seq', 1, true);

