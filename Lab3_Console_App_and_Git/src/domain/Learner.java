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
