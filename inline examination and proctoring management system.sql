CREATE DATABASE online_examination;
USE online_examination;
SELECT DATABASE();
USE online_examination;
SHOW TABLES;

USE online_examination;

CREATE TABLE Exams (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_title VARCHAR(150) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES Users(user_id)
);

CREATE TABLE Questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    marks INT NOT NULL DEFAULT 1,
    FOREIGN KEY (exam_id) REFERENCES Exams(exam_id)
);

CREATE TABLE Exam_Attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME,
    status ENUM('IN_PROGRESS', 'SUBMITTED') DEFAULT 'IN_PROGRESS',
    FOREIGN KEY (student_id) REFERENCES Users(user_id),
    FOREIGN KEY (exam_id) REFERENCES Exams(exam_id)
);

CREATE TABLE Student_Answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option CHAR(1),
    FOREIGN KEY (attempt_id) REFERENCES Exam_Attempts(attempt_id),
    FOREIGN KEY (question_id) REFERENCES Questions(question_id),
    UNIQUE (attempt_id, question_id)
);

CREATE TABLE Results (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL UNIQUE,
    marks_obtained DECIMAL(7,2) NOT NULL DEFAULT 0,
    total_marks DECIMAL(7,2) NOT NULL,
    evaluated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES Exam_Attempts(attempt_id)
);

CREATE TABLE Proctoring_Activities (
    activity_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    activity_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    review_status ENUM('PENDING', 'REVIEWED') DEFAULT 'PENDING',
    FOREIGN KEY (attempt_id) REFERENCES Exam_Attempts(attempt_id)
);

CREATE TABLE Audit_Logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(150) NOT NULL,
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
SHOW TABLES;
USE online_examination;
INSERT INTO Users
(full_name, email, password_hash, role, status)
VALUES
('System Admin', 'admin@example.com', 'demo_admin_hash', 'ADMIN', 'ACTIVE'),
('Janhavi Student', 'student@example.com', 'demo_student_hash', 'STUDENT', 'ACTIVE');

SELECT user_id, full_name, email, role
FROM Users;

INSERT INTO Exams
(exam_title, description, duration_minutes, total_marks, created_by)
VALUES
('Java Basics Test', 'A sample multiple-choice Java examination', 30, 10, 1);

SELECT * FROM Exams;

INSERT INTO Questions
(exam_id, question_text, option_a, option_b, option_c, option_d, correct_option, marks)
VALUES
(1, 'Which keyword is used to inherit a class in Java?',
 'implements', 'extends', 'inherits', 'instanceof', 'B', 5),
(1, 'Which method is the entry point of a basic Java program?',
 'start()', 'run()', 'main()', 'init()', 'C', 5);

SELECT question_id, exam_id, question_text, marks
FROM Questions;
SELECT * FROM Users;
SELECT * FROM Exams;
SELECT * FROM Questions;
USE online_examination;
SELECT user_id, full_name, email, role, status
FROM Users;
USE online_examination;
SELECT user_id, full_name, email, role, status
FROM Users
WHERE email = 'janhaviwaychal@gmail.com';
USE online_examination;

SELECT *
FROM Questions;
USE online_examination;

DESCRIBE Results;
SHOW COLUMNS FROM Results;
DESCRIBE Exam_Attempts;
SHOW COLUMNS FROM Exam_Attempts;
SELECT user_id, full_name, email
FROM Users
WHERE email = 'janhaviwaychal@gmail.com';
SELECT * FROM Exam_Attempts
ORDER BY attempt_id DESC;
SELECT * FROM Results
ORDER BY result_id DESC;
SELECT
    u.full_name,
    e.exam_title,
    r.marks_obtained,
    r.total_marks,
    r.evaluated_at
FROM Results r
JOIN Exam_Attempts a ON r.attempt_id = a.attempt_id
JOIN Users u ON a.student_id = u.user_id
JOIN Exams e ON a.exam_id = e.exam_id
ORDER BY r.result_id DESC;
USE online_examination;
DESCRIBE Student_Answers;
SELECT * FROM Student_Answers
ORDER BY answer_id DESC;
SELECT * FROM Results
ORDER BY result_id DESC;
SELECT * FROM Exam_Attempts
ORDER BY attempt_id DESC;
SHOW DATABASES;
USE online_examination;
SHOW TABLES;
USE online_examination;
DESCRIBE Questions;
USE online_examination;

DESCRIBE Results;
SHOW CREATE TABLE Results;
SHOW TABLES;
DESCRIBE exam_attempts;
SELECT * FROM Results;
DESCRIBE student_answers;
DESCRIBE questions;
DESCRIBE users;
SELECT user_id, full_name, role
FROM users
WHERE role = 'STUDENT';
USE online_examination;

SELECT user_id, full_name, email, role, status
FROM users;
SELECT user_id, email, password_hash
FROM users
WHERE user_id = 4;
USE online_examination;

UPDATE users
SET password_hash = 'PASTE_YOUR_GENERATED_HASH_HERE'
WHERE user_id = 4;
SELECT user_id, email
FROM users
WHERE user_id = 4;
USE online_examination;

UPDATE users
SET password_hash = 'PASTE_GENERATED_HASH_HERE'
WHERE user_id = 4;
SELECT
    LENGTH(password_hash) AS hash_length,
    LOCATE(':', password_hash) AS colon_position
FROM users
WHERE user_id = 4;
USE online_examination;

UPDATE users
SET password_hash = 'PASTE_YOUR_NEW_COMPLETE_HASH_HERE'
WHERE user_id = 4;
SELECT
    LENGTH(password_hash) AS hash_length,
    LOCATE(':', password_hash) AS colon_position
FROM users
WHERE user_id = 4;
SELECT password_hash
FROM users
WHERE user_id = 4;
UPDATE users
SET password_hash = "pF7JS+uD98L/ZECcg/HBog==:xVlKtTHjyti2OG+2H2m/k3nTcwHMGN27Ewbs9+wAXAU="
WHERE user_id = 4;
SELECT LENGTH(password_hash), LOCATE(':', password_hash)
FROM users
WHERE user_id = 4;

USE online_examination;

DESCRIBE exam_attempts;

DESCRIBE student_answers;

DESCRIBE results;

DESCRIBE questions;
DESCRIBE exam_attempts;
