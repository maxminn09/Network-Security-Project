import java.util.Scanner;
public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String token = null;
        String username = null;

        System.out.println("Welcome to Mako Online Portal.");

        while (true) {
            System.out.println("\nAre you: ");
            System.out.println("1. Admin");
            System.out.println("2. Client");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int roleChoice = scanner.nextInt();
            scanner.nextLine();

            switch (roleChoice) {
                case 1:
                    adminMenu(scanner);
                    break;
                case 2:
                    clientLogin(scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void adminMenu(Scanner scanner) {
        String token = null;
        String username = null;

        System.out.println("Welcome to Mako Online Portal Admin Console.");

        while (true) {
            displayMenu();

            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (requiresAdmin(choice) && !isAdminAuthorized(token, username)) {
                System.out.println("Permission denied. Please login as admin.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter username: ");
                    username = scanner.nextLine();
                    Controller.initializeServer();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();


                    if(Controller.verifyPassword(username, password)) {
                        token = Controller.generateToken(username);
                        if(token != null && !token.isEmpty()) {
                            System.out.println("Logged in successfully. Token generated!");
                        } else {
                            System.out.println("Error generating token. Please try again.");
                        }
                    } else {
                        System.out.println("Invalid username or password.");

                    }

                    break;

                case 2:
                    System.out.print("Enter new username: ");
                    String newUser = scanner.nextLine();
                    System.out.print("Enter new user's email: ");
                    String email = scanner.nextLine();
                    System.out.println(Controller.addUser(newUser, email));
                    break;

                case 3:
                    System.out.print("Enter username to modify: ");
                    String modUser = scanner.nextLine();
                    System.out.print("Enter new group: ");
                    String newGroup = scanner.nextLine();
                    System.out.print("Enter new security level: ");
                    String newSecurityLevel = scanner.nextLine();
                    System.out.println(Controller.modifyUser(modUser, newGroup, newSecurityLevel));
                    break;

                case 4:
                    System.out.print("Enter username to delete: ");
                    String delUser = scanner.nextLine();
                    System.out.println(Controller.deleteUser(delUser));
                    break;

                case 5:
                    System.out.print("Enter the username of the user to verify: ");
                    String verifyUser = scanner.nextLine();
                    System.out.print("Enter the code sent to the user: ");
                    String sentCode = scanner.nextLine();

                    if (Controller.verifyMFACode(verifyUser, sentCode)) {
                        System.out.println("User verified successfully.");
                    } else {
                        System.out.println("Verification failed. The provided code doesn't match.");
                    }
                    break;

                case 6:
                    token = null;
                    username = null;
                    System.out.println("Logged out successfully.");
                    return;

                case 7:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nPlease select an operation:");
        System.out.println("1. Login");
        System.out.println("2. Add User");
        System.out.println("3. Modify User");
        System.out.println("4. Delete User");
        System.out.println("5. Verify MFA (Client)");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
    }

    private static boolean requiresAdmin(int choice) {
        return choice >= 2 && choice <= 5;
    }

    private static boolean isAdminAuthorized(String token, String username) {
        return token != null && Controller.validateToken(token) && Controller.isAdmin(username);
    }

    private static void clientLogin(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();


        if(!Controller.verifyPassword(username, password)) {
            System.out.println("Incorrect password.");
            return;
        }

        System.out.print("Enter MFA code sent to your email: ");
        String mfaCode = scanner.nextLine();

        if (Controller.verifyMFACode(username, mfaCode)) {
            System.out.println("Logged in successfully.");
            clientMenu(scanner, username);
        } else {
            System.out.println("Failed to login. Please check your MFA code.");
        }
    }

    private static void clientMenu(Scanner scanner, String username) {
        while (true) {
            System.out.println("\nWelcome, " + username);
            System.out.println("1. View Profile");
            System.out.println("2. Audit Expenses");
            System.out.println("3. Add Expense");
            System.out.println("4. Audit Timesheets");
            System.out.println("5. Submit Timesheet");
            System.out.println("6. View Meeting Minutes");
            System.out.println("7. Add Meeting Minutes");
            System.out.println("8. View Roster");
            System.out.println("9. Roster Shift");
            System.out.println("10. Logout");
            System.out.println("11. Exit");

            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println(username + ", this is your profile.");
                    break;
                case 2:
                    System.out.println(Controller.auditExpenses(username));
                    break;
                case 3:
                    System.out.println("Enter expense data: ");
                    String expenseData = scanner.nextLine();
                    System.out.println(Controller.addExpense(username, expenseData));
                    break;
                case 4:
                    System.out.println(Controller.auditTimesheets(username));
                    break;
                case 5:
                    System.out.println("Enter timesheet data: ");
                    String timesheetData = scanner.nextLine();
                    System.out.println(Controller.submitTimesheet(username, timesheetData));
                    break;
                case 6:
                    System.out.println(Controller.viewMeetingMinutes(username));
                    break;
                case 7:
                    System.out.println("Enter meeting data: ");
                    String meetingData = scanner.nextLine();
                    System.out.println(Controller.addMeetingMinutes(username, meetingData));
                    break;
                case 8:
                    System.out.println(Controller.viewRoster(username));
                    break;
                case 9:
                    System.out.println("Enter roster shift data: ");
                    String shiftData = scanner.nextLine();
                    System.out.println(Controller.rosterShift(username, shiftData));
                    break;
                case 10:
                    System.out.println("Logged out successfully.");
                    return;
                case 11:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
