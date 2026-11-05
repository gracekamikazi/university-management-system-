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
