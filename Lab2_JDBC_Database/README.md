# Lab 2: Database Integration with JDBC
**Extend the University System with PostgreSQL** (IntelliJ + pgAdmin)

## Where is each exercise?
| Exercise | Instruction | File(s) |
|---|---|---|
| 2.1 | PostgreSQL schema: `students`, `courses`, `enrollments` | `sql/00_create_database.sql`, `sql/01_schema.sql`, `sql/02_sample_data.sql` |
| 2.2 | CRUD: insert student, list courses, update enrollment, delete student | `persistence/*DAO.java`, tested in `ui/LabTwoRunner.java` |
| 2.3 | PreparedStatements against SQL injection | used in every DAO method in `persistence/` |
| 2.4 | DAO classes (StudentDAO, CourseDAO...) | `persistence/LearnerDAO.java`, `SubjectDAO.java`, `RegistrationDAO.java`, `RegistrationRecord.java`; connection in `config/ConnectionFactory.java` |

Run order: SQL files in pgAdmin -> set your password in `ConnectionFactory.java` -> run `ui/LabTwoRunner.java`.
The `domain/` folder is the Lab 1 code, copied so this lab stands on its own.


Uses the `domain` classes from Lab 1.

---

## Exercise 2.1 – Create the PostgreSQL schema (pgAdmin)

### Step 1: Create the database
In pgAdmin: right-click **Databases → Create → Database…** → name it `university_db` → Save.

### Step 2: Create the tables
Right-click `university_db` → **Query Tool**, paste this and press **F5** (Execute):

```sql
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
```

### Step 3: Add sample data (optional but useful for testing)
```sql
INSERT INTO students (name, email, student_type) VALUES
  ('Alice', 'alice@uni.edu', 'UNDERGRADUATE'),
  ('Bob',   'bob@uni.edu',   'GRADUATE');

INSERT INTO courses (code, title, credits) VALUES
  ('CS101', 'Intro to Java', 3),
  ('CS102', 'Databases', 4);

INSERT INTO enrollments (student_id, course_id) VALUES (1, 1), (1, 2), (2, 1);
```
`ON DELETE CASCADE` means deleting a student automatically deletes their enrollments.

---

## Add the PostgreSQL JDBC driver (IntelliJ)
1. **File → Project Structure → Libraries → + → From Maven…**
2. Type `org.postgresql:postgresql:42.7.3` → Search → OK → Apply.

(If you use a Maven project instead, add this to `pom.xml`:)
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>
```

---

## Connection class

### `config/ConnectionFactory.java`
```java
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final String URL = "jdbc:postgresql://localhost:5432/university_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "your_password_here";   // <-- change this

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

---

## Exercise 2.3 – PreparedStatements (SQL injection protection)
Never build SQL by joining strings with user input:

```java
// BAD: if name is  x'; DROP TABLE students; --  the database runs it!
String sql = "SELECT * FROM students WHERE name = '" + name + "'";

// GOOD: the ? is a placeholder. The value is sent separately and is never run as SQL.
String sql = "SELECT * FROM students WHERE name = ?";
PreparedStatement ps = con.prepareStatement(sql);
ps.setString(1, name);
```
Every DAO below uses `PreparedStatement`, which covers this exercise too.

---

## Exercise 2.4 – DAO classes
A **DAO** (Data Access Object) is the only place that contains SQL. The rest of the app just calls methods like `learnerDAO.findAll()`.

### `persistence/LearnerDAO.java`
```java
package persistence;

import config.ConnectionFactory;
import domain.PostgradLearner;
import domain.Learner;
import domain.UndergradLearner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LearnerDAO {

    // CREATE
    public void insert(Learner s) throws SQLException {
        String sql = "INSERT INTO students (name, email, student_type) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getLearnerType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt("id"));   // put the new id back on the object
            }
        }
    }

    // READ (all)
    public List<Learner> findAll() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY id";
        List<Learner> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // READ (one)
    public Optional<Learner> findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // UPDATE
    public boolean update(Learner s) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, student_type = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getLearnerType());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;   // true if a row was really deleted
        }
    }

    // Turns a database row into the right Java object (polymorphism again)
    private Learner mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String type = rs.getString("student_type");
        if ("GRADUATE".equals(type)) {
            return new PostgradLearner(id, name, email);
        }
        return new UndergradLearner(id, name, email);
    }
}
```

### `persistence/SubjectDAO.java`
```java
package persistence;

import config.ConnectionFactory;
import domain.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO {

    public void insert(Subject c) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt("id"));
            }
        }
    }

    public List<Subject> findAll() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY id";
        List<Subject> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Subject> findById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean update(Subject c) throws SQLException {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        return new Subject(rs.getInt("id"), rs.getString("code"),
                rs.getString("title"), rs.getInt("credits"));
    }
}
```

### `persistence/RegistrationRecord.java` (a small holder for JOIN results)
```java
package persistence;

// A record is a short way to write a class that just holds data (needs JDK 16+)
public record RegistrationRecord(int studentId, String studentName,
                             String courseCode, String courseTitle, Double grade) {
}
```

### `persistence/RegistrationDAO.java`
```java
package persistence;

import config.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    // CREATE: enroll a student in a course
    public void enroll(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    // UPDATE: record / change a grade
    public boolean updateGrade(int studentId, int courseId, double grade) throws SQLException {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
        String sql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, grade);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: remove one enrollment
    public boolean unenroll(int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    // READ with JOINs: every student with their courses.
    // LEFT JOIN keeps students who have no courses yet (course columns come back NULL).
    public List<RegistrationRecord> findAllWithDetails() throws SQLException {
        String sql = """
                SELECT s.id AS student_id, s.name, c.code, c.title, e.grade
                FROM students s
                LEFT JOIN enrollments e ON e.student_id = s.id
                LEFT JOIN courses c     ON c.id = e.course_id
                ORDER BY s.name, s.id, c.code
                """;
        List<RegistrationRecord> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double g = rs.getDouble("grade");
                Double grade = rs.wasNull() ? null : g;   // wasNull() checks if the column was NULL
                list.add(new RegistrationRecord(rs.getInt("student_id"), rs.getString("name"),
                        rs.getString("code"), rs.getString("title"), grade));
            }
        }
        return list;
    }
}
```

---

## Exercise 2.2 – Test all CRUD operations: `ui/LabTwoRunner.java`
```java
package ui;

import persistence.*;
import domain.*;

import java.sql.SQLException;

public class LabTwoRunner {
    public static void main(String[] args) {
        LearnerDAO learnerDAO = new LearnerDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        RegistrationDAO registrationDAO = new RegistrationDAO();

        try {
            // 1) INSERT a student
            Learner carol = new UndergradLearner(0, "Carol", "carol@uni.edu");
            learnerDAO.insert(carol);
            System.out.println("Inserted: " + carol);

            // 2) LIST courses
            System.out.println("\nCourses:");
            for (Subject c : subjectDAO.findAll()) System.out.println("  " + c);

            // 3) UPDATE enrollment (enroll Carol in course 1, then set her grade)
            registrationDAO.enroll(carol.getId(), 1);
            registrationDAO.updateGrade(carol.getId(), 1, 88.5);
            System.out.println("\nCarol enrolled in course 1 with grade 88.5");

            // JOIN query
            System.out.println("\nStudents and their courses:");
            for (RegistrationRecord e : registrationDAO.findAllWithDetails()) {
                System.out.println("  " + e.studentName() + " | " + e.courseCode()
                        + " | " + e.courseTitle() + " | grade: " + e.grade());
            }

            // 4) DELETE the student (their enrollments are deleted automatically)
            boolean deleted = learnerDAO.delete(carol.getId());
            System.out.println("\nCarol deleted? " + deleted);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
```
Check in pgAdmin: right-click a table → **View/Edit Data → All Rows** to see the changes.

### Common problems
| Error | Fix |
|---|---|
| `No suitable driver found` | the PostgreSQL library was not added (see above) |
| `password authentication failed` | wrong `PASSWORD` in `ConnectionFactory` |
| `Connection refused` | PostgreSQL isn't running, or the port isn't 5432 |
| `relation "students" does not exist` | you ran the SQL in the wrong database; run it in `university_db` |

## Key takeaways
- JDBC steps: **connect → PreparedStatement → execute → read ResultSet → close** (try-with-resources closes things for you).
- `PreparedStatement` with `?` prevents SQL injection.
- DAO pattern: SQL lives only in `persistence`, which keeps the rest of the code clean.
- JOINs let one query pull data from several tables.
