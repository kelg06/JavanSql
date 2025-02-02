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

            // Start menu
            menuHandler menuHandler = new menuHandler(conn, scanner);
            menuHandler.startMenu();

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    // Keep all database functions public so MenuHandler can access them
    public static void addUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter new User Name: ");
        String userName = scanner.nextLine();

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

    public static boolean userExists(Connection conn, String userName) throws SQLException {
        String query = "SELECT 1 FROM \"User\" WHERE UserName = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void addBook(Connection conn, Scanner scanner) throws SQLException {
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

    public static int getUserId(Connection conn, String userName) throws SQLException {
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

    public static void viewBooks(Connection conn) throws SQLException {
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

    public static void searchBook(Connection conn, Scanner scanner) throws SQLException {
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

    public static void deleteByUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter User Name to Delete Books: ");
        String userName = scanner.nextLine();

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
