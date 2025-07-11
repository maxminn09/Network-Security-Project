import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Controller {
    public enum SecurityLevel {
        TOP_SECRET, SECRET, UNCLASSIFIED;
    }
    private static class User {
        String username;
        String email;
        String password;
        String group;
        SecurityLevel securityLevel;
        String mfaCode;

        User(String username, String email, String password, String group, SecurityLevel securityLevel) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.group = group;
            this.securityLevel = securityLevel;
        }
    }

    private static Map<String, User> users = new HashMap<>();
    private static Map<String, String> userTokens = new HashMap<>();
    private static Map<String, Long> tokenTimestamps = new HashMap<>();
    private static Map<String, String> userMfaCodes = new HashMap<>();
    private static final String SMTP_SERVER = "in-v3.mailjet.com";
    private static final String SMTP_USERNAME = "996b949b0cd39cb4e6556483aed8ee9a";
    private static final String SMTP_PASSWORD = "3a0f925fa44dde18ab3023a28c605889";

    public static void initializeServer() {

    }

    static {
        String randomPassword = generateRandomPassword(8);
        System.out.println("Generated root password: " + randomPassword);
        String hashedPassword = BCrypt.hashpw(randomPassword, BCrypt.gensalt());
        User root = new User("root", "root@email.com", hashedPassword, "admin", SecurityLevel.TOP_SECRET);

        users.put("root", root);
    }


    public static String addUser(String username, String email) {
        if (users.containsKey(username)) {
            return "User already exists!";
        }

        String randomPassword = generateRandomPassword(8);
        String hashedPassword = BCrypt.hashpw(randomPassword, BCrypt.gensalt());
        User newUser = new User(username, email, hashedPassword, "standard", SecurityLevel.UNCLASSIFIED);

        users.put(username, newUser);

        String mfaCode = generateMFACode(6);
        userMfaCodes.put(username, mfaCode);

        sendEmail(email,
                "Welcome to the system of SENG2250, " + username,
                "Dear " + username + ",\n" +
                        "Please find your account details below. Do not share your password or MFA code with anyone." +
                        "\nUsername: " + username +
                        "\nPassword: " + randomPassword +
                        "\nMFA Code: " + mfaCode);

        return "User added successfully! Email with credentials and MFA code sent!";
    }


    public static boolean verifyPassword(String username, String password) {
        if(!users.containsKey(username)) return false;

        User user = users.get(username);
        return BCrypt.checkpw(password, user.password);
    }

    public static String modifyUser(String username, String newGroup, String newSecurityLevel) {
        if (!users.containsKey(username)) {
            return "User not found!";
        }

        User user = users.get(username);
        try {
            user.group = newGroup;
            user.securityLevel = SecurityLevel.valueOf(newSecurityLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid security level provided!";
        }

        return "User modified successfully!";
    }


    public static String deleteUser(String username) {
        if (!users.containsKey(username)) {
            return "User not found!";
        }

        users.remove(username);
        return "User deleted successfully!";
    }

    public static boolean verifyMFACode(String username, String sentCode) {
        if (!users.containsKey(username)) {
            return false;
        }
        String storedCode = userMfaCodes.get(username);
        if (sentCode.equals(storedCode)) {
            userMfaCodes.remove(username);
            return true;
        }
        return false;
    }

    public static boolean validateToken(String token) {
        if (!tokenTimestamps.containsKey(token)) return false;

        long tokenTime = tokenTimestamps.get(token);
        long timeNow = System.currentTimeMillis();
        long fifteenMinutesInMillis = 15 * 60 * 1000;

        return (timeNow - tokenTime) <= fifteenMinutesInMillis;
    }

    public static String generateToken(String username) {
        if (!users.containsKey(username)) {
            return "User not found!";
        }

        String mfaCode = generateMFACode(6);
        userMfaCodes.put(username, mfaCode);
        sendEmail(users.get(username).email, "MFA Verification", "Your MFA code is: " + mfaCode);

        String token = UUID.randomUUID().toString();
        userTokens.put(username, token);
        tokenTimestamps.put(token, System.currentTimeMillis());

        return token;
    }

    public static boolean isAdmin(String username) {
        User user = users.get(username);
        return user != null && "admin".equals(user.group);
    }

    private static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random rnd = new Random();
        while (password.length() < length) {
            int index = (int) (rnd.nextFloat() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    private static String generateMFACode(int length) {
        String chars = "0123456789";
        StringBuilder mfaCode = new StringBuilder();
        Random rnd = new Random();
        while (mfaCode.length() < length) {
            int index = (int) (rnd.nextFloat() * chars.length());
            mfaCode.append(chars.charAt(index));
        }
        return mfaCode.toString();
    }

    private static void sendEmail(String to, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_SERVER);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
			//change the email to your email when testing
            message.setFrom(new InternetAddress("example@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
    private static SecurityLevel stringToSecurityLevel(String level) {
        switch(level) {
            case "Top Secret":
                return SecurityLevel.TOP_SECRET;
            case "Secret":
                return SecurityLevel.SECRET;
            default:
                return SecurityLevel.UNCLASSIFIED;
        }
    }


    public static boolean canRead(User user, SecurityLevel resourceSecurityLevel) {
        return user.securityLevel.ordinal() <= resourceSecurityLevel.ordinal();
    }

    public static boolean canWrite(User user, SecurityLevel resourceSecurityLevel) {
        return user.securityLevel.ordinal() >= resourceSecurityLevel.ordinal();
    }

    public static String auditExpenses(String username) {
        User user = users.get(username);
        if (user != null && canRead(user, SecurityLevel.TOP_SECRET)) {
            return "Expense data";
        }
        return "Access denied";
    }

    public static String addExpense(String username, String expenseData) {
        User user = users.get(username);
        if (user != null && canWrite(user, SecurityLevel.TOP_SECRET)) {

            return "Expense added";
        }
        return "Access denied";
    }

    public static String auditTimesheets(String username) {
        User user = users.get(username);
        if (user != null && canRead(user, SecurityLevel.TOP_SECRET)) {
            return "Timesheet data";
        }
        return "Access denied";
    }

    public static String submitTimesheet(String username, String timesheetData) {
        User user = users.get(username);
        if (user != null && canWrite(user, SecurityLevel.TOP_SECRET)) {

            return "Timesheet submitted";
        }
        return "Access denied";
    }

    public static String viewMeetingMinutes(String username) {
        User user = users.get(username);
        if (user != null && canRead(user, SecurityLevel.SECRET)) {
            return "Meeting minutes data";
        }
        return "Access denied";
    }

    public static String addMeetingMinutes(String username, String meetingData) {
        User user = users.get(username);
        if (user != null && canWrite(user, SecurityLevel.SECRET)) {

            return "Meeting minutes added";
        }
        return "Access denied";
    }

    public static String viewRoster(String username) {
        User user = users.get(username);
        if (user != null && canRead(user, SecurityLevel.UNCLASSIFIED)) {
            return "Roster data";
        }
        return "Access denied";
    }

    public static String rosterShift(String username, String shiftData) {
        User user = users.get(username);
        if (user != null && canWrite(user, SecurityLevel.UNCLASSIFIED)) {

            return "Roster shift added";
        }
        return "Access denied";
    }
}
