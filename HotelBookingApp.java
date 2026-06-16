
/*
 Royal Palace Hotel Booking System
 Features:
 - Modern UI
 - Live Room Availability
 - Check-In / Check-Out Dates
 - Booking Timestamp
 - Booking History
 - Bill Calculation
*/

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.formdev.flatlaf.FlatDarkLaf;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class HotelBookingApp {

    static int luxuryRooms = 3;
    static int deluxeRooms = 5;
    static int standardRooms = 8;

    static JLabel luxuryLabel;
    static JLabel deluxeLabel;
    static JLabel standardLabel;

    static final int MAX_LUXURY = 3;
    static final int MAX_DELUXE = 5;
    static final int MAX_STANDARD = 8;

    static void updateAvailability(int lux, int del, int std) {
        luxuryLabel.setText("Luxury Rooms Available : " + lux);
        deluxeLabel.setText("Deluxe Rooms Available : " + del);
        standardLabel.setText("Standard Rooms Available : " + std);
    }

    static void calculateAvailability(String inStr, String outStr) {
        int lux = MAX_LUXURY;
        int del = MAX_DELUXE;
        int std = MAX_STANDARD;

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate inDate = LocalDate.parse(inStr.trim(), fmt);
            LocalDate outDate = LocalDate.parse(outStr.trim(), fmt);
            
            if (!outDate.isAfter(inDate)) {
                updateAvailability(lux, del, std);
                return;
            }

            String inSql = inDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String outSql = outDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT room, COUNT(*) as count FROM bookings WHERE check_in < ? AND check_out > ? GROUP BY room")) {
                pstmt.setString(1, outSql);
                pstmt.setString(2, inSql);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()) {
                    String r = rs.getString("room");
                    int count = rs.getInt("count");
                    if (r.contains("Luxury")) lux = Math.max(0, lux - count);
                    else if (r.contains("Deluxe")) del = Math.max(0, del - count);
                    else if (r.contains("Standard")) std = Math.max(0, std - count);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (DateTimeParseException | NullPointerException e) {
            // Invalid date format, default to max
        }
        
        updateAvailability(lux, del, std);
    }

    // Database connection
    static Connection connectDB() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:hotel_booking.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY, " +
                    "customer TEXT, " +
                    "mobile TEXT, " +
                    "check_in TEXT, " +
                    "check_out TEXT, " +
                    "room TEXT, " +
                    "nights INTEGER, " +
                    "bill INTEGER, " +
                    "timestamp TEXT" +
                    ")");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void loadHistory(JTextArea history) {
        try (Connection conn = connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bookings ORDER BY id DESC")) {
            while (rs.next()) {
                history.append(
                        "\n===================================\n" +
                        "Booking ID : " + rs.getInt("id") + "\n" +
                        "Customer   : " + rs.getString("customer") + "\n" +
                        "Mobile     : " + rs.getString("mobile") + "\n" +
                        "Check-In   : " + rs.getString("check_in") + "\n" +
                        "Check-Out  : " + rs.getString("check_out") + "\n" +
                        "Room       : " + rs.getString("room") + "\n" +
                        "Nights     : " + rs.getInt("nights") + "\n" +
                        "Bill       : ₹" + rs.getInt("bill") + "\n" +
                        "Booked At  : " + rs.getString("timestamp") + "\n" +
                        "===================================\n"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        JFrame frame = new JFrame("Royal Palace Hotel Booking System");
        frame.setSize(900, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(24, 24, 36)); // Darker vibrant background

        JPanel header = new JPanel(null);
        header.setBounds(0,0,900,90);
        header.setBackground(new Color(138, 43, 226)); // Vibrant Purple

        JLabel title = new JLabel("ROYAL PALACE HOTEL", SwingConstants.CENTER);
        title.setBounds(0,15,900,35);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel sub = new JLabel("Luxury • Comfort • Hospitality", SwingConstants.CENTER);
        sub.setBounds(0,50,900,20);
        sub.setForeground(Color.WHITE);

        header.add(title);
        header.add(sub);

        JPanel bookingPanel = new JPanel(null);
        bookingPanel.setBounds(20,110,420,360);
        bookingPanel.setBackground(new Color(35, 35, 50)); // Dark panel
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Guest Information"));

        JLabel nameLabel = new JLabel("Customer Name");
        nameLabel.setBounds(20,30,120,25);

        JTextField nameField = new JTextField();
        nameField.setBounds(160,30,220,25);

        JLabel mobileLabel = new JLabel("Mobile Number");
        mobileLabel.setBounds(20,70,120,25);

        JTextField mobileField = new JTextField();
        mobileField.setBounds(160,70,220,25);

        JLabel inLabel = new JLabel("Check-In Date");
        inLabel.setBounds(20,110,120,25);

        JTextField inField = new JTextField("DD/MM/YYYY");
        inField.setBounds(160,110,220,25);

        JLabel outLabel = new JLabel("Check-Out Date");
        outLabel.setBounds(20,150,120,25);

        JTextField outField = new JTextField("DD/MM/YYYY");
        outField.setBounds(160,150,220,25);

        JLabel roomLabel = new JLabel("Room Type");
        roomLabel.setBounds(20,190,120,25);

        JComboBox<String> roomBox = new JComboBox<>(new String[]{
                "Luxury Suite - ₹5000",
                "Deluxe Room - ₹3000",
                "Standard Room - ₹1500"
        });
        roomBox.setBounds(160,190,220,25);

        JButton bookBtn = new JButton("BOOK NOW");
        bookBtn.setBounds(40,240,140,40); // Shifted up
        bookBtn.setBackground(new Color(255, 20, 147)); // Vibrant Deep Pink
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton resetBtn = new JButton("RESET");
        resetBtn.setBounds(220,240,140,40); // Shifted up
        resetBtn.setBackground(new Color(0, 206, 209)); // Dark Turquoise
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        bookingPanel.add(nameLabel);
        bookingPanel.add(nameField);
        bookingPanel.add(mobileLabel);
        bookingPanel.add(mobileField);
        bookingPanel.add(inLabel);
        bookingPanel.add(inField);
        bookingPanel.add(outLabel);
        bookingPanel.add(outField);
        bookingPanel.add(roomLabel);
        bookingPanel.add(roomBox);
        bookingPanel.add(bookBtn);
        bookingPanel.add(resetBtn);

        JPanel availabilityPanel = new JPanel(new GridLayout(3,1));
        availabilityPanel.setBounds(470,110,390,140);
        availabilityPanel.setBorder(BorderFactory.createTitledBorder("Live Room Availability"));

        luxuryLabel = new JLabel();
        deluxeLabel = new JLabel();
        standardLabel = new JLabel();

        availabilityPanel.add(luxuryLabel);
        availabilityPanel.add(deluxeLabel);
        availabilityPanel.add(standardLabel);
        updateAvailability(MAX_LUXURY, MAX_DELUXE, MAX_STANDARD);

        DocumentListener dateListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { calculateAvailability(inField.getText(), outField.getText()); }
            public void removeUpdate(DocumentEvent e) { calculateAvailability(inField.getText(), outField.getText()); }
            public void changedUpdate(DocumentEvent e) { calculateAvailability(inField.getText(), outField.getText()); }
        };
        inField.getDocument().addDocumentListener(dateListener);
        outField.getDocument().addDocumentListener(dateListener);

        JTextArea history = new JTextArea();
        history.setEditable(false);
        history.setBackground(new Color(30, 30, 40));
        history.setForeground(new Color(200, 255, 200));

        JScrollPane scroll = new JScrollPane(history);
        
        loadHistory(history); // Load history from DB on startup

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBounds(20,490,840,160);
        historyPanel.setBorder(BorderFactory.createTitledBorder("Booking History"));
        historyPanel.add(scroll);

        JLabel prices = new JLabel("<html><b>Room Prices</b><br>Luxury ₹5000<br>Deluxe ₹3000<br>Standard ₹1500</html>");
        prices.setBounds(470,280,180,120);

        JLabel facilities = new JLabel("<html><b>Facilities</b><br>✓ WiFi<br>✓ Pool<br>✓ Gym<br>✓ Restaurant<br>✓ Parking</html>");
        facilities.setBounds(680,280,180,120);

        resetBtn.addActionListener(e -> {
            nameField.setText("");
            mobileField.setText("");
            inField.setText("DD/MM/YYYY");
            outField.setText("DD/MM/YYYY");
            calculateAvailability("", "");
        });

        bookBtn.addActionListener(e -> {

            String customer = nameField.getText().trim();
            String mobile = mobileField.getText().trim();
            String checkIn = inField.getText().trim();
            String checkOut = outField.getText().trim();

            if(customer.isEmpty() || mobile.isEmpty()) {
                JOptionPane.showMessageDialog(frame,"Fill all details");
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate inDate;
            LocalDate outDate;
            try {
                inDate = LocalDate.parse(checkIn, fmt);
                outDate = LocalDate.parse(checkOut, fmt);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(frame,"Enter dates in DD/MM/YYYY format");
                return;
            }

            if (!outDate.isAfter(inDate)) {
                JOptionPane.showMessageDialog(frame,"Check-Out must be after Check-In");
                return;
            }

            long nights = ChronoUnit.DAYS.between(inDate, outDate);

            String room = roomBox.getSelectedItem().toString();
            int maxCapacity = room.contains("Luxury") ? MAX_LUXURY : (room.contains("Deluxe") ? MAX_DELUXE : MAX_STANDARD);
            int price = room.contains("Luxury") ? 5000 : (room.contains("Deluxe") ? 3000 : 1500);

            String inSql = inDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String outSql = outDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Check availability for this specific room
            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM bookings WHERE room = ? AND check_in < ? AND check_out > ?")) {
                pstmt.setString(1, room);
                pstmt.setString(2, outSql);
                pstmt.setString(3, inSql);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) >= maxCapacity) {
                    JOptionPane.showMessageDialog(frame,"Room Not Available for these dates");
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame,"Database Error");
                return;
            }

            int bookingId = (int)(Math.random()*9000)+1000;
            long bill = price * nights;

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            try (Connection conn = connectDB();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO bookings (id, customer, mobile, check_in, check_out, room, nights, bill, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, bookingId);
                pstmt.setString(2, customer);
                pstmt.setString(3, mobile);
                pstmt.setString(4, inSql);
                pstmt.setString(5, outSql);
                pstmt.setString(6, room);
                pstmt.setLong(7, nights);
                pstmt.setLong(8, bill);
                pstmt.setString(9, timestamp);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            history.insert(
                    "\n===================================\n" +
                    "Booking ID : " + bookingId + "\n" +
                    "Customer   : " + customer + "\n" +
                    "Mobile     : " + mobile + "\n" +
                    "Check-In   : " + inSql + "\n" +
                    "Check-Out  : " + outSql + "\n" +
                    "Room       : " + room + "\n" +
                    "Nights     : " + nights + "\n" +
                    "Bill       : ₹" + bill + "\n" +
                    "Booked At  : " + timestamp + "\n" +
                    "===================================\n", 0
            );

            calculateAvailability(checkIn, checkOut);

            JOptionPane.showMessageDialog(frame,
                    "Booking Confirmed\nBooking ID: " + bookingId +
                    "\nBill: ₹" + bill);

        });

        panel.add(header);
        panel.add(bookingPanel);
        panel.add(availabilityPanel);
        panel.add(prices);
        panel.add(facilities);
        panel.add(historyPanel);

        frame.add(panel);
        frame.setVisible(true);
    }
}
