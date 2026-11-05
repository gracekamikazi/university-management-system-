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
