package SalesSystemAdmin;

import java.sql.*;
import java.util.Scanner;

public class Manager extends SalesSystemDatabase {
	
	private void listAllSalespersons(String order) {
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT sID, sName, sPhoneNumber, sExperience FROM salesperson ORDER BY sExperience " + order);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println(" | ID | Name | Mobile Phone | Years of Experience |");
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(" | " + rs.getString(i));
				}
				System.out.println(" |");
			}
		} catch (SQLException e) {
			System.err.println("Error listing all salespersons: " + e.getMessage());
		}
	}
	
	private void countSalesRecord(int lowerBound, int upperBound) {
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(
					"SELECT s.sID, s.sName, s.sExperience, COUNT(t.tID) AS transaction_count " + 
					"FROM salesperson s LEFT JOIN transaction t ON s.sID = t.sID " + 
					"WHERE s.sExperience BETWEEN " + lowerBound + " AND " + upperBound +
					" GROUP BY s.sID, s.sName, s.sExperience ORDER BY s.sID DESC");
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println(" | ID | Name | Years of Experience | Number of Transaction |");
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(" | " + rs.getString(i));
				}
				System.out.println(" |");
			}
		} catch (SQLException e) {
			System.err.println("Error counting sales record: " + e.getMessage());
		}
	}
	
	private void showTotalSales() {
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery(
					"SELECT m.mID, m.mName, SUM(p.pPrice) AS total_sales FROM manufacturer m " +
                            "JOIN part p on m.mID = p.mID JOIN transaction t ON p.pID = t.pID " +
                            "GROUP BY m.mID, m.mName " +
                            "ORDER BY total_sales DESC");
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println(" | Manufacturer ID | Manufacturer Name | Total Sales Value |");
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(" | " + rs.getString(i));
				}
				System.out.println(" |");
			}
		} catch (SQLException e) {
			System.err.println("Error counting sales record: " + e.getMessage());
		}
	}
	
	private void showNPopular(int N) {
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT p.pID, p.pName, COUNT(t.tID) AS total_transactions " +
                    "FROM part p LEFT JOIN transaction t ON p.pID = t.pID " +
                    "GROUP BY p.pID, p.pName ORDER BY total_transactions DESC " +
                    "FETCH FIRST " + N + " ROWS ONLY");
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			System.out.println(" | Part ID | Part Name | Number of Transaction |");
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					System.out.print(" | " + rs.getString(i));
				}
				System.out.println(" |");
			}
		} catch (SQLException e) {
			System.err.println("Error counting sales record: " + e.getMessage());
		}
	}
	
	protected void manager (Scanner scanner) {
		while (true) {
			System.out.println();
			System.out.println("-----Operations for manager menu-----");
			System.out.println("What kinds of operation would you like to perform?");
			System.out.println("1. List all salespersons");
			System.out.println("2. Count the no. of sales records of each salesperson within a specific range on years of experience");
			System.out.println("3. Show the total sales value of each manufacturer");
			System.out.println("4. Show the N most popular part");
			System.out.println("5. Return to the main menu");
			System.out.print("Enter Your Choice: ");
			
			int managerChoice = 0;
			if (scanner.hasNextInt()) {
				managerChoice = scanner.nextInt();
				scanner.nextLine(); // Consume newline
				String order = "";
				
				switch (managerChoice) {
					case 1 -> {
						while (true) {
							System.out.println();
							System.out.println("Choose ordering: ");
							System.out.println("1. By ascending order");
							System.out.println("2. By descending order");
							System.out.print("Choose the list ordering: ");
							int choice = scanner.nextInt();
							switch (choice) {
							case 1 -> order = "ASC";
							case 2 -> order = "DESC";
							default -> System.out.println("Invalid choice. Please try again.");
							}
							if (order != "") {
								listAllSalespersons(order);
								break;
							}
						}
					}
					case 2 -> {
						int lowerBound = 0;
						int upperBound = 0;
						while (true) {
							System.out.print("Type in the lower bound for years of experience: ");
							if (scanner.hasNextInt()) {
								lowerBound = scanner.nextInt();
								scanner.nextLine();
								if (lowerBound < 1) {
									System.out.println("Invalid choice. Please try again.");
									continue;
								}
								break;
							} else {
								System.out.println("Invalid choice. Please try again.");
								scanner.next();
							}
						}
						while (true) {
							System.out.print("Type in the upper bound for years of experience: ");
							if (scanner.hasNextInt()) {
								upperBound = scanner.nextInt();
								scanner.nextLine();
								if (upperBound < 1) {
									System.out.println("Invalid choice. Please try again.");
									continue;
								}
								break;
							} else {
								System.out.println("Invalid choice. Please try again.");
								scanner.next();
							}
						}
						countSalesRecord(lowerBound, upperBound);
					}
					case 3 -> {
						showTotalSales();
					}
					case 4 -> {
						int N = 0;
						while (true) {
							System.out.print("Type in the number of parts: ");
							if (scanner.hasNextInt()) {
								N = scanner.nextInt();
								scanner.nextLine();
								if (N < 1) {
									System.out.println("Invalid choice. Please try again.");
									continue;
								}
								break;
							} else {
								System.out.println("Invalid choice. Please try again.");
								scanner.next();
							}
						}
						showNPopular(N);
					}
					case 5 -> {
						return; // Return to main menu
					}
					default -> {
						System.out.println("Invalid choice. Please try again.");
						continue;
					}
				}
			} else {
				System.out.println("Invalid choice. Please try again.");
				scanner.next();
				continue;
			}
			System.out.println("End of Query");
		}
	}
}