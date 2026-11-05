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
