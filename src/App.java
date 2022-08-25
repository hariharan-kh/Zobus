import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class App {

    static String[] a = { "sleeperwithac", "seaterwithac", "sleeperwithoutac", "seaterwithoutac" };
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
        Boolean useravail = true;
        while (result.next()) {
            useravail = false;
        }
        return useravail;
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

    public static void bookTickets() throws Exception {
        String availquery = "select Count(*) from ? where avail = 1;";
        // st = con.prepareStatement("select Count(*) from ? where avail = 1;");
        // st.setString(1, a[0].toString());
        int availseats[] = new int[4];

        for (int i = 0; i < a.length; i++) {
            String availQuery = availquery.replace("?", a[i]);
            // st.setString(1,i);
            st = con.prepareStatement(availQuery);
            ResultSet res = st.executeQuery();
            res.next();
            availseats[i] = res.getInt(1);
            System.out.println((i == 0 ? "AC Sleeper"
                    : i == 1 ? "AC Seater" : i == 2 ? "NonAC Sleeper" : i == 3 ? "NonAC Seater" : "") + " - "
                    + res.getInt(1) + " seat(s) available" + " - " + (i + 1));
        }
        System.out.println("Enter your Choice : ");
        int busChoice = sc.nextInt();
        printSeats(busChoice - 1);
        System.out.println();
        System.out.println("---------------------------------------------");
        System.out.println();
        System.out.println("Enter number of seats to book : ");
        int numberofseats = sc.nextInt();
        if (numberofseats > 0 && numberofseats < availseats[busChoice - 1]) {
            HashSet<Integer> seatset = new HashSet<>();
            int[] seats = new int[numberofseats];
            int[] age = new int[numberofseats];
            String[] gender = new String[numberofseats];
            String[] name = new String[numberofseats];
            Boolean allSeatsAvail = true;
            for (int i = 0; i < numberofseats; i++) {
                System.out.print("Enter seat Number : ");
                seats[i] = sc.nextInt();
                System.out.print("Enter Age : ");
                age[i] = sc.nextInt();
                System.out.print("Enter Gender :  ");
                sc.nextLine();
                gender[i] = sc.nextLine();
                System.out.print("Enter Name : ");
                name[i] = sc.nextLine();
                seatset.add(seats[i]);
                allSeatsAvail = allSeatsAvail && (isSeatValid(seats[i], gender[i], busChoice - 1));
            }
            if(allSeatsAvail && seats.length==seatset.size()){
                System.out.println("Seats Selected are "+Arrays.toString(seats));
            }else{
                System.out.println("Invalid Seat Selection");
            }
        } else {
            System.out.println("Enter valid number of seats");
        }
    }

    public static Boolean isSeatValid(int seat, String gender, int bus) throws Exception {
        Boolean isSeatAvail = true;
        String Query = "select * from " + a[bus] + " ;";
        st = con.prepareStatement(Query);
        ResultSet res = st.executeQuery();
        if (bus % 2 == 0) {
            for (int i = 1; i <= 12; i++) {
                res.next();
                if (i == seat) {
                    isSeatAvail = res.getInt(6) == 0 ? false : true;
                }
                if (seat == 4 && res.getInt(1) == 2 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 2 && res.getInt(1) == 4 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 10 && res.getInt(1) == 8 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 8 && res.getInt(1) == 10 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 7 && res.getInt(1) == 9 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 9 && res.getInt(1) == 7 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                }

            }
        }
        else{
            System.out.println(seat+"-"+gender);
            for (int i = 1; i <= 12; i++) {
                res.next();
                if (i == seat) {
                    isSeatAvail = res.getInt(6) == 0 ? false : true;
                }
                if (seat == 5 && res.getInt(1) == 2 && !gender.equals(res.getString(2)) && (res.getString(2)!=null)) {
                    isSeatAvail = false;
                } else if (seat == 2 && res.getInt(1) == 5 && !gender.equals(res.getString(2)) && (res.getString(2)!=null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 10 && res.getInt(1) == 8 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 8 && res.getInt(1) == 10 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 7 && res.getInt(1) == 9 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                } else if (seat == 9 && res.getInt(1) == 7 && !gender.equals(res.getString(2)) && !(res.getString(2)==null)) {
                    isSeatAvail = false;
                }

            }
        }

        return isSeatAvail;
    }

    public static void printSeats(int bus) throws Exception {
        String printQuery = "Select * from " + a[bus];
        st = con.prepareStatement(printQuery);
        ResultSet res = st.executeQuery();
        String leftAlignFormat = "| %-4d -  %-15s ,  %-4d ( %-3s) |";
        String nullAlignFormat = "| %-4d - %-38s |";
        if (bus % 2 == 0) {
            int i;
            for (i = 0; i < 6; i++) {
                res.next();
                if (i == 0)
                    System.out.println("Lower Deck");
                if (i != 0 && i % 2 == 0) {
                    System.out.println();
                }
                if (i % 4 == 0) {
                    System.out.println();
                }
                if (res.getInt(6) == 0) {
                    System.out.format(leftAlignFormat, res.getInt(1), res.getString(3), res.getInt(4),
                            res.getString(2));
                } else {
                    System.out.format(nullAlignFormat, res.getInt(1), "Available");
                }
            }
            System.out.println();
            System.out.println();
            for (; i < 12; i++) {
                res.next();
                if (i == 6)
                    System.out.println("Upper Deck");
                if (i != 6 && i % 2 == 0) {
                    System.out.println();
                }
                if (i == 10) {
                    System.out.println();
                }
                if (res.getInt(6) == 0) {
                    System.out.format(leftAlignFormat, res.getInt(1), res.getString(3), res.getInt(4),
                            res.getString(2));
                } else {
                    System.out.format(nullAlignFormat, res.getInt(1), "Available");
                }
            }
        } else {
            for (int i = 0; i < 12; i++) {
                res.next();
                if (i != 0 && i % 3 == 0) {
                    System.out.println();
                }
                if (i == 6) {
                    System.out.println();
                }
                if (res.getInt(6) == 0) {
                    System.out.format(leftAlignFormat, res.getInt(1), res.getString(3), res.getInt(4),
                            res.getString(2));
                } else {
                    System.out.format(nullAlignFormat, res.getInt(1), "Available");
                }
            }
        }
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
                                Boolean userChoiceExit = false;
                                while (!userChoiceExit) {
                                    System.out.println("1-Book Tickets");
                                    System.out.println("2-Cancel Tickets");
                                    System.out.println("3-Show Tickets");
                                    System.out.println("4-LogOut");
                                    System.out.println("Please Enter Your Choice");
                                    System.out.println("-----------------------------");
                                    int userChoice = sc.nextInt();
                                    sc.nextLine();
                                    switch (userChoice) {
                                        case 1:
                                            bookTickets();
                                            break;
                                        case 2:
                                            System.out.println("You are in the cancellation page");
                                            break;
                                        case 3:
                                            System.out.println("Your tickets are viewed here ");
                                            break;
                                        case 4:
                                            userChoiceExit = true;
                                            break;
                                        default:
                                            System.out.println("Enter a valid option");
                                    }
                                }
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

                    } else {
                        System.out.println("Incorrect Crentials");
                    }
                    break;
                case 3:
                    totalExit = true;
            }
        }
    }
}