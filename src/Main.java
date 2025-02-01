import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/library";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Markelg06";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Connected to database!");

            // Main menu loop
            boolean running = true;
            do {
                try {
                    System.out.println("\n1. Add User");
                    System.out.println("2. Add Book");
                    System.out.println("3. View Books");
                    System.out.println("4. Search Book");
                    System.out.println("5. Delete by User");
                    System.out.println("6. Quit");
                    System.out.print("Choose an option: ");

                    if (scanner.hasNextInt()) {
                        int choice = scanner.nextInt();
                        scanner.nextLine();

                        switch (choice) {
                            case 1:
                                addUser(conn, scanner);
                                break;
                            case 2:
                                addBook(conn, scanner);
                                break;
                            case 3:
                                viewBooks(conn);
                                break;
                            case 4:
                                searchBook(conn, scanner);
                                break;
                            case 5:
                                deleteByUser(conn, scanner);
                                break;
                            case 6:
                                System.out.println("Goodbye!");
                                running = false; // Exit the program
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                                break;
                        }
                    } else {
                        System.out.println("Invalid input. Please enter a number between 1 and 6.");
                        scanner.next();
                    }
                } catch (SQLException e) {
                    System.out.println("SQL Error: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected Error: " + e.getMessage());
                }
            } while (running);

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void addUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter new User Name: ");
        String userName = scanner.nextLine();

        // Check if the user already exists
        if (userExists(conn, userName)) {
            System.out.println("User '" + userName + "' already exists. Please choose a different name.");
            return;
        }
        String query = "INSERT INTO \"User\" (UserName) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            stmt.executeUpdate();
            System.out.println("User '" + userName + "' added successfully!");
        }
    }

    private static boolean userExists(Connection conn, String userName) throws SQLException {
        String query = "SELECT 1 FROM \"User\" WHERE UserName = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void addBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter User Name: ");
        String userName = scanner.nextLine();


        if (!userExists(conn, userName)) {
            System.out.println("User not found. Please add the user first.");
            return;
        }


        int userId = getUserId(conn, userName);

        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Book Author: ");
        String author = scanner.nextLine();


        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO \"Book\" (Title, Author, Userid) VALUES (?, ?, ?)")) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
            System.out.println("Book '" + title + "' added successfully!");
        }
    }

    private static int getUserId(Connection conn, String userName) throws SQLException {
        String query = "SELECT Userid FROM \"User\" WHERE UserName = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Userid");
                }
            }
        }
        return -1;
    }

    private static void viewBooks(Connection conn) throws SQLException {
        String query = "SELECT b.Title, b.Author, u.UserName FROM \"Book\" b JOIN \"User\" u ON b.Userid = u.Userid";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- Books in the Library ---");
            System.out.printf("%-30s %-30s %-20s%n", "Title", "Author", "User");
            System.out.println("---------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-30s %-30s %-20s%n", rs.getString("Title"), rs.getString("Author"), rs.getString("UserName"));
            }
            System.out.println("---------------------------------------------");
        }
    }

    private static void searchBook(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter Book Title to Search: ");
        String title = scanner.nextLine();

        String query = "SELECT b.Title, b.Author, u.UserName FROM \"Book\" b JOIN \"User\" u ON b.Userid = u.Userid WHERE b.Title LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + title + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n--- Search Results ---");
                System.out.printf("%-30s %-30s %-20s%n", "Title", "Author", "User");
                System.out.println("---------------------------------------------");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-30s %-30s %-20s%n", rs.getString("Title"), rs.getString("Author"), rs.getString("UserName"));
                }
                if (!found) {
                    System.out.println("No books found matching the title.");
                }
                System.out.println("---------------------------------------------");
            }
        }
    }

    private static void deleteByUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter User Name to Delete Books: ");
        String userName = scanner.nextLine();

        // Get the user ID for the given username
        int userId = getUserId(conn, userName);
        if (userId == -1) {
            System.out.println("User not found. No books deleted.");
            return;
        }


        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"Book\" WHERE Userid = ?")) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Books deleted successfully for user '" + userName + "'.");
            } else {
                System.out.println("No books found for user '" + userName + "'.");
            }
        }
    }
}
