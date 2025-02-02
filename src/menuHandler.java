import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;



    public class menuHandler {
        private Connection conn;
        private Scanner scanner;

        public menuHandler(Connection conn, Scanner scanner) {
            this.conn = conn;
            this.scanner = scanner;
        }

        public void startMenu() {
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
                                Main.addUser(conn, scanner);
                                break;
                            case 2:
                                Main.addBook(conn, scanner);
                                break;
                            case 3:
                                Main.viewBooks(conn);
                                break;
                            case 4:
                                Main.searchBook(conn, scanner);
                                break;
                            case 5:
                                Main.deleteByUser(conn, scanner);
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
        }
    }


