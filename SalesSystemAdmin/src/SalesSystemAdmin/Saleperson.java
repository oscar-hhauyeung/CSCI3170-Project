package SalesSystemAdmin;

import java.sql.*;
import java.util.Scanner;

public class Saleperson extends SalesSystemDatabase {
    private Scanner scanner;

    public Saleperson() {
        this.scanner = new Scanner(System.in);
    }

    public void listMenu() {
        while (true) {
            System.out.println("\n-----Operations for salesperson menu-----");
            System.out.println("What kinds of operation would you like to perform?");
            System.out.println("1. Search for parts");
            System.out.println("2. Sell a part");
            System.out.println("3. Return to the main menu");
            System.out.print("Enter Your Choice: ");

            int menuChoice = scanner.nextInt();
            scanner.nextLine();  
            switch (menuChoice) {
                case 1:
                    searchPart();
                    break;
                case 2:
                    sellPart();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input! Please try again.");
            }
        }
    }

    public void searchPart() {
        while (true) {
            System.out.println("Choose the Search criterion:");
            System.out.println("1. Part Name");
            System.out.println("2. Manufacturer Name");

            int menuChoice = scanner.nextInt();
            scanner.nextLine();  

            switch (menuChoice) {
                case 1:
                    System.out.println("Enter Part Name");
                    processSearching(1);
                    return;
                case 2:
                    System.out.println("Enter Manufacturer Name");
                    processSearching(2);
                    return;
                default:
                    System.out.println("Invalid input! Please try again.");
            }
        }
    }

    // Only 2 criteria: part, manufacturer
    public void processSearching(int criteria) {
        while (true) {
            System.out.println("Type in the Search keyword: ");
            if (scanner.hasNext()) {
                String keyword = scanner.nextLine();
            	System.out.println("keyword" + keyword);
                System.out.println("Choose ordering: ");
                System.out.println("1. By price, ascending order");
                System.out.println("2. By price, descending order");
                System.out.print("Choose the search criterion: ");

                int ordering = scanner.nextInt();
                scanner.nextLine(); 

                switch (ordering) {
                    case 1:
                        partSearching(criteria, keyword, 1);
                        return;
                    case 2:
                        partSearching(criteria, keyword, 2);
                        return;
                    default:
                        System.out.println("Invalid input! Please try again.");
                }
            }
        }
    }

    public void partSearching(int criteria, String keyword, Integer order) {
        System.out.println("criteria: " + criteria);
        String query = "";

        if (criteria == 1) {
            query = """
                    SELECT pID as ID, pName as Name, mName as Manufacturer, cName AS Category, 
                    pAvailableQuantity as Quantity, pWarrantyPeriod AS Warranty, pPrice AS Price
                    FROM part
                    INNER JOIN manufacturer ON part.mID = manufacturer.mID
                    INNER JOIN category ON part.cID = category.cid
                    WHERE part.pName LIKE ?
                    ORDER BY pPrice """ + (order == 1 ? " ASC" : " DESC");
        } else if (criteria == 2) {
            query = """
                    SELECT pID as ID, pName as Name, mName as Manufacturer, cName AS Category, 
                    pAvailableQuantity as Quantity, pWarrantyPeriod AS Warranty, pPrice AS Price
                    FROM part
                    INNER JOIN manufacturer ON part.mID = manufacturer.mID
                    INNER JOIN category ON part.cID = category.cid
                    WHERE manufacturer.mName LIKE ?
                    ORDER BY pPrice """ + (order == 1 ? " ASC" : " DESC");
        }

        try (PreparedStatement pstat = connection.prepareStatement(query)) {
            pstat.setString(1, "%" + keyword + "%");
            ResultSet rs = pstat.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCnt = metaData.getColumnCount();
                System.out.println(" | ID | Name | Manufacturer | Category | Quantity | Warranty | Price |");
                while (rs.next()) {
    				for (int i = 1; i <= columnCnt; i++) {
    					System.out.print(" | " + rs.getString(i));
    				}
    				System.out.println(" |");
    			}
        } catch (SQLException e) {
            System.err.println("Error searching parts: " + e.getMessage());
        }
    }

    public void sellPart() {
        System.out.println("Enter The Part ID:");
        int partID = scanner.nextInt();
        System.out.println("Enter The Salesperson ID:");
        int salespersonID = scanner.nextInt();
        
        try (Statement stat = connection.createStatement()) {
            ResultSet rs = stat.executeQuery("SELECT pAvailableQuantity, pName FROM part WHERE pID = " + partID);

            if (rs.next()) {
                int availableQuantity = rs.getInt("pAvailableQuantity");
                String partName = rs.getString("pName");

                if (availableQuantity > 0) {
                    stat.executeUpdate("UPDATE part SET pAvailableQuantity = pAvailableQuantity - 1 WHERE pID =" + partID);

                    String query = "INSERT INTO transaction (pID, sID, tDate) values (?, ?, SYSDATE)";
                    try (PreparedStatement pstat = connection.prepareStatement(query)) {
                        pstat.setInt(1, partID);
                        pstat.setInt(2, salespersonID);
                        pstat.executeUpdate();
                        System.out.println("Product: " + partName + "(id: " + partID + ") Remaining Quantity: " + (availableQuantity - 1));
                    }
                } else {
                    System.err.println("Error: The part is not available for sale");
                }
            } else {
                System.out.println("Error: The part is not found");
            }
        } catch (SQLException e) {
            System.out.println("Error in selling part: " + e.getMessage());
        }
    }
}
