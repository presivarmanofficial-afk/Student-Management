import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class StudentManagement {

    static Scanner sc = new Scanner(System.in);

    static final String ADMIN_USER = "resi";
    static final String ADMIN_PASS = "resi@123";

    static final File DATA = new File("data/students");
    static final File BACKUP = new File("backups");

    static void initialize() {
        DATA.mkdirs();
        BACKUP.mkdirs();
    }

    static File studentFile(String reg) {
        if (!reg.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Invalid register number");

        return new File(DATA, reg + ".txt");
    }

    static Properties load(File file) throws IOException {
        Properties p = new Properties();

        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
        }

        return p;
    }

    static void save(File file, Properties p) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            p.store(out, "College Student Record");
        }
    }

    static String input(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }

    static int readInt(String message, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(input(message));

                if (n >= min && n <= max)
                    return n;

                System.out.println("Enter a value between " + min + " and " + max);

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number!");
            }
        }
    }

    static double readDouble(String message, double min, double max) {
        while (true) {
            try {
                double n = Double.parseDouble(input(message));

                if (Double.isFinite(n) && n >= min && n <= max)
                    return n;

                System.out.println("Enter a value between " + min + " and " + max);

            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number!");
            }
        }
    }

    static Properties findStudent(String reg) throws IOException {
        File file = studentFile(reg);

        if (!file.exists())
            return null;

        return load(file);
    }

    static boolean login() {
        System.out.println("\n================================");
        System.out.println("     COLLEGE ADMIN LOGIN");
        System.out.println("================================");

        for (int i = 1; i <= 3; i++) {
            String user = input("Username: ");
            String pass = input("Password: ");

            if (user.equals(ADMIN_USER) && pass.equals(ADMIN_PASS)) {
                System.out.println("\nLogin Successful!");
                return true;
            }

            System.out.println("Invalid Login! Attempts left: " + (3 - i));
        }

        return false;
    }

    static void addStudent() throws IOException {

        System.out.println("\n===== STUDENT REGISTRATION =====");

        String reg = input("Register Number: ");

        if (reg.isEmpty()) {
            System.out.println("Register number required!");
            return;
        }

        File file = studentFile(reg);

        if (file.exists()) {
            System.out.println("Student already exists!");
            return;
        }

        String name = input("Student Name: ");
        String dept = input("Department: ");
        int year = readInt("Year (1-4): ", 1, 4);
        String email = input("Email: ");
        String phone = input("Phone Number: ");

        if (name.isEmpty() || dept.isEmpty()) {
            System.out.println("Name and department are required!");
            return;
        }

        Properties p = new Properties();

        p.setProperty("reg", reg);
        p.setProperty("name", name);
        p.setProperty("department", dept);
        p.setProperty("year", String.valueOf(year));
        p.setProperty("email", email);
        p.setProperty("phone", phone);
        p.setProperty("cgpa", "0.0");

        p.setProperty("attendance", "");
        p.setProperty("semesters", "");

        save(file, p);

        System.out.println("Student Registered Successfully!");
    }


    static void displayStudent(Properties p) {

        System.out.println("\n========== STUDENT PROFILE ==========");

        System.out.println("Register Number : " + p.getProperty("reg"));
        System.out.println("Name            : " + p.getProperty("name"));
        System.out.println("Department      : " + p.getProperty("department"));
        System.out.println("Year            : " + p.getProperty("year"));
        System.out.println("Email           : " + p.getProperty("email"));
        System.out.println("Phone           : " + p.getProperty("phone"));
        System.out.println("CGPA            : " + p.getProperty("cgpa"));

        System.out.println("=====================================");
    }

    static void viewStudent() throws IOException {

        String reg = input("Enter Register Number: ");
        Properties p = findStudent(reg);

        if (p == null) {
            System.out.println("Student Not Found!");
            return;
        }

        displayStudent(p);
        showAttendance(p);
    }


    static void viewAllStudents() throws IOException {

        File[] files = DATA.listFiles(
            (dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("No Student Records Found!");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        System.out.println("\n========== ALL STUDENTS ==========");

        for (File file : files) {
            Properties p = load(file);

            System.out.println(
                p.getProperty("reg") + " | " +
                p.getProperty("name") + " | " +
                p.getProperty("department") + " | " +
                p.getProperty("year") + " Year | CGPA: " +
                p.getProperty("cgpa"));
        }
    }


    static void searchStudent() throws IOException {

        String reg = input("Enter Register Number: ");

        Properties p = findStudent(reg);

        if (p == null) {
            System.out.println("Student Not Found!");
            return;
        }

        displayStudent(p);
    }

    static void updateStudent() throws IOException {

        String reg = input("Register Number: ");
        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        Properties p = load(file);

        System.out.println("Press Enter to keep the existing value.");

        String name = input("New Name: ");
        String dept = input("New Department: ");
        String year = input("New Year: ");
        String email = input("New Email: ");
        String phone = input("New Phone: ");

        if (!name.isEmpty()) p.setProperty("name", name);
        if (!dept.isEmpty()) p.setProperty("department", dept);
        if (!email.isEmpty()) p.setProperty("email", email);
        if (!phone.isEmpty()) p.setProperty("phone", phone);

        if (!year.isEmpty()) {
            try {
                int y = Integer.parseInt(year);
                if (y < 1 || y > 4) {
                    System.out.println("Invalid year. Keeping old value.");
                } else {
                    p.setProperty("year", year);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year. Keeping old value.");
            }
        }

        save(file, p);

        System.out.println("Student Updated Successfully!");
    }

    static void deleteStudent() {

        String reg = input("Register Number: ");

        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        String confirm = input("Confirm Delete? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            if (file.delete())
                System.out.println("Student Deleted!");
            else
                System.out.println("Unable to delete student!");
        } else {
            System.out.println("Deletion Cancelled.");
        }
    }

    static void markAttendance() throws IOException {

        String reg = input("Register Number: ");
        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        Properties p = load(file);

        String date = input("Date (YYYY-MM-DD, Enter for today): ");

        if (date.isEmpty())
            date = LocalDate.now().toString();

        try {
            date = LocalDate.parse(date).toString();
        } catch (Exception e) {
            System.out.println("Invalid date!");
            return;
        }

        String key = "attendance." + date;

        if (p.containsKey(key)) {
            System.out.println("Attendance already marked for this date!");
            return;
        }

        System.out.println("1. Present");
        System.out.println("2. Absent");

        int choice = readInt("Select: ", 1, 2);

        p.setProperty(key, choice == 1 ? "P" : "A");

        save(file, p);

        System.out.println("Attendance Saved!");
    }

    static void showAttendance(Properties p) {

        int total = 0;
        int present = 0;

        System.out.println("\n===== ATTENDANCE HISTORY =====");

        for (String key : p.stringPropertyNames()) {

            if (key.startsWith("attendance.")) {

                String date = key.substring("attendance.".length());
                String status = p.getProperty(key);

                System.out.println(date + " : " +
                    (status.equals("P") ? "Present" : "Absent"));

                total++;

                if (status.equals("P"))
                    present++;
            }
        }

        double percentage = total == 0 ? 0 :
            present * 100.0 / total;

        System.out.println("Total Classes: " + total);
        System.out.println("Present: " + present);
        System.out.println("Absent: " + (total - present));

        System.out.printf("Attendance Percentage: %.2f%%\n",
            percentage);

        if (total == 0)
            System.out.println("No attendance records yet.");
        else if (percentage >= 75)
            System.out.println("Attendance Status: Eligible (75% rule)");
        else
            System.out.println("Attendance Status: Below 75%");
    }


    static void updateSemester() throws IOException {

        String reg = input("Register Number: ");
        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        Properties p = load(file);

        int sem = readInt("Semester (1-8): ", 1, 8);

        double gpa = readDouble("Enter Semester GPA (0-10): ", 0, 10);

        p.setProperty("semester." + sem, String.valueOf(gpa));

        save(file, p);

        System.out.println("Semester Result Updated!");
    }

    static void academicReport() throws IOException {

        String reg = input("Register Number: ");
        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        Properties p = load(file);

        System.out.println("\n===== ACADEMIC REPORT =====");

        double total = 0;
        int count = 0;

        for (int i = 1; i <= 8; i++) {

            String value = p.getProperty("semester." + i);

            if (value != null) {
                double gpa = Double.parseDouble(value);

                System.out.println("Semester " + i + " GPA: " + gpa);

                total += gpa;
                count++;
            }
        }

        if (count > 0) {
            double cgpa = total / count;

            p.setProperty("cgpa", String.format(Locale.US, "%.2f", cgpa));

            save(file, p);

            System.out.printf("Calculated CGPA: %.2f\n", cgpa);
        } else {
            System.out.println("No Semester Records Found!");
        }
    }


    static void performanceReport() throws IOException {

        String reg = input("Register Number: ");
        File file = studentFile(reg);

        if (!file.exists()) {
            System.out.println("Student Not Found!");
            return;
        }

        Properties p = load(file);

        System.out.println("\n===== STUDENT PERFORMANCE =====");

        System.out.println("Name: " + p.getProperty("name"));

        double cgpa = Double.parseDouble(
            p.getProperty("cgpa", "0.0"));

        System.out.println("CGPA: " + cgpa);

        if (cgpa >= 9)
            System.out.println("Performance: Outstanding");
        else if (cgpa >= 8)
            System.out.println("Performance: Excellent");
        else if (cgpa >= 7)
            System.out.println("Performance: Good");
        else if (cgpa >= 5)
            System.out.println("Performance: Average");
        else
            System.out.println("Performance: Needs Improvement");

        showAttendance(p);
    }


    static void backupRecords() throws IOException {

        File[] files = DATA.listFiles(
            (dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("No records available for backup!");
            return;
        }

        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss"));

        File backupFolder = new File(BACKUP, "backup_" + timestamp);
        backupFolder.mkdirs();

        for (File file : files) {
            Files.copy(
                file.toPath(),
                new File(backupFolder, file.getName()).toPath()
            );
        }

        System.out.println("Backup Created Successfully!");
        System.out.println("Location: " + backupFolder.getPath());
    }


    static void menu() {

        while (true) {

            System.out.println("\n======================================");
            System.out.println("   COLLEGE STUDENT MANAGEMENT SYSTEM");
            System.out.println("======================================");

            System.out.println("1.  Register Student");
            System.out.println("2.  View Student");
            System.out.println("3.  View All Students");
            System.out.println("4.  Search Student");
            System.out.println("5.  Update Student");
            System.out.println("6.  Delete Student");
            System.out.println("7.  Mark Attendance");
            System.out.println("8.  Attendance Report");
            System.out.println("9.  Update Semester GPA");
            System.out.println("10. Academic Report");
            System.out.println("11. Performance Report");
            System.out.println("12. Backup Records");
            System.out.println("13. Logout and Exit");

            System.out.println("======================================");

            int choice = readInt("Enter Your Choice: ", 1, 13);

            try {

                switch (choice) {

                    case 1:
                        addStudent();
                        break;

                    case 2:
                        viewStudent();
                        break;

                    case 3:
                        viewAllStudents();
                        break;

                    case 4:
                        searchStudent();
                        break;

                    case 5:
                        updateStudent();
                        break;

                    case 6:
                        deleteStudent();
                        break;

                    case 7:
                        markAttendance();
                        break;

                    case 8: {
                        String reg = input("Register Number: ");
                        Properties p = findStudent(reg);

                        if (p == null)
                            System.out.println("Student Not Found!");
                        else
                            showAttendance(p);

                        break;
                    }

                    case 9:
                        updateSemester();
                        break;

                    case 10:
                        academicReport();
                        break;

                    case 11:
                        performanceReport();
                        break;

                    case 12:
                        backupRecords();
                        break;

                    case 13:
                        System.out.println("Thank You!");
                        return;
                }

            } catch (IOException e) {
                System.out.println("File Error: " + e.getMessage());

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid Input: " + e.getMessage());

            } catch (Exception e) {
                System.out.println("Unexpected Error: " + e.getMessage());
            }
        }
    }


    public static void main(String[] args) {

        initialize();

        System.out.println("\n**************************************");
        System.out.println("  WELCOME TO COLLEGE STUDENT SYSTEM");
        System.out.println("**************************************");

        if (login()) {
            menu();
        } else {
            System.out.println("Too Many Failed Attempts. Exiting...");
        }

        sc.close();
    }
}
