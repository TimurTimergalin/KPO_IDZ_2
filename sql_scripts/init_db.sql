CREATE TABLE dishes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    count INT NOT NULL CHECK ( count >= 0 ),
    cooking_time_min INT NOT NULL CHECK ( dishes.cooking_time_min > 0 ),
    price FLOAT NOT NULL CHECK ( price > 0 )
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(128) UNIQUE NOT NULL CHECK ( login ~ '[A-Za-z0-9_]+'),
    hashed_pass VARCHAR(256) NOT NULL CHECK ( length(hashed_pass) > 0),
    role VARCHAR(6) NOT NULL CHECK ( role LIKE 'admin' OR role LIKE 'client')
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    dish_id BIGINT NOT NULL REFERENCES dishes (id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK ( 1 <= rating AND rating <= 5),
    text TEXT NOT NULL,
    UNIQUE (author_id, dish_id)
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    finish_reason VARCHAR(100) CHECK ( finish_reason IS null OR finish_reason LIKE 'paid' OR finish_reason LIKE 'cancelled' OR finish_reason LIKE 'refused'),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP
);

CREATE TABLE orders_to_dishes (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders (id) ON DELETE CASCADE,
    dish_id BIGINT REFERENCES dishes (id) ON DELETE CASCADE,
    order_time TIMESTAMP NOT NULL,
    cooking_start_time TIMESTAMP,
    cooking_finishing_time TIMESTAMP,
    status VARCHAR(9) NOT NULL CHECK ( status LIKE 'ordered' OR status LIKE 'cooking' OR status LIKE 'finished' OR status LIKE 'cancelled' )
);
