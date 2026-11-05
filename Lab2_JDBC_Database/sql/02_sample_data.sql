-- Optional sample data for testing
INSERT INTO students (name, email, student_type) VALUES
  ('Alice', 'alice@uni.edu', 'UNDERGRADUATE'),
  ('Bob',   'bob@uni.edu',   'GRADUATE');

INSERT INTO courses (code, title, credits) VALUES
  ('CS101', 'Intro to Java', 3),
  ('CS102', 'Databases', 4);

INSERT INTO enrollments (student_id, course_id) VALUES (1, 1), (1, 2), (2, 1);
