package hotelreservation;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class HotelReservation {
    static final double PER_DAY = 100.0;
    static final double ADULT = 75.0;
    static final double CHILD = 35.0;
    static final double STANDARD = 150.0;
    static final double DELUX = 200.0;
    static final double SUITE = 250.0;

    static final Set<String> HOTELS = new HashSet<>(Arrays.asList("Seasons","Styn","WildTuin"));
    static final String DATA_FILE = "data/bookings.csv";
    static final String SEP = "|";

    static final Scanner KB = new Scanner(System.in);
    static final List<Booking> BOOKINGS = new ArrayList<>();
    static final DecimalFormat MONEY = new DecimalFormat("R #,##0.00");

    public static void main(String[] args) {
        loadBookings();
        run();
        saveBookings();
        System.out.println("Goodbye!");
    }

    static void run() {
        while (true) {
            showMainMenu();
            int opt = getIntSafe("Choose an option: ");
            switch (opt) {
                case 1: makeBooking(); break;
                case 2: manageBooking(); break;
                case 3: listBookings(); break;
                case 4: simulatePayment(); break;
                case 5: saveBookings(); System.out.println("Saved."); break;
                case 6: return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    static void showMainMenu() {
        System.out.println("\n===== Hotel Reservation System =====");
        System.out.println("1) Book a Room");
        System.out.println("2) Manage an Existing Booking");
        System.out.println("3) List All Bookings");
        System.out.println("4) Payment Simulation (mark booking as PAID)");
        System.out.println("5) Save");
        System.out.println("6) Exit");
        System.out.println("====================================");
    }

    static void makeBooking() {
        KB.nextLine();
        String hotel;
        while (true) {
            System.out.print("Enter Hotel (Seasons/Styn/WildTuin): ");
            hotel = KB.nextLine().trim();
            if (HOTELS.contains(cap(hotel))) { hotel = cap(hotel); break; }
            System.out.println("Hotel not found. Try again.");
        }
        System.out.print("Enter Name: ");
        String name = KB.nextLine().trim();
        System.out.print("Enter Surname: ");
        String surname = KB.nextLine().trim();
        int days = getIntSafe("Days: ");
        int adults = getIntSafe("Adults: ");
        int children = getIntSafe("Children: ");
        String roomType = selectRoomType();
        double total = calcTotal(days, adults, children, roomType);
        String id = generateUniqueId(name, surname);
        Booking b = new Booking(id, name, surname, hotel, days, adults, children, roomType, total, false);
        BOOKINGS.add(b);
        saveBookings();
        System.out.println("\nBooking Confirmed!");
        printBooking(b);
    }

    static void manageBooking() {
        KB.nextLine();
        System.out.print("Enter Booking ID: ");
        String id = KB.nextLine().trim();
        Booking b = findById(id);
        if (b == null) { System.out.println("No booking found with ID: " + id); return; }
        System.out.println("Found booking:");
        printBooking(b);
        while (true) {
            System.out.println("\nManage Menu");
            System.out.println("1) Change Days");
            System.out.println("2) Change Adults");
            System.out.println("3) Change Children");
            System.out.println("4) Change Room Type");
            System.out.println("5) Cancel Booking");
            System.out.println("6) Back");
            int ch = getIntSafe("Choose: ");
            switch (ch) {
                case 1: b.days = getIntSafe("New Days: "); recalc(b); break;
                case 2: b.adults = getIntSafe("New Adults: "); recalc(b); break;
                case 3: b.children = getIntSafe("New Children: "); recalc(b); break;
                case 4: b.roomType = selectRoomType(); recalc(b); break;
                case 5: BOOKINGS.remove(b); saveBookings(); System.out.println("Booking cancelled."); return;
                case 6: saveBookings(); return;
                default: System.out.println("Invalid.");
            }
            saveBookings();
            System.out.println("Updated:");
            printBooking(b);
        }
    }

    static void listBookings() {
        if (BOOKINGS.isEmpty()) { System.out.println("No bookings yet."); return; }
        System.out.println("\nAll Bookings:");
        for (Booking b : BOOKINGS) { System.out.println("------------------------------"); printBooking(b);}        
        System.out.println("------------------------------");
    }

    static void simulatePayment() {
        KB.nextLine();
        System.out.print("Enter Booking ID to mark as PAID: ");
        String id = KB.nextLine().trim();
        Booking b = findById(id);
        if (b == null) { System.out.println("ID not found."); return; }
        if (b.paid) { System.out.println("Already marked as PAID."); return; }
        b.paid = true;
        saveBookings();
        System.out.println("Payment recorded. Receipt:");
        printBooking(b);
    }

    static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        String lo = s.toLowerCase();
        return Character.toUpperCase(lo.charAt(0)) + lo.substring(1);
    }

    static String selectRoomType() {
        KB.nextLine();
        String input;
        while (true) {
            System.out.print("Room Type [A] Standard  [B] Delux  [C] Suite: ");
            input = KB.nextLine().trim().toUpperCase();
            if (input.matches("[ABC]")) break;
            System.out.println("Please enter A, B or C.");
        }
        switch (input) {
            case "A": return "Standard";
            case "B": return "Delux";
            default: return "Suite";
        }
    }

    static double calcTotal(int days, int adults, int children, String roomType) {
        double amount = 0.0;
        amount += days * PER_DAY;
        amount += adults * ADULT;
        amount += children * CHILD;
        switch (roomType) {
            case "Standard": amount += STANDARD; break;
            case "Delux": amount += DELUX; break;
            case "Suite": amount += SUITE; break;
        }
        return amount;
    }

    static void recalc(Booking b) { b.total = calcTotal(b.days, b.adults, b.children, b.roomType); }

    static String generateUniqueId(String name, String surname) {
        String n = name.trim().toUpperCase();
        String s = surname.trim().toUpperCase();
        String base = n.substring(0, Math.min(3, n.length())) + s.substring(0, Math.min(3, s.length()));
        String id = base;
        int suffix = 100 + new Random().nextInt(900);
        while (findById(id) != null) { id = base + suffix; suffix = 100 + new Random().nextInt(900); }
        return id;
    }

    static Booking findById(String id) { for (Booking b : BOOKINGS) if (b.id.equalsIgnoreCase(id)) return b; return null; }

    static int getIntSafe(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (KB.hasNextInt()) return KB.nextInt();
            System.out.println("Please enter a valid number.");
            KB.next();
        }
    }

    static void printBooking(Booking b) {
        System.out.println("ID: " + b.id);
        System.out.println("Name: " + b.name + " " + b.surname);
        System.out.println("Hotel: " + b.hotel);
        System.out.println("Room: " + b.roomType);
        System.out.println("Days/Adults/Children: " + b.days + "/" + b.adults + "/" + b.children);
        System.out.println("Total: " + MONEY.format(b.total));
        System.out.println("Paid: " + (b.paid ? "YES" : "NO"));
    }

    static void loadBookings() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 10) continue;
                Booking b = new Booking(
                        p[0], p[1], p[2], p[3],
                        Integer.parseInt(p[4]),
                        Integer.parseInt(p[5]),
                        Integer.parseInt(p[6]),
                        p[7],
                        Double.parseDouble(p[8]),
                        Boolean.parseBoolean(p[9])
                );
                BOOKINGS.add(b);
            }
        } catch (Exception e) { System.out.println("Failed to load bookings: " + e.getMessage()); }
    }

    static void saveBookings() {
        File f = new File(DATA_FILE);
        f.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("id|name|surname|hotel|days|adults|children|roomType|total|paid");
            for (Booking b : BOOKINGS) {
                pw.println(String.join(SEP,
                        safe(b.id), safe(b.name), safe(b.surname), safe(b.hotel),
                        String.valueOf(b.days), String.valueOf(b.adults), String.valueOf(b.children),
                        b.roomType, String.valueOf(b.total), String.valueOf(b.paid)));
            }
        } catch (IOException e) { System.out.println("Failed to save bookings: " + e.getMessage()); }
    }

    static String safe(String s) { return s == null ? "" : s.replace("|", "/"); }
}
