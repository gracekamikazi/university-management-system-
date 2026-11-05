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
