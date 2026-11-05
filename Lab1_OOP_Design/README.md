# Lab 1: Object-Oriented Design in Action
**University Student Management System** (IntelliJ, pure Java, no database yet)

## Where is each exercise? (files are in `src/`)
| Exercise | Instruction | File(s) |
|---|---|---|
| 1.1 | Design Student, Course, Instructor classes with inheritance and encapsulation (fields, constructors, getters, setters, toString) | `domain/Member.java`, `domain/Learner.java`, `domain/Lecturer.java`, `domain/Subject.java` |
| 1.2 | Polymorphism: override `calculateGPA()` for undergraduates vs graduates | `domain/UndergradLearner.java`, `domain/PostgradLearner.java` |
| 1.3 | Use List, Set, Map for enrolled students, rosters, instructor assignments | `domain/CampusHub.java`, test in `ui/LabOneRunner.java` |

Run: `ui/LabOneRunner.java`.


## Setup (one time)
1. IntelliJ → **New Project** → Name: `university-system`, Language: **Java**, Build system: **IntelliJ**, JDK: **17 or newer**.
2. Right-click `src` → **New → Package**. Create these packages:
   - `domain`  (Lab 1 classes)
   - `ui`    (classes with `main`)
   - `persistence` and `config` (used later in Lab 2)

## Design overview

```
Member (id, name, email)
 ├── Learner  (abstract: grades, calculateGPA())
 │     ├── UndergradLearner
 │     └── PostgradLearner
 └── Lecturer (department)

Subject (id, code, title, credits, lecturer)
```

---

## Exercise 1.1 – Classes with inheritance and encapsulation
Every class has private fields, constructors, getters, setters and `toString()`.

### `domain/Member.java`
```java
package domain;

import java.util.Objects;

public class Member {
    private int id;
    private String name;
    private String email;

    public Member(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Member{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }

    // Needed so Sets / Maps can tell two people apart by id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member other = (Member) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### `domain/Learner.java` (abstract: it only makes sense as Undergraduate or Graduate)
```java
package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Learner extends Member {
    private final List<Double> grades = new ArrayList<>();   // percentages 0-100

    public Learner(int id, String name, String email) {
        super(id, name, email);
    }

    public void addGrade(double grade) {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
        grades.add(grade);
    }

    public List<Double> getGrades() {
        return Collections.unmodifiableList(grades);   // encapsulation: callers can't change our list
    }

    protected double averageGrade() {
        if (grades.isEmpty()) return 0;
        double sum = 0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    // Exercise 1.2: each subclass decides how GPA is calculated
    public abstract double calculateGPA();

    public abstract String getLearnerType();

    @Override
    public String toString() {
        return getLearnerType() + "{id=" + getId() + ", name='" + getName()
                + "', email='" + getEmail() + "'}";
    }
}
```

### `domain/Lecturer.java`
```java
package domain;

public class Lecturer extends Member {
    private String department;

    public Lecturer(int id, String name, String email, String department) {
        super(id, name, email);
        this.department = department;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "Lecturer{id=" + getId() + ", name='" + getName()
                + "', department='" + department + "'}";
    }
}
```

### `domain/Subject.java`
```java
package domain;

import java.util.Objects;

public class Subject {
    private int id;
    private String code;
    private String title;
    private int credits;
    private Lecturer lecturer;   // may be null

    public Subject(int id, String code, String title, int credits) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }

    @Override
    public String toString() {
        return "Subject{id=" + id + ", code='" + code + "', title='" + title
                + "', credits=" + credits
                + ", lecturer=" + (lecturer == null ? "none" : lecturer.getName()) + "}";
    }

    // Two courses are the same if they have the same code
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return code.equals(((Subject) o).code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
```

---

## Exercise 1.2 – Polymorphism (method overriding)
`calculateGPA()` is declared once in `Learner`, and each subclass overrides it.

### `domain/UndergradLearner.java`
```java
package domain;

public class UndergradLearner extends Learner {

    public UndergradLearner(int id, String name, String email) {
        super(id, name, email);
    }

    // Standard scale: 100% = 4.0, 0% = 0.0
    @Override
    public double calculateGPA() {
        double gpa = averageGrade() / 100.0 * 4.0;
        return Math.round(gpa * 100.0) / 100.0;
    }

    @Override
    public String getLearnerType() {
        return "UNDERGRADUATE";
    }
}
```

### `domain/PostgradLearner.java`
```java
package domain;

public class PostgradLearner extends Learner {

    public PostgradLearner(int id, String name, String email) {
        super(id, name, email);
    }

    // Stricter scale: 50% or below = 0.0, 100% = 4.0
    @Override
    public double calculateGPA() {
        double gpa = (averageGrade() - 50) / 50.0 * 4.0;
        if (gpa < 0) gpa = 0;
        return Math.round(gpa * 100.0) / 100.0;
    }

    @Override
    public String getLearnerType() {
        return "GRADUATE";
    }
}
```

---

## Exercise 1.3 – Collections (List, Set, Map)

| Need | Collection used | Why |
|---|---|---|
| All students by id | `Map<Integer, Learner>` | fast lookup by id |
| All courses (no duplicates) | `Set<Subject>` | a Set rejects duplicates |
| Subject roster | `Map<Subject, List<Learner>>` | each course has a list of students |
| Lecturer assignments | `Map<Lecturer, Set<Subject>>` | each lecturer has a set of courses |

### `domain/CampusHub.java`
```java
package domain;

import java.util.*;

public class CampusHub {
    private final Map<Integer, Learner> students = new HashMap<>();
    private final Set<Subject> courses = new HashSet<>();
    private final Map<Subject, List<Learner>> rosters = new HashMap<>();
    private final Map<Lecturer, Set<Subject>> assignments = new HashMap<>();

    public void addLearner(Learner s) {
        students.put(s.getId(), s);
    }

    public void addCourse(Subject c) {
        if (courses.add(c)) {              // add() returns false if it already exists
            rosters.put(c, new ArrayList<>());
        }
    }

    public boolean enroll(Learner s, Subject c) {
        if (!courses.contains(c)) {
            throw new IllegalArgumentException("Course does not exist: " + c.getCode());
        }
        List<Learner> roster = rosters.get(c);
        if (roster.contains(s)) return false;   // already enrolled
        roster.add(s);
        return true;
    }

    public void assignLecturer(Lecturer i, Subject c) {
        assignments.computeIfAbsent(i, k -> new HashSet<>()).add(c);
        c.setLecturer(i);
    }

    public List<Learner> getRoster(Subject c) {
        return rosters.getOrDefault(c, new ArrayList<>());
    }

    public List<Subject> getCoursesOf(Learner s) {
        List<Subject> result = new ArrayList<>();
        for (Map.Entry<Subject, List<Learner>> entry : rosters.entrySet()) {
            if (entry.getValue().contains(s)) result.add(entry.getKey());
        }
        return result;
    }

    public Set<Subject> getCoursesTaughtBy(Lecturer i) {
        return assignments.getOrDefault(i, new HashSet<>());
    }
}
```

---

## Test it: `ui/LabOneRunner.java`
```java
package ui;

import domain.*;

public class LabOneRunner {
    public static void main(String[] args) {
        CampusHub uni = new CampusHub();

        Learner alice = new UndergradLearner(1, "Alice", "alice@uni.edu");
        Learner bob = new PostgradLearner(2, "Bob", "bob@uni.edu");
        Lecturer dr = new Lecturer(1, "Dr. Smith", "smith@uni.edu", "Computer Science");
        Subject java = new Subject(1, "CS101", "Intro to Java", 3);
        Subject dbCourse = new Subject(2, "CS102", "Databases", 4);

        uni.addLearner(alice);
        uni.addLearner(bob);
        uni.addCourse(java);
        uni.addCourse(dbCourse);
        uni.addCourse(java);                 // duplicate: ignored by the Set

        uni.assignLecturer(dr, java);
        uni.enroll(alice, java);
        uni.enroll(bob, java);
        uni.enroll(alice, dbCourse);

        alice.addGrade(80);
        alice.addGrade(90);
        bob.addGrade(80);
        bob.addGrade(90);

        // Polymorphism: same method call, different behaviour
        Learner[] all = { alice, bob };
        for (Learner s : all) {
            System.out.println(s + " -> GPA = " + s.calculateGPA());
        }

        System.out.println("\nRoster of " + java.getCode() + ":");
        for (Learner s : uni.getRoster(java)) System.out.println("  " + s.getName());

        System.out.println("\nAlice's courses:");
        for (Subject c : uni.getCoursesOf(alice)) System.out.println("  " + c);

        System.out.println("\nDr. Smith teaches: " + uni.getCoursesTaughtBy(dr).size() + " course(s)");
    }
}
```

### Expected output
```
UNDERGRADUATE{id=1, name='Alice', email='alice@uni.edu'} -> GPA = 3.4
GRADUATE{id=2, name='Bob', email='bob@uni.edu'} -> GPA = 2.8

Roster of CS101:
  Alice
  Bob

Alice's courses:
  Subject{id=1, code='CS101', title='Intro to Java', credits=3, lecturer=Dr. Smith}
  Subject{id=2, code='CS102', title='Databases', credits=4, lecturer=none}
  (the order of the two lines can differ)

Dr. Smith teaches: 1 course(s)
```
Same grades (average 85), different GPA: that is polymorphism.

## Key takeaways
- **Inheritance:** `Learner` and `Lecturer` reuse `Member`.
- **Encapsulation:** private fields + getters/setters, and the grades list is returned read-only.
- **Polymorphism:** `calculateGPA()` behaves differently per subclass.
- **Collections:** `List` (ordered), `Set` (no duplicates), `Map` (key → value) each fit a different relationship.
