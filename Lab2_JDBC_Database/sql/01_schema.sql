-- Exercise 2.1: schema (run inside university_db)
CREATE TABLE students (
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    student_type VARCHAR(20)  NOT NULL
                 CHECK (student_type IN ('UNDERGRADUATE', 'GRADUATE'))
);

CREATE TABLE courses (
    id      SERIAL PRIMARY KEY,
    code    VARCHAR(20)  NOT NULL UNIQUE,
    title   VARCHAR(150) NOT NULL,
    credits INT          NOT NULL CHECK (credits > 0)
);

CREATE TABLE enrollments (
    id         SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    course_id  INT NOT NULL REFERENCES courses(id)  ON DELETE CASCADE,
    grade      NUMERIC(5,2),                 -- NULL until a grade is recorded
    UNIQUE (student_id, course_id)           -- a student can't enroll twice in the same course
);
