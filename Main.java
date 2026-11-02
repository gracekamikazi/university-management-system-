import java.util.Scanner;
import dao.StudentDAO;
import model.Student;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StudentDAO dao = new StudentDAO();
        
        while(true){
            System.out.println("\n--- University System ---");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            if(choice == 1){
                System.out.print("Enter name: ");
                String name = scanner.nextLine();
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                System.out.print("Enter course: ");
                String course = scanner.nextLine();
                
                Student s = new Student(name, email, course);
                dao.addStudent(s);
                
            } else if(choice == 2){
                dao.getAllStudents();
                
            } else if(choice == 3){
                System.out.println("Goodbye!");
                break;
            }
        }
        scanner.close();
    }
}