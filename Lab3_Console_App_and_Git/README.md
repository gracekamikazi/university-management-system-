# Lab 3: Putting It All Together (Mini Capstone)
**University Management Console Application**

## Where is each exercise?
| Exercise | Instruction | File(s) |
|---|---|---|
| 3.1 | Console CRUD app combining Labs 1 and 2: add/remove students and courses, enroll, view students with their courses (JOIN) | `src/ui/CampusApp.java` (uses `domain/`, `config/`, `persistence/`) |
| 3.2 | Exception handling for invalid input and DB errors | `CampusApp.java`: `readInt`, `readDouble`, `readText`, `handleDbError` |
| 3.3 | Git/GitHub: feature branches, pull requests, merge conflicts | the Git steps below, plus `.gitignore` |

Run: `ui/CampusApp.java`. Create the database first using the SQL files in `Lab2_JDBC_Database/sql/`.


Needs Lab 1 (`domain`), Lab 2 (`config`, `persistence`, database) to be working first.

---

## Exercise 3.1 – Console CRUD application

### What it does
```
===== UNIVERSITY MANAGEMENT =====
1. Add student          6. List courses
2. Remove student       7. Enroll student in course
3. List students        8. Record grade
4. Add course           9. View students with their courses (JOIN)
5. Remove course        0. Exit
```

### `ui/CampusApp.java`
```java
package ui;

import persistence.SubjectDAO;
import persistence.RegistrationDAO;
import persistence.RegistrationRecord;
import persistence.LearnerDAO;
import domain.Subject;
import domain.PostgradLearner;
import domain.Learner;
import domain.UndergradLearner;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CampusApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final LearnerDAO learnerDAO = new LearnerDAO();
    private static final SubjectDAO subjectDAO = new SubjectDAO();
    private static final RegistrationDAO registrationDAO = new RegistrationDAO();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> addLearner();
                    case 2 -> removeStudent();
                    case 3 -> listStudents();
                    case 4 -> addCourse();
                    case 5 -> removeCourse();
                    case 6 -> listCourses();
                    case 7 -> enrollStudent();
                    case 8 -> recordGrade();
                    case 9 -> viewStudentsWithCourses();
                    case 0 -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Please choose a number from the menu.");
                }
            } catch (IllegalArgumentException e) {          // Exercise 3.2: bad input
                System.out.println("Invalid input: " + e.getMessage());
            } catch (SQLException e) {                      // Exercise 3.2: database problems
                handleDbError(e);
            }
        }
    }

    // ---------- Menu actions ----------

    private static void addLearner() throws SQLException {
        String name = readText("Name: ");
        String email = readText("Email: ");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
        int type = readInt("Type (1 = Undergraduate, 2 = Graduate): ");

        Learner s;
        if (type == 1) s = new UndergradLearner(0, name, email);
        else if (type == 2) s = new PostgradLearner(0, name, email);
        else throw new IllegalArgumentException("Type must be 1 or 2");

        learnerDAO.insert(s);
        System.out.println("Student added with ID " + s.getId());
    }

    private static void removeStudent() throws SQLException {
        int id = readInt("Student ID to remove: ");
        if (learnerDAO.delete(id)) System.out.println("Student removed.");
        else System.out.println("No student with ID " + id);
    }

    private static void listStudents() throws SQLException {
        List<Learner> students = learnerDAO.findAll();
        if (students.isEmpty()) {
            System.out.println("No students yet.");
            return;
        }
        for (Learner s : students) System.out.println(s);
    }

    private static void addCourse() throws SQLException {
        String code = readText("Course code (e.g. CS101): ");
        String title = readText("Title: ");
        int credits = readInt("Credits: ");
        if (credits <= 0) throw new IllegalArgumentException("Credits must be greater than 0");

        Subject c = new Subject(0, code, title, credits);
        subjectDAO.insert(c);
        System.out.println("Course added with ID " + c.getId());
    }

    private static void removeCourse() throws SQLException {
        int id = readInt("Course ID to remove: ");
        if (subjectDAO.delete(id)) System.out.println("Course removed.");
        else System.out.println("No course with ID " + id);
    }

    private static void listCourses() throws SQLException {
        List<Subject> courses = subjectDAO.findAll();
        if (courses.isEmpty()) {
            System.out.println("No courses yet.");
            return;
        }
        for (Subject c : courses) System.out.println(c);
    }

    private static void enrollStudent() throws SQLException {
        int studentId = readInt("Student ID: ");
        int courseId = readInt("Course ID: ");
        registrationDAO.enroll(studentId, courseId);
        System.out.println("Student enrolled.");
    }

    private static void recordGrade() throws SQLException {
        int studentId = readInt("Student ID: ");
        int courseId = readInt("Course ID: ");
        double grade = readDouble("Grade (0-100): ");
        if (registrationDAO.updateGrade(studentId, courseId, grade)) System.out.println("Grade saved.");
        else System.out.println("That student is not enrolled in that course.");
    }

    // JOIN result, grouped by student
    private static void viewStudentsWithCourses() throws SQLException {
        List<RegistrationRecord> rows = registrationDAO.findAllWithDetails();
        if (rows.isEmpty()) {
            System.out.println("No students yet.");
            return;
        }
        int lastStudentId = -1;
        for (RegistrationRecord e : rows) {
            if (e.studentId() != lastStudentId) {
                System.out.println("\n" + e.studentName() + " (ID " + e.studentId() + ")");
                lastStudentId = e.studentId();
            }
            if (e.courseCode() == null) {
                System.out.println("   (no courses)");
            } else {
                System.out.println("   - " + e.courseCode() + " " + e.courseTitle()
                        + " | grade: " + (e.grade() == null ? "-" : e.grade()));
            }
        }
    }

    // ---------- Exercise 3.2: error handling ----------

    private static void handleDbError(SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        switch (state) {
            case "23505" -> System.out.println("Error: that value already exists "
                    + "(duplicate email, course code, or enrollment).");
            case "23503" -> System.out.println("Error: that student or course ID does not exist.");
            case "23514" -> System.out.println("Error: a value broke a database rule (check the input).");
            case "28P01" -> System.out.println("Error: wrong database password (check ConnectionFactory).");
            default -> {
                if (state.startsWith("08")) {
                    System.out.println("Error: cannot connect to the database. Is PostgreSQL running?");
                } else {
                    System.out.println("Database error: " + e.getMessage());
                }
            }
        }
    }

    // ---------- Safe input helpers: they keep asking until the input is valid ----------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = sc.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("This field cannot be empty.");
        }
    }

    private static void printMenu() {
        System.out.println("\n===== UNIVERSITY MANAGEMENT =====");
        System.out.println("1. Add student");
        System.out.println("2. Remove student");
        System.out.println("3. List students");
        System.out.println("4. Add course");
        System.out.println("5. Remove course");
        System.out.println("6. List courses");
        System.out.println("7. Enroll student in course");
        System.out.println("8. Record grade");
        System.out.println("9. View students with their courses");
        System.out.println("0. Exit");
    }
}
```
Run it: right-click `CampusApp` → **Run 'CampusApp.main()'**.

---

## Exercise 3.2 – Exception handling (already built in)

| Problem | How it is handled |
|---|---|
| Typing letters instead of a number | `readInt` / `readDouble` catch `NumberFormatException` and ask again |
| Empty name / bad email / credits ≤ 0 / grade outside 0–100 | `IllegalArgumentException` → "Invalid input: …" |
| Duplicate email, course code or enrollment | SQL state `23505` → friendly message |
| Enrolling a student or course that doesn't exist | SQL state `23503` → friendly message |
| PostgreSQL stopped / wrong password | SQL states `08…` / `28P01` → friendly message |
| Removing an ID that doesn't exist | DAO returns `false` → "No student with ID…" |

Test each one yourself: enter `abc` as an ID, add the same email twice, enroll the same student twice, enroll with ID 999.

---

## Exercise 3.3 – Git / GitHub collaboration

### Step 1: Put the project on GitHub
1. On github.com click **New repository** → name `university-system` → Create (no README).
2. In IntelliJ's **Terminal** tab (bottom):
```bash
git init
git add .
git commit -m "Initial commit: Lab 1 and Lab 2 code"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/university-system.git
git push -u origin main
```
Add a `.gitignore` first so you don't upload IDE files:
```
.idea/
out/
*.iml
```
**Never commit your real database password.** Use a placeholder in the repo.

### Step 2: Create an issue
On GitHub → **Issues → New issue**: "Add enrollment grade feature". Issues are how a team tracks tasks.

### Step 3: Feature branch
```bash
git checkout -b feature/record-grade
# ...make your changes in IntelliJ...
git add .
git commit -m "Add record grade option to menu (closes #1)"
git push -u origin feature/record-grade
```
Suggested branches (one per feature): `feature/student-management`, `feature/course-management`, `feature/enrollment`, `feature/join-report`.

### Step 4: Pull Request and review
1. On GitHub click **Compare & pull request** → write what changed → **Create pull request**.
2. Review simulation: open the **Files changed** tab, add a comment on a line (or ask a teammate), then click **Approve** / **Merge pull request**.
3. Update your local copy:
```bash
git checkout main
git pull
```

### Step 5: Simulate and resolve a merge conflict
A conflict happens when two branches change the same line.
```bash
git checkout -b feature/change-a
# edit the title in printMenu(): "===== UNIVERSITY MANAGEMENT ====="  ->  "=== UNI A ==="
git commit -am "Change menu title A"

git checkout main
git checkout -b feature/change-b
# edit the SAME line to "=== UNI B ==="
git commit -am "Change menu title B"

git checkout main
git merge feature/change-a        # works
git merge feature/change-b        # CONFLICT!
```
Git marks the file like this:
```
<<<<<<< HEAD
System.out.println("\n=== UNI A ===");
=======
System.out.println("\n=== UNI B ===");
>>>>>>> feature/change-b
```
To fix it:
1. Open the file (IntelliJ also offers **Resolve** / merge tool).
2. Keep the line you want and delete the `<<<<<<<`, `=======`, `>>>>>>>` lines.
3. Finish the merge:
```bash
git add .
git commit -m "Resolve merge conflict in menu title"
git push
```

### Useful commands
| Command | What it does |
|---|---|
| `git status` | shows changed files |
| `git branch` | lists branches |
| `git log --oneline` | short history |
| `git checkout -b name` | create and switch to a branch |
| `git pull` | download the latest changes |

---

## Final checklist
- [ ] Lab 1 demo prints GPAs, rosters and lecturer assignments
- [ ] Tables exist in pgAdmin and `LabTwoRunner` runs
- [ ] `CampusApp` can add/remove students and courses, enroll, grade, and show the JOIN view
- [ ] Wrong input and DB errors give friendly messages (no crashes)
- [ ] Repo on GitHub with at least 2 feature branches, 1 merged PR, and 1 resolved conflict
- [ ] Password not committed

## Key takeaways
- Full cycle: **design (OOP) → persistence (JDBC + DAO) → application (console) → version control (Git)**.
- Keep layers separate: `domain` = data, `persistence` = SQL, `ui` = user interface.
