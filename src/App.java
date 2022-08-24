import java.sql.*;
import java.util.Scanner;

public class App {

    public static Connection con;
    public static PreparedStatement st;
    static Scanner sc = new Scanner(System.in);
    static int currentUserId;

    public static int validateUser(String userName, String passWord) throws Exception {
        st = con.prepareStatement("Select id from usercredentials where username = ? and password = ?");
        st.setString(1, userName);
        st.setString(2, passWord);
        ResultSet result = st.executeQuery();
        int id = -1;
        while (result.next()) {
            id = result.getInt(1);
        }
        return id;
    }

    public static Boolean validateAdmin() throws Exception {
        sc.nextLine();
        System.out.print("Enter admin username : ");
        String userName = sc.nextLine();
        System.out.print("Enter admin password : ");
        String passWord = sc.nextLine();
        st = con.prepareStatement("Select * from admincredentials where username = ? and password = ?");
        st.setString(1, userName);
        st.setString(2, passWord);
        ResultSet result = st.executeQuery();
        Boolean id = false;
        while (result.next()) {
            id = true;
        }
        return id;
    }

    public static Boolean usernameAvail(String userName) throws Exception {
        st = con.prepareStatement("Select id from usercredentials where username = ?");
        st.setString(1, userName);
        ResultSet result = st.executeQuery();
        Boolean avail = true;
        while (result.next()) {
            avail = false;
        }
        return avail;
    }

    public static void registerUser(String userName, String passWord, int age, String Gender) throws Exception {
        st = con.prepareStatement(
                "INSERT INTO `zobus`.`usercredentials` (`username`, `password`, `age`, `gender`) VALUES (?, ?, ?, ?);");
        st.setString(1, userName);
        st.setString(2, passWord);
        st.setInt(3, age);
        st.setString(4, Gender);
        st.executeUpdate();
    }

    public static void main(String[] args) throws Exception {
        // Class.forName("com.mysql..jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Zobus", "root", "19cs046H");
        Boolean totalExit = false;
        while (!totalExit) {
            System.out.println("1-User");
            System.out.println("2-Admin");
            System.out.println("3-Exit");
            System.out.println("Please Enter Your Choice");
            System.out.println("-----------------------------");
            int totalExitChoice = sc.nextInt();
            switch (totalExitChoice) {
                case 1:
                    Boolean userOnboardExit = false;
                    System.out.println("1-Login");
                    System.out.println("2-Register");
                    System.out.println("3-Exit");
                    System.out.println("Please Enter Your Choice");
                    System.out.println("-----------------------------");
                    int onBoardChoice = sc.nextInt();
                    sc.nextLine();
                    switch (onBoardChoice) {
                        case 1:
                            System.out.print("Enter Username : ");
                            String lun = sc.nextLine();
                            System.out.print("Enter Password : ");
                            String lpw = sc.nextLine();
                            currentUserId = validateUser(lun, lpw);
                            if (currentUserId != -1) {
                                System.out.println("Login Successful");
                            } else {
                                System.out.println("Login Unsuccessful");
                            }
                            break;
                        case 2:
                            System.out.print("Enter Username : ");
                            String run = sc.nextLine();
                            System.out.print("Enter Password : ");
                            String rpw = sc.nextLine();
                            System.out.print("Enter Age : ");
                            int rage = sc.nextInt();
                            sc.nextLine();
                            System.out.print("Enter Gender : (m/f) ");
                            String gen = sc.nextLine();
                            if (rage > 0) {
                                if (usernameAvail(run)) {
                                    if ((Character.toLowerCase(gen.charAt(0)) == 'm'
                                            || Character.toLowerCase(gen.charAt(0)) == 'f') && (gen.length() == 1)) {
                                        registerUser(run, rpw, rage, Character.toLowerCase(gen.charAt(0)) + "");
                                        System.out.println("Registration Successfull");
                                    } else {
                                        System.out.println("Enter a valid Gender");
                                    }
                                } else {
                                    System.out.println("Username not available");
                                }

                            } else {
                                System.out.println("Enter a valid Age");
                            }
                            break;
                        case 3:
                            userOnboardExit = true;
                            break;
                        default:
                            System.out.println("Enter a valid Choice");
                    }
                    break;
                case 2:
                    if (validateAdmin()) {
                        System.out.println("Admin Login Successful");
                        
                    }else{
                        System.out.println("Incorrect Crentials");
                    }
                    break;
                case 3:
                    totalExit = true;
            }
        }
    }
}
