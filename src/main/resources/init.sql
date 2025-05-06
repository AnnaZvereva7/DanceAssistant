--https://dbdiagram.io/d/6802694f1ca52373f582483e

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    chat_name VARCHAR(100),
    chat_id BIGINT UNIQUE,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'NEW_USER',
    plans VARCHAR(255),
    birthday DATE,
    messenger VARCHAR(50) NOT NULL,
    balance INTEGER DEFAULT 0,
    language VARCHAR(10) DEFAULT 'ENG'
);
--
--INSERT INTO users (name, chat_name, email, role, birthday, chat_id, messenger) VALUES
-- ('Anna', 'AnnaZverevaMorozova','anna7489lamia@gmail.com','ADMIN','1990-01-23', 152942083, 'TELEGRAM');
-- ('Anton', 'zmalchunz',null,'NEW',null, 290556570, 'TELEGRAM'),
-- ('Alicja', 'Alicja', null, 'PERMANENT', null, 0, 'WHATSAPP')

CREATE TABLE IF NOT EXISTS lessons (
    lesson_id SERIAL PRIMARY KEY,
    student_id INT REFERENCES users(user_id) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    to_do VARCHAR(255),
    cost INT DEFAULT 0
);

--INSERT INTO lessons (student_id, date_time, status) VALUES
--(1, '2025-05-05 09:30', 'NEW'),
--(1, '2025-05-02 10:30', 'COMPLETED');
