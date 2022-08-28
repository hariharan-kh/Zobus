import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class App {

    static String[] a = { "sleeperwithac", "seaterwithac", "sleeperwithoutac", "seaterwithoutac" };
    static int[] fare = { 700, 550, 600, 450 };
    static int[] cancellationfee = { 50, 50, 25, 25 };
    static int[] availBus = { 0, 0, 0, 0 };
    public static Connection con;
    public static PreparedStatement st;
    static Scanner sc = new Scanner(System.in);
    static int currentUserId;
    static double currentUserBal;

    public static int validateUser(String userName, String passWord) throws Exception {
        st = con.prepareStatement("Select * from usercredentials where username = ? and password = ?");
        st.setString(1, userName);
        st.setString(2, passWord);
        ResultSet result = st.executeQuery();
        int id = -1;
        while (result.next()) {
            id = result.getInt(1);
            currentUserBal = result.getDouble(6);
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
        String insertQuery = "INSERT INTO tickettable (`ticketid`, `bookedby`, `bus`, `seat`,`fare`) VALUES (?, ?, ?, ?,?);";
        String updateQuery = "UPDATE table SET `gender` = ?, `name` = ?, `age` = ?, `ticketid` = ?, `avail` = '0' WHERE (`seat` = ?);";
        String updateBalQuery = "UPDATE usercredentials SET `bal` = ? WHERE (`id` = ?);";
        String getWalletQuery = "select wallet from admincredentials;";
        String updateWalletQuery = "UPDATE admincredentials SET `wallet` = ? ;";
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
                System.out.println(isSeatValid(seats[i], gender[i], busChoice - 1));
                allSeatsAvail = allSeatsAvail && (isSeatValid(seats[i], gender[i], busChoice - 1));
                System.out.println(allSeatsAvail);
            }
            if (allSeatsAvail && seats.length == seatset.size()) {
                System.out.println("The seats to be booked are : " + Arrays.toString(seats));
                System.out.println("Total Fair to book is : " + seats.length * fare[busChoice - 1]);
                System.out.println("Confirm Booking ? (y/n)");
                String confirm = sc.nextLine();
                if (confirm.equalsIgnoreCase("y")) {
                    if (seats.length * fare[busChoice - 1] <= currentUserBal) {
                        String ticketId = ticketIdCreator(busChoice - 1, seats);
                        for (int i = 0; i < seats.length; i++) {
                            st = con.prepareStatement(updateQuery.replace("table", a[busChoice - 1]));
                            st.setString(1, gender[i]);
                            st.setString(2, name[i]);
                            st.setInt(3, age[i]);
                            st.setString(4, ticketId);
                            st.setInt(5, seats[i]);
                            st.executeUpdate();
                        }
                        st = con.prepareStatement(insertQuery);
                        st.setString(1, ticketId);
                        st.setInt(2, currentUserId);
                        st.setInt(3, busChoice - 1);
                        st.setString(4, Arrays.toString(seats));
                        st.setDouble(5, seats.length * fare[busChoice - 1]);
                        st.executeUpdate();
                        st = con.prepareStatement(updateBalQuery);
                        currentUserBal = currentUserBal - seats.length * fare[busChoice - 1];
                        st.setInt(2, currentUserId);
                        st.setDouble(1, currentUserBal);
                        st.executeUpdate();
                        st = con.prepareStatement(getWalletQuery);
                        ResultSet walset = st.executeQuery();
                        walset.next();
                        double wal = walset.getDouble(1);
                        st = con.prepareStatement(updateWalletQuery);
                        st.setDouble(1,wal + seats.length * fare[busChoice - 1]);
                        st.executeUpdate();
                        System.out.println("Your tickets are booked");
                    } else {
                        System.out.println("Insufficient Balance");
                    }
                } else if (confirm.equalsIgnoreCase("n")) {
                    System.out.println("Thank you for your time");
                } else {
                    System.out.println("Invalid Operation");
                }
            } else {
                System.out.println("Invalid Seat Selection");
            }
        } else {
            System.out.println("Enter valid number of seats");
        }
    }

    public static Boolean isSeatValid(int seat, String gender, int bus) throws Exception {
        Boolean isSeatAvail = true;
        Boolean istheseatavail = true;
        Boolean isGender = gender == "m" || gender == "f";
        String Query = "select * from " + a[bus] + " ;";
        st = con.prepareStatement(Query);
        ResultSet res = st.executeQuery();
        if (bus % 2 == 0) {
            for (int i = 1; i <= 12; i++) {
                res.next();
                if (i == seat) {
                    istheseatavail = res.getInt(6) == 0 ? false : true;
                }
                if (seat == 4 && res.getInt(1) == 2 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 2 && res.getInt(1) == 4 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 10 && res.getInt(1) == 8 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 8 && res.getInt(1) == 10 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 7 && res.getInt(1) == 9 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 9 && res.getInt(1) == 7 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                }

            }
        } else {

            for (int i = 1; i <= 12; i++) {
                res.next();
                if (i == seat) {
                    istheseatavail = res.getInt(6) == 0 ? false : true;
                }
                if (seat == 5 && res.getInt(1) == 2 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 2 && res.getInt(1) == 5 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 10 && res.getInt(1) == 8 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 8 && res.getInt(1) == 10 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 7 && res.getInt(1) == 9 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 9 && res.getInt(1) == 7 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                }

            }
        }

        return isSeatAvail && istheseatavail && isGender;
    }

    public static String ticketIdCreator(int bus, int[] seats) {
        String seat = "";
        for (int i = 0; i < seats.length; i++) {
            seat = seat + "," + seats[i];
        }
        String Ticket = a[bus] + "-" + currentUserId + "-" + seat;
        return Ticket;
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

    public static int viewBalance(int userID) throws Exception {
        int bal = 0;
        String balQuery = "select bal from usercredentials where id = " + userID + " ;";
        st = con.prepareStatement(balQuery);
        ResultSet res = st.executeQuery();
        while (res.next()) {
            bal = res.getInt(1);
        }
        currentUserBal = bal;
        return bal;
    }

    public static void addBalance() throws Exception {
        String addBalanceQuery = "UPDATE usercredentials SET `bal` = ?WHERE (`id` = ?);";
        System.out.print("Enter amount to add to your wallet : ");
        int amt = sc.nextInt();
        sc.nextLine();
        if (amt > 0) {
            st = con.prepareStatement(addBalanceQuery);
            currentUserBal += amt;
            st.setDouble(1, currentUserBal);
            st.setInt(2, currentUserId);
            st.executeUpdate();
        } else {
            System.out.println("Enter a valid amount");
        }
    }

    public static void viewTickets() throws Exception {
        String viewTicketsQuery = "select bus,ticketid,fare from tickettable where bookedby = " + currentUserId + ";";
        st = con.prepareStatement(viewTicketsQuery);
        ResultSet result = st.executeQuery();
        if (result.next()) {
            do {
                String viewSingleTicketQuery = "select * from table where ticketid = ?;";
                String newViewSingleTicketQuery = viewSingleTicketQuery.replace("table", a[result.getInt(1)]);
                st = con.prepareStatement(newViewSingleTicketQuery);
                st.setString(1, result.getString(2));
                ResultSet passengers = st.executeQuery();
                System.out.println("Ticket ID : " + result.getString(2));
                while (passengers.next()) {
                    System.out.println("Seat no. : " + passengers.getInt(1) + "-" + "Name : " + passengers.getString(3)
                            + "-" + passengers.getString(2)
                            + "(" + passengers.getInt(4) + ")");
                }
                System.out.println("Total Fare : " + result.getDouble(3));
                System.out.println("-----------------------------------------------------");
            } while (result.next());
        } else {
            System.out.println("No tickets to book");
        }
    }

    public static void cancelTickets() throws Exception {
        ArrayList<String> bookedTickets = new ArrayList<>();
        ArrayList<Integer> bookedBus = new ArrayList<>();
        ArrayList<Double> bookedFare = new ArrayList<>();
        String viewTicketsQuery = "select bus,ticketid,fare from tickettable where bookedby = " + currentUserId + ";";
        String cancelTicketQueryFromTicketTable = "delete from tickettable where ticketid = ?";
        String updateQuery = "UPDATE table SET `gender` = null, `name` = null, `age` = null, `ticketid` = null, `avail` = '1' WHERE (`ticketid` = ?);";
        st = con.prepareStatement(viewTicketsQuery);
        ResultSet result = st.executeQuery();
        int count = 1;
        Boolean isAvail = false;
        if (result.next()) {
            do {
                String viewSingleTicketQuery = "select * from table where ticketid = ?;";
                String newViewSingleTicketQuery = viewSingleTicketQuery.replace("table", a[result.getInt(1)]);
                st = con.prepareStatement(newViewSingleTicketQuery);
                st.setString(1, result.getString(2));
                ResultSet passengers = st.executeQuery();
                System.out.print("Choice : " + count + " ");
                System.out.println("Ticket ID : " + result.getString(2));
                bookedTickets.add(result.getString(2));
                bookedBus.add(result.getInt(1));
                bookedFare.add(result.getDouble(3));
                while (passengers.next()) {
                    System.out.println("Seat no. : " + passengers.getInt(1) + "-" + "Name : " + passengers.getString(3)
                            + "-" + passengers.getString(2)
                            + "(" + passengers.getInt(4) + ")");
                }
                System.out.println("Total Fare : " + result.getDouble(3));
                System.out.println("-----------------------------------------------------");
                count += 1;
            } while (result.next());

            System.out.println("Enter Your Choice : (1-" + (count - 1) + ")");
            int cancellationChoice = sc.nextInt();
            sc.nextLine();
            st = con.prepareStatement(cancelTicketQueryFromTicketTable);
            st.setString(1, bookedTickets.get(cancellationChoice - 1));
            st.executeUpdate();
            String cancelBus = updateQuery.replace("table", a[bookedBus.get(cancellationChoice - 1)]);
            st = con.prepareStatement(cancelBus);
            st.setString(1, bookedTickets.get(cancellationChoice - 1));
            st.executeUpdate();
            currentUserBal += (bookedBus.get(cancellationChoice - 1) % 2 == 0)
                    ? bookedFare.get(cancellationChoice - 1) / 2
                    : bookedFare.get(cancellationChoice - 1) / 4;
            st = con.prepareStatement("update usercredentials set bal = ? where id = ?");
            st.setDouble(1, currentUserBal);
            st.setInt(2, currentUserId);
            st.executeUpdate();
        } else {
            System.out.println("No tickets to cancel");
        }
    }

    public static void showAvailability() throws Exception {
        int[] preferanceBus = { 1, 2, 3, 4 };
        String availquery = "select Count(*) from ? where avail = 1;";
        String[] busName = { "sleeperwithac", "seaterwithac", "sleeperwithoutac", "seaterwithoutac" };
        for (int i = 0; i < a.length; i++) {
            String availQuery = availquery.replace("?", a[i]);
            // st.setString(1,i);
            st = con.prepareStatement(availQuery);
            ResultSet res = st.executeQuery();
            res.next();
            availBus[i] = res.getInt(1);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (availBus[i] == availBus[j]) {
                    if (preferanceBus[i] > preferanceBus[j]) {
                        int tempAvail = availBus[i];
                        availBus[i] = availBus[j];
                        availBus[j] = tempAvail;
                        String tempBus = busName[i];
                        busName[i] = busName[j];
                        busName[j] = tempBus;
                        int tempPref = preferanceBus[i];
                        preferanceBus[i] = preferanceBus[j];
                        preferanceBus[j] = tempPref;
                    }
                } else if (availBus[i] < availBus[j]) {
                    int tempAvail = availBus[i];
                    availBus[i] = availBus[j];
                    availBus[j] = tempAvail;
                    String tempBus = busName[i];
                    busName[i] = busName[j];
                    busName[j] = tempBus;
                    int tempPref = preferanceBus[i];
                    preferanceBus[i] = preferanceBus[j];
                    preferanceBus[j] = tempPref;
                }
            }
        }
        for(int i=0;i<availBus.length;i++){
            if(availBus[i]>0){
            System.out.println(busName[i]+" - "+availBus[i]);
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
                                    System.out.println("4-Show Availability");
                                    System.out.println("5-View Balance");
                                    System.out.println("6-Add amount to wallet");
                                    System.out.println("7-LogOut");
                                    System.out.println("Please Enter Your Choice");
                                    System.out.println("-----------------------------");
                                    int userChoice = sc.nextInt();
                                    sc.nextLine();
                                    switch (userChoice) {
                                        case 1:
                                            bookTickets();
                                            break;
                                        case 2:
                                            cancelTickets();
                                            break;
                                        case 3:
                                            viewTickets();
                                            break;
                                        case 4:
                                            showAvailability();
                                            break;
                                        case 5:
                                            System.out.println("Your balance is : " + viewBalance(currentUserId));
                                            break;
                                        case 6:
                                            addBalance();
                                            System.out.println("Your balance is : " + viewBalance(currentUserId));
                                            break;
                                        case 7:
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
                        Boolean adminExit = false;
                        while (!adminExit) {
                            System.out.println("1-");
                        }

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