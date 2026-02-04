--https://dbdiagram.io/d/6802694f1ca52373f582483e

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    chat_name VARCHAR(100),
    chat_id BIGINT UNIQUE,
    email VARCHAR(100) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'NEW',
    birthday DATE,
    messenger VARCHAR(50) NOT NULL,
    balance INTEGER DEFAULT 0,
    language VARCHAR(10) DEFAULT 'ENG',
    user_site_status VARCHAR(50) DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS lessons (
    lesson_id SERIAL PRIMARY KEY,
    student_id INT REFERENCES users(user_id) NOT NULL,
    trainer_id INT REFERENCES users(user_id) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_min INT,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
--    recap/to_do убрать потом
    cost INT DEFAULT 0,
    for_payment INT DEFAULT 0,
    google_event_id VARCHAR UNIQUE,
    title VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS payments (
   payment_id SERIAL PRIMARY KEY,
   student_id INT REFERENCES users(user_id) NOT NULL,
    trainer_id INT REFERENCES users(user_id) NOT NULL,
   date_of_payment TIMESTAMP NOT NULL,
   amount INT NOT NULL
);

CREATE TABLE if NOT EXISTS student_info (
info_id SERIAL PRIMARY KEY,
student_id INT REFERENCES users(user_id) NOT NULL,
    trainer_id INT REFERENCES users(user_id) NOT NULL,
info_date DATE NOT NULL,
status VARCHAR(50) NOT NULL DEFAULT 'ACTUAL',
info TEXT NOT NULL
);

    CREATE TABLE IF NOT EXISTS free_slots (
       slot_id SERIAL PRIMARY KEY,
        trainer_id INT REFERENCES users(user_id) NOT NULL,
       start_date_time TIMESTAMP NOT NULL,
       end_date_time TIMESTAMP NOT NULL
    );

    create table if not exists trainer_student (
        id SERIAL PRIMARY KEY,
        student_id INT REFERENCES users(user_id) NOT NULL,
        trainer_id INT REFERENCES users(user_id) NOT NULL,
        UNIQUE (student_id, trainer_id)
    );

create table if not exists schedule (
    id serial PRIMARY KEY,
    student_id INT REFERENCES users(user_id) NOT NULL,
    trainer_id INT REFERENCES users(user_id) NOT NULL,
    schedule_day VARCHAR(50),
    schedule_time TIME
);
