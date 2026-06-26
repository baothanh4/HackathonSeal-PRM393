-- =========================================================================
-- HackathonSeal Database Seed Script (data.sql)
-- Target Database: PostgreSQL
-- =========================================================================

-- Clear existing data (in correct dependency order to prevent FK conflicts)
TRUNCATE TABLE evaluations CASCADE;
TRUNCATE TABLE judge_assignments CASCADE;
TRUNCATE TABLE submissions CASCADE;
TRUNCATE TABLE event_registrations CASCADE;
TRUNCATE TABLE teams CASCADE;
TRUNCATE TABLE event_criteria CASCADE;
TRUNCATE TABLE criteria_templates CASCADE;
TRUNCATE TABLE category_mentors CASCADE;
TRUNCATE TABLE category_judges CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE rules CASCADE;
TRUNCATE TABLE rounds CASCADE;
TRUNCATE TABLE events CASCADE;
TRUNCATE TABLE user_profiles CASCADE;
TRUNCATE TABLE email_verification_tokens CASCADE;
TRUNCATE TABLE password_reset_tokens CASCADE;
TRUNCATE TABLE refresh_tokens CASCADE;
TRUNCATE TABLE users CASCADE;

-- 1. USERS
-- Password for all mock accounts: Password123@
-- BCrypt hash: $2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou
INSERT INTO users (id, email, password, full_name, role, status, is_email_verified, created_at) VALUES
(1, 'admin@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'System Administrator', 'ADMIN', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, 'coordinator@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Event Coordinator', 'COORDINATOR', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '9 days'),
(3, 'judge1@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Dr. Alan Turing', 'JUDGE', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '8 days'),
(4, 'judge2@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Prof. Grace Hopper', 'JUDGE', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '8 days'),
(5, 'mentor1@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Steve Jobs', 'MENTOR', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(6, 'leader_a@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Alice Vance', 'STUDENT', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '6 days'),
(7, 'member_a@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Bob Smith', 'STUDENT', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '6 days'),
(8, 'leader_b@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Charlie Brown', 'STUDENT', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(9, 'member_b@gmail.com', '$2b$10$gAFtJbfOPPFi7noPuxGm7.V8Q22aO9HjDCnpqHRtIBqjmCwtPE9ou', 'Diana Prince', 'STUDENT', 'APPROVED', true, CURRENT_TIMESTAMP - INTERVAL '5 days');

-- 2. USER PROFILES
INSERT INTO user_profiles (id, user_id, participant_type, student_code, university_name) VALUES
(1, 6, 'FPT_STUDENT', 'SE160001', 'FPT University'),
(2, 7, 'FPT_STUDENT', 'SE160002', 'FPT University'),
(3, 8, 'EXTERNAL_STUDENT', 'ET160003', 'VNU University of Engineering and Technology'),
(4, 9, 'EXTERNAL_STUDENT', 'ET160004', 'Hanoi University of Science and Technology');

-- 3. EVENTS
INSERT INTO events (id, title, description, location, start_time, end_time, image_url, max_participants, current_participants, status, created_at, updated_at) VALUES
(1, 'FPT Tech Hackathon 2026', 'The ultimate challenge for students to showcase innovative tech solutions and build high-impact applications.', 'FPT Innovation Center, Hoa Lac Campus', '2026-07-01 08:00:00', '2026-07-03 18:00:00', 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d', 100, 4, 'UPCOMING', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(2, 'AI for Green Future 2026', 'Harnessing Artificial Intelligence to address critical environmental challenges like climate change, waste management, and renewable energy.', 'Online / Hybrid', '2026-08-15 09:00:00', '2026-08-17 17:00:00', 'https://images.unsplash.com/photo-1451187580459-43490279c0fa', 200, 0, 'UPCOMING', CURRENT_TIMESTAMP - INTERVAL '9 days', CURRENT_TIMESTAMP - INTERVAL '9 days');

-- 4. RULES
INSERT INTO rules (id, name, description, event_id, created_at, updated_at) VALUES
(1, 'Team Size Limit', 'Each team must contain between 2 and 5 members to participate in the hackathon.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Original Coding', 'All projects must be started and developed entirely during the hackathon hours. Pre-existing projects are disqualified.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Submission Package', 'Teams must submit their final project description, GitHub repository link, and a short video demo link before the round deadline.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. CATEGORIES / TRACKS
INSERT INTO categories (id, name, description, event_id, created_at, updated_at) VALUES
(1, 'Web Development', 'Building modern, interactive, and responsive web applications using advanced tech stacks.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Mobile Applications', 'Designing and creating user-friendly native or cross-platform mobile apps.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'AI & Machine Learning', 'Developing solutions utilizing smart algorithms, ML models, or large language model integrations.', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. CATEGORY MENTORS
INSERT INTO category_mentors (category_id, user_id) VALUES
(1, 5); -- Steve Jobs mentors Web Development

-- 7. CATEGORY JUDGES
INSERT INTO category_judges (category_id, user_id) VALUES
(1, 3), -- Alan Turing judges Web Development
(1, 4), -- Grace Hopper judges Web Development
(2, 3), -- Alan Turing judges Mobile Apps
(3, 4); -- Grace Hopper judges AI & ML

-- 8. TEAMS
INSERT INTO teams (id, name, event_id, category_id, leader_id) VALUES
(1, 'Team Alpha', 1, 1, 6), -- Category: Web Dev, Leader: Alice
(2, 'Team Beta', 1, 1, 8);  -- Category: Web Dev, Leader: Charlie

-- 9. EVENT REGISTRATIONS
INSERT INTO event_registrations (id, event_id, user_id, registered_by_id, team_id, registered_at, active) VALUES
(1, 1, 6, 6, 1, CURRENT_TIMESTAMP - INTERVAL '4 days', true),
(2, 1, 7, 6, 1, CURRENT_TIMESTAMP - INTERVAL '4 days', true),
(3, 1, 8, 8, 2, CURRENT_TIMESTAMP - INTERVAL '3 days', true),
(4, 1, 9, 8, 2, CURRENT_TIMESTAMP - INTERVAL '3 days', true);

-- 10. ROUNDS
INSERT INTO rounds (id, event_id, name, description, order_index, submission_deadline, is_active, created_at, updated_at) VALUES
(1, 1, 'Elimination Round', 'Initial screening phase where projects are reviewed based on their core prototype and repository implementation.', 1, '2026-07-02 12:00:00', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Final Pitching', 'Final presentation and demonstration to the main judge panel to select the winners.', 2, '2026-07-03 15:00:00', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 11. CRITERIA TEMPLATES
INSERT INTO criteria_templates (id, criteria_name, criteria_description, default_max_score, default_weight, created_at) VALUES
(1, 'Innovation & Creativity', 'The uniqueness, novelty, and original approach to resolving the target problem.', 10.0, 0.3, CURRENT_TIMESTAMP),
(2, 'Technical Complexity', 'Quality of system architecture, code cleaniness, appropriate choice of tech stack, and feature completeness.', 10.0, 0.4, CURRENT_TIMESTAMP),
(3, 'Feasibility & Impact', 'The practical viability of the solution and its potential positive impact on real users.', 10.0, 0.3, CURRENT_TIMESTAMP);

-- 12. EVENT CRITERIA
INSERT INTO event_criteria (id, round_id, template_id, custom_name, custom_weight, max_score, is_active, created_at) VALUES
(1, 1, 1, 'Innovation & Creativity (Tech Hackathon)', 0.3, 10.0, true, CURRENT_TIMESTAMP),
(2, 1, 2, 'Technical Execution', 0.4, 10.0, true, CURRENT_TIMESTAMP),
(3, 1, 3, 'Presentation & Live Demo', 0.3, 10.0, true, CURRENT_TIMESTAMP);

-- 13. SUBMISSIONS
INSERT INTO submissions (id, team_id, round_id, project_name, github_url, version_number, status, submitted_at, updated_at) VALUES
(1, 1, 1, 'EcoSphere Web Application', 'https://github.com/team-alpha/ecosphere', 1, 'SUBMITTED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 2, 1, 'MedConnect Emergency Portal', 'https://github.com/team-beta/medconnect', 1, 'SUBMITTED', CURRENT_TIMESTAMP - INTERVAL '18 hours', CURRENT_TIMESTAMP - INTERVAL '18 hours');

-- 14. JUDGE ASSIGNMENTS
INSERT INTO judge_assignments (id, judge_id, round_id, track_id, judge_type, assigned_at) VALUES
(1, 3, 1, 1, 'INTERNAL', CURRENT_TIMESTAMP), -- Alan Turing assigned to Round 1, Category 1 (Web Dev)
(2, 4, 1, 1, 'INTERNAL', CURRENT_TIMESTAMP); -- Grace Hopper assigned to Round 1, Category 1 (Web Dev)

-- 15. EVALUATIONS (Mock Evaluations for Team Alpha / Submission 1)
INSERT INTO evaluations (id, submission_id, judge_id, criterion_id, score_value, feedback, internal_note, is_calibration, is_finalized, evaluated_at, updated_at) VALUES
-- Alan Turing evaluations for Team Alpha
(1, 1, 3, 1, 8.5, 'Highly creative concept targeting local community carbon footprints. User flow is well-defined.', 'Strong product concept.', false, true, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(2, 1, 3, 2, 7.5, 'Excellent stack choices, but some backend routes suffer from validation issues. Code comments could be improved.', 'Need to check connection pooling.', false, true, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(3, 1, 3, 3, 9.0, 'Live presentation was outstanding. Slide design and speaker tone were very professional.', 'Strong candidates for the final round.', false, true, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),

-- Grace Hopper evaluations for Team Alpha
(4, 1, 4, 1, 9.0, 'Impressive interactive graphs. The approach is fresh and the styling is clean.', '', false, true, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(5, 1, 4, 2, 8.0, 'Very clean component architecture on the React frontend. Nicely structured SQL relational schema.', '', false, true, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(6, 1, 4, 3, 8.5, 'Well-organized slides. Prototype worked successfully without crashing during the demo.', '', false, true, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours');


-- =========================================================================
-- Reset PostgreSQL Auto-Increment Sequences to prevent primary key conflicts
-- =========================================================================
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('user_profiles', 'id'), COALESCE(MAX(id), 1)) FROM user_profiles;
SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE(MAX(id), 1)) FROM events;
SELECT setval(pg_get_serial_sequence('rules', 'id'), COALESCE(MAX(id), 1)) FROM rules;
SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE(MAX(id), 1)) FROM categories;
SELECT setval(pg_get_serial_sequence('teams', 'id'), COALESCE(MAX(id), 1)) FROM teams;
SELECT setval(pg_get_serial_sequence('event_registrations', 'id'), COALESCE(MAX(id), 1)) FROM event_registrations;
SELECT setval(pg_get_serial_sequence('rounds', 'id'), COALESCE(MAX(id), 1)) FROM rounds;
SELECT setval(pg_get_serial_sequence('criteria_templates', 'id'), COALESCE(MAX(id), 1)) FROM criteria_templates;
SELECT setval(pg_get_serial_sequence('event_criteria', 'id'), COALESCE(MAX(id), 1)) FROM event_criteria;
SELECT setval(pg_get_serial_sequence('submissions', 'id'), COALESCE(MAX(id), 1)) FROM submissions;
SELECT setval(pg_get_serial_sequence('judge_assignments', 'id'), COALESCE(MAX(id), 1)) FROM judge_assignments;
SELECT setval(pg_get_serial_sequence('evaluations', 'id'), COALESCE(MAX(id), 1)) FROM evaluations;
SELECT setval(pg_get_serial_sequence('email_verification_tokens', 'id'), COALESCE(MAX(id), 1)) FROM email_verification_tokens;
SELECT setval(pg_get_serial_sequence('password_reset_tokens', 'id'), COALESCE(MAX(id), 1)) FROM password_reset_tokens;
SELECT setval(pg_get_serial_sequence('refresh_tokens', 'id'), COALESCE(MAX(id), 1)) FROM refresh_tokens;
