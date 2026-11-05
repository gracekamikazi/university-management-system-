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
