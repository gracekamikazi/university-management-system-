package persistence;

// A record is a short way to write a class that just holds data (needs JDK 16+)
public record RegistrationRecord(int studentId, String studentName,
                             String courseCode, String courseTitle, Double grade) {
}
