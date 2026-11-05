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
