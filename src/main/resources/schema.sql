-- =========================================================================
-- USERS
-- =========================================================================

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       status VARCHAR(50) NOT NULL,
                       is_email_verified BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- USER PROFILES
-- =========================================================================

CREATE TABLE user_profiles (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL UNIQUE,
                               participant_type VARCHAR(50),
                               student_code VARCHAR(100),
                               university_name VARCHAR(255),

                               CONSTRAINT fk_profile_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);

-- =========================================================================
-- EVENTS
-- =========================================================================

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        location VARCHAR(255),
                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP NOT NULL,
                        image_url TEXT,
                        max_participants INTEGER,
                        current_participants INTEGER DEFAULT 0,
                        status VARCHAR(50),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- RULES
-- =========================================================================

CREATE TABLE rules (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       event_id BIGINT NOT NULL,

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_rule_event
                           FOREIGN KEY (event_id)
                               REFERENCES events(id)
                               ON DELETE CASCADE
);

-- =========================================================================
-- CATEGORIES
-- =========================================================================

CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,

                            event_id BIGINT NOT NULL,

                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_category_event
                                FOREIGN KEY (event_id)
                                    REFERENCES events(id)
                                    ON DELETE CASCADE
);

-- =========================================================================
-- CATEGORY MENTORS
-- =========================================================================

CREATE TABLE category_mentors (
                                  category_id BIGINT NOT NULL,
                                  user_id BIGINT NOT NULL,

                                  PRIMARY KEY (category_id, user_id),

                                  FOREIGN KEY (category_id)
                                      REFERENCES categories(id)
                                      ON DELETE CASCADE,

                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);

-- =========================================================================
-- CATEGORY JUDGES
-- =========================================================================

CREATE TABLE category_judges (
                                 category_id BIGINT NOT NULL,
                                 user_id BIGINT NOT NULL,

                                 PRIMARY KEY (category_id, user_id),

                                 FOREIGN KEY (category_id)
                                     REFERENCES categories(id)
                                     ON DELETE CASCADE,

                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE
);

-- =========================================================================
-- TEAMS
-- =========================================================================

CREATE TABLE teams (
                       id BIGSERIAL PRIMARY KEY,

                       name VARCHAR(255) NOT NULL,

                       event_id BIGINT NOT NULL,
                       category_id BIGINT NOT NULL,
                       leader_id BIGINT NOT NULL,

                       FOREIGN KEY (event_id)
                           REFERENCES events(id),

                       FOREIGN KEY (category_id)
                           REFERENCES categories(id),

                       FOREIGN KEY (leader_id)
                           REFERENCES users(id)
);

-- =========================================================================
-- EVENT REGISTRATIONS
-- =========================================================================

CREATE TABLE event_registrations (
                                     id BIGSERIAL PRIMARY KEY,

                                     event_id BIGINT NOT NULL,
                                     user_id BIGINT NOT NULL,
                                     registered_by_id BIGINT NOT NULL,
                                     team_id BIGINT,

                                     registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     active BOOLEAN DEFAULT TRUE,

                                     FOREIGN KEY (event_id)
                                         REFERENCES events(id),

                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id),

                                     FOREIGN KEY (registered_by_id)
                                         REFERENCES users(id),

                                     FOREIGN KEY (team_id)
                                         REFERENCES teams(id)
);

-- =========================================================================
-- TEAM JOIN REQUESTS
-- =========================================================================

CREATE TABLE team_join_requests (
                                    id BIGSERIAL PRIMARY KEY,
                                    team_id BIGINT NOT NULL,
                                    registration_id BIGINT NOT NULL,
                                    status VARCHAR(50) NOT NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                    FOREIGN KEY (team_id)
                                        REFERENCES teams(id)
                                        ON DELETE CASCADE,

                                    FOREIGN KEY (registration_id)
                                        REFERENCES event_registrations(id)
                                        ON DELETE CASCADE
);

-- =========================================================================
-- ROUNDS
-- =========================================================================

CREATE TABLE rounds (
                        id BIGSERIAL PRIMARY KEY,

                        event_id BIGINT NOT NULL,

                        name VARCHAR(255) NOT NULL,
                        description TEXT,

                        order_index INTEGER,

                        submission_deadline TIMESTAMP,

                        is_active BOOLEAN DEFAULT FALSE,
                        advancement_count INTEGER,

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (event_id)
                            REFERENCES events(id)
                            ON DELETE CASCADE
);

-- =========================================================================
-- CRITERIA TEMPLATES
-- =========================================================================

CREATE TABLE criteria_templates (
                                    id BIGSERIAL PRIMARY KEY,

                                    criteria_name VARCHAR(255) NOT NULL,
                                    criteria_description TEXT,

                                    default_max_score NUMERIC(5,2),
                                    default_weight NUMERIC(5,2),

                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- EVENT CRITERIA
-- =========================================================================

CREATE TABLE event_criteria (
                                id BIGSERIAL PRIMARY KEY,

                                round_id BIGINT NOT NULL,
                                template_id BIGINT NOT NULL,

                                custom_name VARCHAR(255),

                                custom_weight NUMERIC(5,2),
                                max_score NUMERIC(5,2),

                                is_active BOOLEAN DEFAULT TRUE,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                FOREIGN KEY (round_id)
                                    REFERENCES rounds(id)
                                    ON DELETE CASCADE,

                                FOREIGN KEY (template_id)
                                    REFERENCES criteria_templates(id)
);

-- =========================================================================
-- SUBMISSIONS
-- =========================================================================

CREATE TABLE submissions (
                             id BIGSERIAL PRIMARY KEY,

                             team_id BIGINT NOT NULL,
                             round_id BIGINT NOT NULL,

                             project_name VARCHAR(255) NOT NULL,
                             github_url TEXT,

                             version_number INTEGER DEFAULT 1,

                             status VARCHAR(50),

                             submitted_at TIMESTAMP,
                             updated_at TIMESTAMP,

                             FOREIGN KEY (team_id)
                                 REFERENCES teams(id),

                             FOREIGN KEY (round_id)
                                 REFERENCES rounds(id)
);

-- =========================================================================
-- JUDGE ASSIGNMENTS
-- =========================================================================

CREATE TABLE judge_assignments (
                                   id BIGSERIAL PRIMARY KEY,

                                   judge_id BIGINT NOT NULL,

                                   round_id BIGINT NOT NULL,
                                   track_id BIGINT NOT NULL,

                                   judge_type VARCHAR(50),

                                   assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   FOREIGN KEY (judge_id)
                                       REFERENCES users(id),

                                   FOREIGN KEY (round_id)
                                       REFERENCES rounds(id),

                                   FOREIGN KEY (track_id)
                                       REFERENCES categories(id)
);

-- =========================================================================
-- EVALUATIONS
-- =========================================================================

CREATE TABLE evaluations (
                             id BIGSERIAL PRIMARY KEY,

                             submission_id BIGINT NOT NULL,
                             judge_id BIGINT NOT NULL,
                             criterion_id BIGINT NOT NULL,

                             score_value NUMERIC(5,2),

                             feedback TEXT,
                             internal_note TEXT,

                             is_calibration BOOLEAN DEFAULT FALSE,
                             is_finalized BOOLEAN DEFAULT FALSE,

                             evaluated_at TIMESTAMP,
                             updated_at TIMESTAMP,

                             FOREIGN KEY (submission_id)
                                 REFERENCES submissions(id)
                                 ON DELETE CASCADE,

                             FOREIGN KEY (judge_id)
                                 REFERENCES users(id),

                             FOREIGN KEY (criterion_id)
                                 REFERENCES event_criteria(id)
);

-- =========================================================================
-- EMAIL VERIFICATION TOKENS
-- =========================================================================

CREATE TABLE email_verification_tokens (
                                           id BIGSERIAL PRIMARY KEY,

                                           token VARCHAR(255) NOT NULL UNIQUE,

                                           user_id BIGINT NOT NULL,

                                           expires_at TIMESTAMP NOT NULL,

                                           used BOOLEAN NOT NULL DEFAULT FALSE,

                                           created_at TIMESTAMP NOT NULL,

                                           FOREIGN KEY (user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE
);

-- =========================================================================
-- PASSWORD RESET TOKENS
-- =========================================================================

CREATE TABLE password_reset_tokens (
                                       id BIGSERIAL PRIMARY KEY,

                                       token VARCHAR(255) NOT NULL UNIQUE,

                                       user_id BIGINT NOT NULL,

                                       expires_at TIMESTAMP NOT NULL,

                                       used BOOLEAN NOT NULL DEFAULT FALSE,

                                       created_at TIMESTAMP NOT NULL,

                                       FOREIGN KEY (user_id)
                                           REFERENCES users(id)
                                           ON DELETE CASCADE
);

-- =========================================================================
-- REFRESH TOKENS
-- =========================================================================

CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,

                                token VARCHAR(500) NOT NULL UNIQUE,

                                user_id BIGINT NOT NULL,

                                expiry_date TIMESTAMP NOT NULL,

                                revoked BOOLEAN DEFAULT FALSE,

                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
);