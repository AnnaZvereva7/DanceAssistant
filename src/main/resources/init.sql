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

    CREATE TABLE IF NOT EXISTS slots (
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

    CREATE TABLE if not exists trainer_calendar_links (
        link_id BIGSERIAL PRIMARY KEY,
        trainer_id BIGINT NOT NULL,
        token TEXT NOT NULL UNIQUE,
        created_at TIMESTAMPTZ NOT NULL,
        expires BOOLEAN not NULL
    );

    create table if not exists spotify_tracks (
        track_id SERIAL PRIMARY KEY,
        spotify_track_id VARCHAR(255) NOT NULL,
        track_title VARCHAR(255),
        artist_name VARCHAR(255),
        bpm int NOT NULL
    );

    create table if not exists tags (
        tag_id SERIAL PRIMARY KEY,
        tag_name VARCHAR(255) NOT NULL UNIQUE
    );

    create table if not exists track_tags (
        track_id SERIAL REFERENCES spotify_tracks(track_id) ON DELETE CASCADE,
        tag_id INT REFERENCES tags(tag_id) ON DELETE CASCADE,
        PRIMARY KEY (track_id, tag_id)
    );

    create table if not exists telegram_code (
        code_id SERIAL PRIMARY KEY,
        code VARCHAR(255) NOT NULL UNIQUE,
        user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
        created_at TIMESTAMPTZ NOT NULL,
        expired_at TIMESTAMPTZ NOT NULL
    );

    create table if not exists calendar_event_sequences (
    lesson_id SERIAL PRIMARY KEY,
    sequence_number INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_calendar_event_sequences_lesson
    FOREIGN KEY (lesson_id)
        REFERENCES lessons(lesson_id)
        ON DELETE CASCADE
    );

    create table if not exists subscription_types (
        subscription_type_id SERIAL PRIMARY KEY,
        trainer_id BIGINT NOT NULL REFERENCES users(user_id),
        trainer_name VARCHAR(255) NOT NULL,
        duration_months INT NOT NULL,
        amount_lessons INT NOT NULL,
        price INT NOT NULL,
        active BOOLEAN NOT NULL,
        comment TEXT NOT NULL
    );

    create table if not exists students_subscriptions (
        student_subscription_id SERIAL PRIMARY KEY,
        subscription_type_id BIGINT REFERENCES subscription_types(subscription_type_id),
        student_id BIGINT NOT NULL REFERENCES users(user_id),
        student_name VARCHAR(255) NOT NULL,
        trainer_name VARCHAR(255) NOT NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        remaining_lessons INT NOT NULL,
        created_at TIMESTAMP NOT NULL,
        status VARCHAR(50) NOT NULL,
        comment TEXT
    );

    create table if not exists lesson_subscription_links (
        lesson_subscription_link_id SERIAL PRIMARY KEY,
        lesson_id BIGINT REFERENCES lessons(lesson_id) ON DELETE CASCADE,
        subscription_id BIGINT REFERENCES students_subscriptions(student_subscription_id) ON DELETE CASCADE
    );
