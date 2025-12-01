CREATE SEQUENCE musicband_seq START 1 INCREMENT 1;

CREATE TABLE album(
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL CHECK (length(trim(name))>0),
  sales INTEGER NOT NULL CHECK (sales>0)
);

CREATE TABLE location(
  id BIGSERIAL PRIMARY KEY,
  x BIGINT NOT NULL,
  y REAL NOT NULL,
  name TEXT NOT NULL CHECK (length(name)<=205)
);

CREATE TABLE person(
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL CHECK (length(trim(name))>0),
  eye_color TEXT,
  hair_color TEXT,
  location_id BIGINT REFERENCES location(id) ON DELETE RESTRICT,
  birthday DATE NOT NULL,
  weight BIGINT CHECK (weight IS NULL OR weight>0),
  nationality TEXT
);

CREATE TABLE music_band(
  id BIGINT PRIMARY KEY DEFAULT nextval('musicband_seq'),
  name TEXT NOT NULL CHECK (length(trim(name))>0),
  creation_date TIMESTAMP NOT NULL DEFAULT now(),
  genre TEXT NOT NULL,
  number_of_participants BIGINT CHECK (number_of_participants IS NULL OR number_of_participants>0),
  singles_count INTEGER NOT NULL CHECK (singles_count>0),
  description TEXT NOT NULL,
  best_album_id BIGINT REFERENCES album(id) ON DELETE RESTRICT,
  albums_count BIGINT NOT NULL CHECK (albums_count>0),
  front_man_id BIGINT NOT NULL REFERENCES person(id) ON DELETE RESTRICT,
  coord_x REAL NOT NULL CHECK (coord_x<=231),
  coord_y DOUBLE PRECISION NOT NULL CHECK (coord_y<=879)
);
