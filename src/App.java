import java.sql.*;
import java.util.*;

import com.mysql.cj.xdevapi.Result;

public class App {

    static String[] a = { "sleeperwithac", "seaterwithac", "sleeperwithoutac", "seaterwithoutac" };
    static String[] formalBusName = { "Sleeper-AC", "Seater-AC", "Sleeper-NonAC", "Seate-NonAC" };
    static int[] fare = { 700, 550, 600, 450 };
    static int[] cancellationfee = { 50, 50, 25, 25 };

    public static Connection con;
    public static PreparedStatement st;
    public static PreparedStatement st1;
    public static PreparedStatement st2;
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
        int availseats[] = new int[4];

        for (int i = 0; i < a.length; i++) {
            String availQuery = availquery.replace("?", a[i]);
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
                System.out.println("Enter your " + (i + 1) + " th seat : ");
                int seatx;
                String genderx;
                int agex;
                int chance = 0;
                String namex;
                do {
                    if (chance == 1) {
                        System.out.println(" Invalid Seat ! Last Chance");
                    }
                    System.out.print("Enter seat Number : ");
                    seatx = sc.nextInt();
                    System.out.print("Enter Age : ");
                    agex = sc.nextInt();
                    System.out.print("Enter Gender :  ");
                    sc.nextLine();
                    genderx = sc.nextLine();
                    System.out.print("Enter Name : ");
                    namex = sc.nextLine();
                    chance += 1;
                } while (!isseatValid(seatx, genderx, busChoice - 1) && chance < 2);
                seats[i] = seatx;
                age[i] = agex;
                gender[i] = genderx;
                name[i] = namex;
                seatset.add(seats[i]);
                allSeatsAvail = allSeatsAvail && (isseatValid(seats[i], gender[i], busChoice - 1));
                System.out.println("--------------------------------------");
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
                        st.setDouble(1, wal + seats.length * fare[busChoice - 1]);
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

    public static Boolean isseatValid(int seat, String gender, int bus) throws Exception {
        Boolean isNeighAvail = true;
        Boolean isAvail = true;
        Boolean isGender = gender.equalsIgnoreCase("m") || gender.equalsIgnoreCase("f");
        String Query = "select * from " + a[bus] + " ;";
        st = con.prepareStatement(Query);
        ResultSet res = st.executeQuery();
        if (bus % 2 == 0) {
            res.next();
            for (int i = 1; i <= 12; i++) {
                if (seat == res.getInt(1)) {
                    isAvail = res.getInt(6) == 0 ? false : true;
                }
                if (seat % 6 != 0 && seat % 6 != 5 && res.getInt(1) % 6 != 0 && res.getInt(1) % 6 != 5) {
                    if (res.getString(2) != null) {
                        if (!gender.equalsIgnoreCase(res.getString(2))) {
                            if (seat - 2 == res.getInt(1) || seat + 2 == res.getInt(1)) {
                                isNeighAvail = false;
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 1; i <= 12; i++) {
                res.next();
                if (seat == res.getInt(1)) {
                    isAvail = res.getInt(6) == 0 ? false : true;
                }
                if (seat <= 6 && res.getInt(1) <= 6 || res.getInt(1) > 6 && seat > 6) {
                    if (res.getString(2) != null) {
                        if (!gender.equalsIgnoreCase(res.getString(2))) {
                            if (seat - 3 == res.getInt(1) || seat + 3 == res.getInt(1)) {
                                isNeighAvail = false;
                            }
                        }
                    }
                }
            }
        }
        return isNeighAvail && isAvail && isGender;
    }

    public static Boolean isSeatValid(int seat, String gender, int bus) throws Exception {
        Boolean isSeatAvail = true;
        Boolean istheseatavail = true;
        Boolean isGender = gender.equalsIgnoreCase("m") || gender.equalsIgnoreCase("f");
        String Query = "select * from " + a[bus] + " ;";
        st = con.prepareStatement(Query);
        ResultSet res = st.executeQuery();
        if (bus % 2 == 0) {
            for (int i = 1; i <= 12; i++) {
                res.next();
                if (i == seat) {
                    istheseatavail = res.getInt(6) == 0 ? false : true;
                }
                if (seat == 1 && res.getInt(1) == 3 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 2 && res.getInt(1) == 4 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 4 && res.getInt(1) == 2 && !gender.equals(res.getString(2))
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
                } else if (seat == 4 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 1 && res.getInt(1) == 1 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 11 && res.getInt(1) == 8 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 8 && res.getInt(1) == 11 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 7 && res.getInt(1) == 10 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 10 && res.getInt(1) == 7 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 3 && res.getInt(1) == 6 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 6 && res.getInt(1) == 3 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 9 && res.getInt(1) == 12 && !gender.equals(res.getString(2))
                        && !(res.getString(2) == null)) {
                    isSeatAvail = false;
                } else if (seat == 12 && res.getInt(1) == 9 && !gender.equals(res.getString(2))
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
        System.out.println();
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
        ArrayList<Integer> bookedNoSeats = new ArrayList<>();
        String viewTicketsQuery = "select bus,ticketid,fare from tickettable where bookedby = " + currentUserId + ";";
        String cancelTicketQueryFromTicketTable = "delete from tickettable where ticketid = ?";
        String updateQuery = "UPDATE table SET `gender` = null, `name` = null, `age` = null, `ticketid` = null, `avail` = '1' , `cancelled`=`cancelled`+1 WHERE (`ticketid` = ?);";
        String getWalletQuery = "select wallet from admincredentials;";
        String updateWalletQuery = "UPDATE admincredentials SET `wallet` = ? ;";
        st = con.prepareStatement(viewTicketsQuery);
        ResultSet result = st.executeQuery();
        int count = 1;
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
                int seats = 0;
                while (passengers.next()) {
                    System.out.println("Seat no. : " + passengers.getInt(1) + "-" + "Name : " + passengers.getString(3)
                            + "-" + passengers.getString(2)
                            + "(" + passengers.getInt(4) + ")");
                            seats+=1;
                }
                bookedNoSeats.add(seats);
                System.out.println("Total Fare : " + result.getDouble(3));
                System.out.println("-----------------------------------------------------");
                count += 1;
            } while (result.next());

            System.out.println("Enter Your Choice : (1-" + (count - 1) + ")");
            int cancellationChoice = sc.nextInt();
            System.out.println("1-Cancel Ticket");
            System.out.println("2-Cancel Seats");
            int part = sc.nextInt();
            System.out.println("--------------------------------------------");
            if (part == 1) {
                sc.nextLine();
                System.out.println("Amount Deducted for cancellation of tickets is : "
                        + (bookedBus.get(cancellationChoice - 1) < 2
                                ? fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1) / 2
                                : fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1)  / 4));
                System.out.println("Confirm Cancellation (y/n) ? ");
                String confirmCancel = sc.nextLine();
                if (confirmCancel.equalsIgnoreCase("y")) {
                    st = con.prepareStatement(cancelTicketQueryFromTicketTable);
                    st.setString(1, bookedTickets.get(cancellationChoice - 1));
                    st.executeUpdate();
                    String cancelBus = updateQuery.replace("table", a[bookedBus.get(cancellationChoice - 1)]);
                    st = con.prepareStatement(cancelBus);
                    st.setString(1, bookedTickets.get(cancellationChoice - 1));
                    st.executeUpdate();
                    currentUserBal += bookedBus.get(cancellationChoice - 1) < 2
                    ? fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1) / 2
                    : fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1)  / 4;
                    st = con.prepareStatement("update usercredentials set bal = ? where id = ?");
                    st.setDouble(1, currentUserBal);
                    st.setInt(2, currentUserId);
                    st.executeUpdate();
                    st = con.prepareStatement(getWalletQuery);
                    ResultSet wal = st.executeQuery();
                    wal.next();
                    Double wallet = wal.getDouble(1);
                    wallet -= bookedBus.get(cancellationChoice - 1) < 2
                    ? fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1) / 2
                    : fare[bookedBus.get(cancellationChoice-1)]*bookedNoSeats.get(cancellationChoice-1)  / 4;
                    st = con.prepareStatement(updateWalletQuery);
                    st.setDouble(1, wallet);
                    st.executeUpdate();
                    System.out.println("Tickets cancelled Successfully !!!");
                } else if (confirmCancel.equalsIgnoreCase("n")) {
                    System.out.println("You made a right choice !");
                } else {
                    System.out.println("Enter a valid choice");
                }
            } else if (part == 2) {
                st = con.prepareStatement("select seat from table where ticketid = ?;".replace("table",
                        a[bookedBus.get(cancellationChoice - 1)]));
                st.setString(1, bookedTickets.get(cancellationChoice - 1));
                ResultSet res = st.executeQuery();
                int chno = 0;
                ArrayList<Integer> bookedSeats = new ArrayList<>();
                while (res.next()) {
                    System.out.println((chno + 1) + "-" + res.getInt(1));
                    bookedSeats.add(res.getInt(1));
                    chno += 1;
                }
                System.out.println("Enter number of seats to cancel : ");
                int no = sc.nextInt();
                System.out.println("---------------------------");
                if (chno > no) {
                    System.out.println("Enter the Choice For Seats Number(s) : ");
                    HashSet<Integer> seatSet = new HashSet<>();
                    ArrayList<Integer> seatsToCancel = new ArrayList<>();
                    Boolean isIns = true;
                    for (int i = 0; i < no; i++) {
                        int x = sc.nextInt();
                        if (x <= 0 || x > chno) {
                            isIns = false;
                        }
                        seatSet.add(x);
                        seatsToCancel.add(bookedSeats.get(x - 1));
                    }
                    sc.nextLine();
                    if (seatSet.size() == no && isIns) {

                        System.out.println("Amount Deducted for cancellation of tickets is : "
                                + fare[bookedBus.get(cancellationChoice - 1)] * no
                                        / (bookedBus.get(cancellationChoice - 1) < 2 ? 2 : 4));
                        System.out.println("Seats to be cancelled are : ");
                        for (Integer x : seatsToCancel) {
                            System.out.println(x);
                        }
                        System.out.println("Confirm Cancellation (y/n) ? ");
                        String x = sc.nextLine();
                        if (x.charAt(0) == 'y') {
                            String updBusQuery = "UPDATE table SET `gender` = null, `name` = null, `age` = null, `ticketid` = null, `avail` = '1',`cancelled`=`cancelled`+1 WHERE (`seat` = ?);";
                            String updTicQuery = "UPDATE tickettable SET `seat`=? , `fare`=? WHERE (`ticketid` = ?);";
                            String updBalanceQuery = "UPDATE usercredentials set `bal` = ? WHERE (id=?)";
                            String newUpdBusQuery = updBusQuery.replace("table",
                                    a[bookedBus.get(cancellationChoice - 1)]);
                            for (Integer v : seatsToCancel) {
                                st = con.prepareStatement(newUpdBusQuery);
                                st.setInt(1, v);
                                st.executeUpdate();
                            }
                            st = con.prepareStatement(updTicQuery);
                            st.setString(1, seatsToCancel.toString());
                            st.setDouble(2,
                                    bookedFare.get(cancellationChoice - 1)
                                            - fare[bookedBus.get(cancellationChoice - 1)] * no
                                                    / (bookedBus.get(cancellationChoice - 1) < 2 ? 2 : 4));
                            st.setString(3, bookedTickets.get(cancellationChoice - 1));
                            st.executeUpdate();
                            System.out.println(
                                    "Tickets cancelled from the ticket : " + bookedTickets.get(cancellationChoice - 1));
                            st = con.prepareStatement(updBalanceQuery);
                            st.setInt(2, currentUserId);
                            st.setDouble(1,
                                    currentUserBal + fare[bookedBus.get(cancellationChoice - 1)] * no
                                            / (bookedBus.get(cancellationChoice - 1) < 2 ? 2 : 4));
                            st.executeUpdate();
                            st = con.prepareStatement(getWalletQuery);
                            ResultSet wal = st.executeQuery();
                            wal.next();
                            Double wallet = wal.getDouble(1);
                            st = con.prepareStatement(updateWalletQuery);
                            st.setDouble(1,wallet -  fare[bookedBus.get(cancellationChoice - 1)] * no
                                    / (bookedBus.get(cancellationChoice - 1) < 2 ? 2 : 4));
                            st.executeUpdate();
                        } else if (x.charAt(0) == 'n') {
                            System.out.println("Tickets Not cancelled");
                        } else {
                            System.out.println("Enter a valid choice \n Your cancellation is not confirmed !");
                        }

                    } else {
                        System.out.println("Dont Enter duplicate choices or invalid choices");
                    }
                } else {
                    System.out.println("Enter valid number of seats to cancel");
                }

            } else {
                System.out.println("Enter a valid cancellation choice");
            }
        } else {
            System.out.println("No tickets to cancel");
        }
    }

    public static void showAvailability() throws Exception {
        int[] availBus = { 0, 0, 0, 0 };
        int[] preferanceBus = { 1, 2, 3, 4 };
        String availquery = "select Count(*) from ? where avail = 1;";
        String[] busName = { "sleeperwithac", "seaterwithac", "sleeperwithoutac", "seaterwithoutac" };
        for (int i = 0; i < a.length; i++) {
            String availQuery = availquery.replace("?", a[i]);
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
        for (int i = 0; i < availBus.length; i++) {
            if (availBus[i] > 0) {
                System.out.println(busName[i] + " - " + availBus[i]);
            }
        }
    }

    public static void displayFare() throws Exception {
        ResultSet res = con.prepareStatement("select wallet from admincredentials;").executeQuery();
        res.next();
        System.out.println("Total fare collected is : " + res.getDouble(1));
    }

    public static void viewAllTickets() throws Exception {
        String displayQuery = "select * from tickettable;";
        ResultSet res = con.prepareStatement(displayQuery).executeQuery();
        if (res.next()) {
            do {
                System.out.println("TicketID : " + res.getString(1));
                System.out.println("Booked By : " + res.getInt(2));
                System.out.println("Bus : " + formalBusName[res.getInt(3)]);
                System.out.println("Seats Booked : " + res.getString(4));
                System.out.println("Fare Collected : " + res.getDouble(5));
                System.out.println("---------------------------------------------------");
            } while (res.next());
        }
    }

    public static void viewSummary() throws Exception {
        String CanceledQuery = "select sum(cancelled) from table where cancelled>0;";
        String BookedQuery = "select count(*) from table where avail = 0;";
        String fareQuery = "select sum(fare) from tickettable where bus = ?;";
        for (int i = 0; i < a.length; i++) {
            String newCancel = CanceledQuery.replace("table", a[i]);
            String newBook = CanceledQuery.replace("table", a[i]);
            st = con.prepareStatement(newCancel);
            st1 = con.prepareStatement(newBook);
            st2 = con.prepareStatement(fareQuery);
            st2.setInt(1, i);
            ResultSet res2 = st2.executeQuery();
            ResultSet res = st.executeQuery();
            ResultSet res1 = st1.executeQuery();
            res.next();
            res1.next();
            res2.next();
            System.out.println(formalBusName[i] + "-->" + res1.getInt(1) + "Booked + " + res.getInt(1) + " cancelled");
            System.out.println("Fare collected : " + res2.getDouble(1));
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
                            System.out.println("1-View All Tickets");
                            System.out.println("2-View Buses");
                            System.out.println("3-Total Fare Collected");
                            System.out.println("4-Bus Summary");
                            System.out.println("5-Exit");
                            System.out.println("Enter Your Choice : ");
                            int adminChoice = sc.nextInt();
                            sc.nextLine();
                            switch (adminChoice) {
                                case 1:
                                    viewAllTickets();
                                    break;
                                case 2:
                                    for (int i = 0; i < 4; i++) {
                                        System.out.println(formalBusName[i]);
                                        printSeats(i);
                                    }
                                    break;
                                case 3:
                                    displayFare();
                                    break;
                                case 4:
                                    viewSummary();
                                    break;
                                case 5:
                                    adminExit = true;
                                    break;
                                default:
                                    System.out.println("Enter a valid option");
                            }
                        }

                    } else {
                        System.out.println("Incorrect Crentials");
                    }
                    break;
                case 3:
                    totalExit = true;
                    break;
                default:
                    System.out.println("Enter valid options");
            }
        }
    }
}