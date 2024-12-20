package SalesSystemAdmin;

import java.sql.*;
import java.io.*;
import java.util.Scanner;

public class SalesSystemDatabase {

    // JDBC URL, user name, and password of Oracle database
    protected static final String DB_URL = "jdbc:oracle:thin:@//db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
    protected static final String DB_USER = "h002";
    protected static final String DB_PASSWORD = "tutjoibA";

    // JDBC Connection
    protected Connection connection;

    // Constructor to connect to the database
    public SalesSystemDatabase() {
        try {
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the Oracle Database.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public void createTables() {
        try (Statement stmt = connection.createStatement()) {

            // Create tables
            stmt.executeUpdate("""
                CREATE TABLE category (
                    cID INTEGER PRIMARY KEY,
                    cName VARCHAR2(20) NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE manufacturer (
                    mID INTEGER PRIMARY KEY,
                    mName VARCHAR2(20) NOT NULL,
                    mAddress VARCHAR2(50) NOT NULL,
                    mPhoneNumber INTEGER NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE part (
                    pID INTEGER PRIMARY KEY,
                    pName VARCHAR2(20) NOT NULL,
                    pPrice INTEGER NOT NULL,
                    mID INTEGER NOT NULL,
                    cID INTEGER NOT NULL,
                    pWarrantyPeriod INTEGER NOT NULL,
                    pAvailableQuantity INTEGER NOT NULL,
                    FOREIGN KEY (mID) REFERENCES manufacturer(mID),
                    FOREIGN KEY (cID) REFERENCES category(cID)
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE salesperson (
                    sID INTEGER PRIMARY KEY,
                    sName VARCHAR2(20) NOT NULL,
                    sAddress VARCHAR2(50) NOT NULL,
                    sPhoneNumber INTEGER NOT NULL,
                    sExperience INTEGER NOT NULL
                )
            """);

            stmt.executeUpdate("""
                CREATE TABLE transaction (
                    tID INTEGER PRIMARY KEY,
                    pID INTEGER NOT NULL,
                    sID INTEGER NOT NULL,
                    tDate DATE NOT NULL,
                    FOREIGN KEY (pID) REFERENCES part(pID),
                    FOREIGN KEY (sID) REFERENCES salesperson(sID)
                )
            """);

            System.out.println("Processing...Done! Database is initialized!");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void deleteTables() {
        String[] tables = {"transaction", "salesperson", "part", "manufacturer", "category"};
        try (Statement stmt = connection.createStatement()) {
            for (String table : tables) {
                try {
                    stmt.executeUpdate("DROP TABLE " + table);
                    System.out.println("Table " + table + " dropped.");
                } catch (SQLException e) {
                    System.out.println("Table " + table + " does not exist.");
                }
            }
            System.out.println("Processing...Done! Database is removed!");
        } catch (SQLException e) {
            System.err.println("Error deleting tables: " + e.getMessage());
        }
    }

    public void insertData(String filePath, String tableName) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("Error: File " + filePath + " not found for table " + tableName);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file));
             PreparedStatement pstmt = prepareInsertStatement(tableName)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\t"); // Split line by tab
                int expectedColumns = getExpectedColumnCount(tableName);

                if (data.length != expectedColumns) {
                    System.err.println("Error: Incorrect number of columns in file " + filePath +
                            " for table " + tableName + ". Expected " + expectedColumns + ", got " + data.length);
                    continue;
                }

                for (int i = 0; i < data.length; i++) {
                    pstmt.setString(i + 1, data[i].trim());
                }

                pstmt.executeUpdate();
            }

            System.out.println("Processing...Done! Data is inputted to the database!");

        } catch (IOException | SQLException e) {
            System.err.println("Error inserting data into " + tableName + ": " + e.getMessage());
        }
    }

    private PreparedStatement prepareInsertStatement(String tableName) throws SQLException {
        return switch (tableName) {
            case "category" -> connection.prepareStatement("INSERT INTO category VALUES (?, ?)");
            case "manufacturer" -> connection.prepareStatement("INSERT INTO manufacturer VALUES (?, ?, ?, ?)");
            case "part" -> connection.prepareStatement("INSERT INTO part VALUES (?, ?, ?, ?, ?, ?, ?)");
            case "salesperson" -> connection.prepareStatement("INSERT INTO salesperson VALUES (?, ?, ?, ?, ?)");
            case "transaction" -> connection.prepareStatement("INSERT INTO transaction VALUES (?, ?, ?, TO_DATE(?, 'DD/MM/YYYY'))");
            default -> throw new SQLException("Unknown table: " + tableName);
        };
    }

    private int getExpectedColumnCount(String tableName) {
        return switch (tableName) {
            case "category" -> 2;
            case "manufacturer" -> 4;
            case "part" -> 7;
            case "salesperson" -> 5;
            case "transaction" -> 4;
            default -> throw new IllegalArgumentException("Unknown table: " + tableName);
        };
    }

    public void showTableContent(String tableName) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t");
            }
            System.out.println();

            // Print rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }

        } catch (SQLException e) {
            System.err.println("Error showing content of table " + tableName + ": " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    // Main interactive menu
    public void runMenu() {
        Scanner scanner = new Scanner(System.in);
        Manager manager = new Manager();
        Saleperson saleperson = new Saleperson();
        
        while (true) {
            System.out.println("Welcome to sales system!");
            System.out.println("-----Main menu-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Operations for administrator");
            System.out.println("2. Operations for salesperson");
            System.out.println("3. Operations for manager");
            System.out.println("4. Exit this program");
            System.out.print("Enter Your Choice: ");

            int mainChoice = scanner.nextInt();

            if (mainChoice == 1) {
                administratorMenu(scanner);
            } else if (mainChoice == 2) {
            	saleperson.listMenu();
            } else if (mainChoice == 3) {
            	manager.manager(scanner);
            } else if (mainChoice == 4) {
            	System.out.println("Exiting...");
                break;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void administratorMenu(Scanner scanner) {
        while (true) {
            System.out.println("-----Operations for administrator menu-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Create all tables");
            System.out.println("2. Delete all tables");
            System.out.println("3. Load from datafile");
            System.out.println("4. Show content of a table");
            System.out.println("5. Return to the main menu");
            System.out.print("Enter Your Choice: ");

            int adminChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (adminChoice) {
                case 1 -> createTables();
                case 2 -> deleteTables();
                case 3 -> {
                    System.out.print("Type in the Source Data Folder Path: ");
                    String folderPath = scanner.nextLine();
                    String[] tables = {"category", "manufacturer", "part", "salesperson", "transaction"};
                    for (String table : tables) {
                        insertData(folderPath + File.separator + table + ".txt", table);
                    }
                }
                case 4 -> {
                    System.out.print("Which table would you like to show: ");
                    String tableName = scanner.nextLine();
                    showTableContent(tableName);
                }
                case 5 -> {
                    return; // Return to main menu
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        SalesSystemDatabase db = new SalesSystemDatabase();
        db.runMenu();
        db.closeConnection();
    }
}