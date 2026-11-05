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
