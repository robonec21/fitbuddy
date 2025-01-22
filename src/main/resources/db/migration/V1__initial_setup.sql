CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE workout_programs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE workout_days (
    id BIGSERIAL PRIMARY KEY,
    day_of_week VARCHAR(20) NOT NULL,
    workout_program_id BIGINT NOT NULL,
    FOREIGN KEY (workout_program_id) REFERENCES workout_programs(id) ON DELETE CASCADE
);

CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    default_sets INTEGER,
    default_reps_per_set INTEGER,
    default_rest_period_between_sets INTEGER,
    media_link VARCHAR(500)
);

CREATE TABLE workout_day_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_day_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    order_index INTEGER NOT NULL,
    sets INTEGER NOT NULL,
    reps_per_set INTEGER NOT NULL,
    rest_period_seconds INTEGER,
    notes VARCHAR(500),
    FOREIGN KEY (workout_day_id) REFERENCES workout_days(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id)
);

CREATE TABLE progress_logs (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    notes TEXT,
    workout_program_id BIGINT NOT NULL,
    workout_day_id BIGINT,
    FOREIGN KEY (workout_program_id) REFERENCES workout_programs(id),
    FOREIGN KEY (workout_day_id) REFERENCES workout_days(id)
);

CREATE TABLE exercise_progresses (
    id BIGSERIAL PRIMARY KEY,
    progress_log_id BIGINT NOT NULL,
    workout_day_exercise_id BIGINT,
    replacement_exercise_id BIGINT,
    order_index INTEGER NOT NULL,
    actual_sets INTEGER NOT NULL,
    rest_period_seconds INTEGER,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    skipped BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(500),
    FOREIGN KEY (progress_log_id) REFERENCES progress_logs(id) ON DELETE CASCADE,
    FOREIGN KEY (workout_day_exercise_id) REFERENCES workout_day_exercises(id),
    FOREIGN KEY (replacement_exercise_id) REFERENCES exercises(id)
);

CREATE TABLE exercise_progress_set_details (
    exercise_progress_id BIGINT NOT NULL,
    set_number INTEGER NOT NULL,
    reps_per_set INTEGER,
    weight_per_set DOUBLE PRECISION,
    FOREIGN KEY (exercise_progress_id) REFERENCES exercise_progresses(id) ON DELETE CASCADE,
    PRIMARY KEY (exercise_progress_id, set_number)
);

CREATE INDEX idx_workout_program_user ON workout_programs(user_id);
CREATE INDEX idx_workout_day_program ON workout_days(workout_program_id);
CREATE INDEX idx_workout_day_exercise_workout_day ON workout_day_exercises(workout_day_id);
CREATE INDEX idx_workout_day_exercise_exercise ON workout_day_exercises(exercise_id);
CREATE INDEX idx_workout_day_exercise_order ON workout_day_exercises(workout_day_id, order_index);
CREATE INDEX idx_progress_log_workout ON progress_logs(workout_program_id);
CREATE INDEX idx_progress_log_day ON progress_logs(workout_day_id);
CREATE INDEX idx_exercise_progress_log ON exercise_progresses(progress_log_id);
CREATE INDEX idx_exercise_progress_workout_exercise ON exercise_progresses(workout_day_exercise_id);
CREATE INDEX idx_exercise_progress_replacement ON exercise_progresses(replacement_exercise_id);
CREATE INDEX idx_exercise_progress_set_details ON exercise_progress_set_details(exercise_progress_id);
