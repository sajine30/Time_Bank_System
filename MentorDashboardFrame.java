import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Vector;
import java.io.FileWriter;
import java.io.IOException;

public class MentorDashboardFrame extends JFrame {
    
    private final String mentorEmail;
    private final String mentorName;
    
    // UI Components for Add Activity
    private JTextField activityNameField, activityTypeField, dateField, hoursField;
    private JLabel pointsLabel;
    
    // UI Components for Redeem Rewards
    private JTable rewardsTable;
    private JComboBox<String> rewardSelectBox;
    private JLabel currentPointsLabel;

    // Constants for aesthetics
    private static final Color HEADER_BG = new Color(52, 152, 219);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);

    public MentorDashboardFrame(String email, String name) {
        this.mentorEmail = email;
        this.mentorName = name;
        
        setTitle("Mentor Dashboard - " + name);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(BOLD_FONT);

        // 1. Add Activity Tab
        tabbedPane.addTab("Add Volunteer Activity", createAddActivityPanel());
        
        // 2. View Leaderboard Tab
        tabbedPane.addTab("View Leaderboard", createLeaderboardPanel());
        
        // 3. Redeem Rewards Tab
        tabbedPane.addTab("Redeem Rewards", createRedeemRewardsPanel());
        
        // 4. Generate Reports Tab
        tabbedPane.addTab("Generate Reports", createReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
        
        // Initial data load for dynamic panels
        loadLeaderboardData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)tabbedPane.getComponentAt(1)).getComponent(0)).getViewport().getView()).getModel());
        updateCurrentPointsLabel();
        loadRewardsData();
    }
    
    // --- Panel Creation Methods ---

    private JPanel createAddActivityPanel() {
        JPanel panel = new newFormPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Log New Volunteer Activity", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(HEADER_BG);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        // Activity Name
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Activity Name:"), gbc);
        gbc.gridx = 1; activityNameField = new JTextField(20); panel.add(activityNameField, gbc);
        
        // Activity Type
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Activity Type:"), gbc);
        gbc.gridx = 1; activityTypeField = new JTextField(20); panel.add(activityTypeField, gbc);
        
        // Date (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; dateField = new JTextField(20); panel.add(dateField, gbc);
        
        // Hours
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Hours Spent:"), gbc);
        gbc.gridx = 1; hoursField = new JTextField(20); panel.add(hoursField, gbc);

        // Calculated Points (Read-only)
        pointsLabel = new JLabel("Points: 0 (Hours x 10)", SwingConstants.CENTER);
        pointsLabel.setFont(BOLD_FONT);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 5; panel.add(pointsLabel, gbc);

        // Calculate and Save Button
        JButton saveButton = new JButton("Save Activity");
        saveButton.setBackground(HEADER_BG);
        saveButton.setForeground(TEXT_COLOR);
        saveButton.setFont(BOLD_FONT);
        saveButton.addActionListener(this::saveActivity);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 6; panel.add(saveButton, gbc);
        
        // Add listener to hours field to auto-calculate points
        hoursField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePoints(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePoints(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePoints(); }
            private void updatePoints() {
                try {
                    int hours = Integer.parseInt(hoursField.getText());
                    pointsLabel.setText("Points: " + (hours * 10) + " (Hours x 10)");
                } catch (NumberFormatException ex) {
                    pointsLabel.setText("Points: 0 (Hours x 10)");
                }
            }
        });

        // Wrap the form panel in a center alignment
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(panel);
        return wrapper;
    }
    
    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Top Mentors Leaderboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] columnNames = {"Rank", "Name", "Total Hours", "Total Points"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable leaderboardTable = new JTable(model);
        leaderboardTable.getTableHeader().setFont(BOLD_FONT);
        panel.add(new JScrollPane(leaderboardTable), BorderLayout.CENTER);
        
        // Sorting and Refresh Panel
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshBtn = new JButton("Refresh Leaderboard");
        refreshBtn.addActionListener(e -> loadLeaderboardData(model));
        sortPanel.add(refreshBtn);
        panel.add(sortPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRedeemRewardsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Redeem Points for Rewards", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        // Center Panel for Points and Redemption Form
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Points Info
        currentPointsLabel = new JLabel("Your Current Points: Calculating...", SwingConstants.CENTER);
        currentPointsLabel.setFont(BOLD_FONT);
        currentPointsLabel.setForeground(HEADER_BG);
        centerPanel.add(currentPointsLabel, BorderLayout.NORTH);
        
        // Redemption Form
        JPanel redemptionForm = new newFormPanel();
        redemptionForm.setLayout(new GridLayout(0, 2, 10, 10));
        
        redemptionForm.add(new JLabel("Select Reward:"));
        rewardSelectBox = new JComboBox<>();
        rewardSelectBox.setFont(BOLD_FONT);
        redemptionForm.add(rewardSelectBox);
        
        JButton redeemButton = new JButton("Redeem Now");
        redeemButton.setBackground(new Color(230, 126, 34)); // Orange
        redeemButton.setForeground(TEXT_COLOR);
        redeemButton.addActionListener(this::redeemReward);
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.add(redeemButton);

        centerPanel.add(redemptionForm, BorderLayout.CENTER);
        centerPanel.add(buttonWrapper, BorderLayout.SOUTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Rewards Table (List of available rewards)
        String[] columnNames = {"ID", "Reward Name", "Points Cost"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        rewardsTable = new JTable(model);
        rewardsTable.getTableHeader().setFont(BOLD_FONT);
        rewardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(rewardsTable), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Generate Volunteer Reports", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        
        JButton monthlyReportBtn = new JButton("Generate Monthly Report (CSV)");
        monthlyReportBtn.addActionListener(e -> generateReport("monthly"));
        
        JButton yearlyReportBtn = new JButton("Generate Yearly Report (CSV)");
        yearlyReportBtn.addActionListener(e -> generateReport("yearly"));

        buttonPanel.add(monthlyReportBtn);
        buttonPanel.add(yearlyReportBtn);
        
        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }
    
    // --- Data/Action Methods ---

    private void saveActivity(ActionEvent e) {
        // Validation and insertion logic
        String name = activityNameField.getText();
        String type = activityTypeField.getText();
        String dateStr = dateField.getText();
        int hours = 0;
        
        try {
            hours = Integer.parseInt(hoursField.getText());
            if (hours <= 0) throw new NumberFormatException();
            // Simple date validation (assumes YYYY-MM-DD format is correct)
            java.sql.Date.valueOf(dateStr); 
        } catch (NumberFormatException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid, positive hours and a date in YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int points = hours * 10;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            String query = "INSERT INTO MentorActivities (mentor_email, activity_name, activity_type, activity_date, hours, points) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, mentorEmail);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setDate(4, java.sql.Date.valueOf(dateStr));
            ps.setInt(5, hours);
            ps.setInt(6, points);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Activity saved successfully! Earned " + points + " points.", "Success", JOptionPane.INFORMATION_MESSAGE);
            activityNameField.setText("");
            activityTypeField.setText("");
            dateField.setText("");
            hoursField.setText("");
            pointsLabel.setText("Points: 0 (Hours x 10)");
            
            // Refresh dependent data
            updateCurrentPointsLabel();
            loadLeaderboardData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)((JTabbedPane)this.getContentPane().getComponent(0)).getComponentAt(1)).getComponent(0)).getViewport().getView()).getModel());


        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }

    private void loadLeaderboardData(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            // SQL to calculate total points and hours for all mentors, sorted by points
            String query = "SELECT m.name, SUM(ma.hours) AS total_hours, SUM(ma.points) AS total_points " +
                           "FROM Mentors m JOIN MentorActivities ma ON m.email = ma.mentor_email " +
                           "GROUP BY m.email, m.name ORDER BY total_points DESC";
            
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rank++,
                    rs.getString("name"),
                    rs.getInt("total_hours"),
                    rs.getInt("total_points")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading leaderboard: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }

    private int getCurrentPoints() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int totalPoints = 0;
        int redeemedPoints = 0;

        try {
            conn = DatabaseManager.getConnection();
            
            // 1. Calculate total earned points
            String pointsQuery = "SELECT SUM(points) FROM MentorActivities WHERE mentor_email = ?";
            ps = conn.prepareStatement(pointsQuery);
            ps.setString(1, mentorEmail);
            rs = ps.executeQuery();
            if (rs.next()) {
                totalPoints = rs.getInt(1);
            }
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);

            // 2. Calculate total redeemed points
            String redeemedQuery = "SELECT SUM(points_spent) FROM Redemptions WHERE mentor_email = ?";
            ps = conn.prepareStatement(redeemedQuery);
            ps.setString(1, mentorEmail);
            rs = ps.executeQuery();
            if (rs.next()) {
                redeemedPoints = rs.getInt(1);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error calculating points: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
        
        return totalPoints - redeemedPoints;
    }
    
    private void updateCurrentPointsLabel() {
        int points = getCurrentPoints();
        currentPointsLabel.setText("Your Current Points: " + points);
    }

    private void loadRewardsData() {
        DefaultTableModel model = (DefaultTableModel) rewardsTable.getModel();
        model.setRowCount(0);
        rewardSelectBox.removeAllItems();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            String query = "SELECT reward_id, reward_name, points_cost FROM Rewards ORDER BY points_cost ASC";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("reward_id");
                String name = rs.getString("reward_name");
                int cost = rs.getInt("points_cost");
                
                model.addRow(new Object[]{id, name, cost});
                rewardSelectBox.addItem(name + " (" + cost + " points)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading rewards: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }
    
    private void redeemReward(ActionEvent e) {
        String selectedItem = (String) rewardSelectBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select a reward to redeem.", "Redemption Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Parse reward name and cost from the selected string
        String rewardName = selectedItem.substring(0, selectedItem.indexOf(" ("));
        int cost = Integer.parseInt(selectedItem.substring(selectedItem.indexOf("(") + 1, selectedItem.indexOf(" points)")));
        
        int currentPoints = getCurrentPoints();
        if (currentPoints < cost) {
            JOptionPane.showMessageDialog(this, "Insufficient points! You need " + cost + " points, but only have " + currentPoints + ".", "Redemption Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Find the reward_id
        int rewardId = -1;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getConnection();
            String idQuery = "SELECT reward_id FROM Rewards WHERE reward_name = ?";
            ps = conn.prepareStatement(idQuery);
            ps.setString(1, rewardName);
            rs = ps.executeQuery();
            if (rs.next()) {
                rewardId = rs.getInt("reward_id");
            }
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);

            if (rewardId == -1) {
                JOptionPane.showMessageDialog(this, "Selected reward not found in database.", "Redemption Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert redemption record
            String insertQuery = "INSERT INTO Redemptions (mentor_email, reward_id, points_spent) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(insertQuery);
            ps.setString(1, mentorEmail);
            ps.setInt(2, rewardId);
            ps.setInt(3, cost);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Redemption successful! Spent " + cost + " points for " + rewardName + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh points
            updateCurrentPointsLabel();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during redemption: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }
    
    private void generateReport(String period) {
        // Simple report generation logic for current mentor
        String filename = mentorName.replaceAll(" ", "_") + "_" + period + "_Report.csv";
        String dateFilter = "";
        
        if ("monthly".equals(period)) {
            // Filter for the last 30 days
            dateFilter = "AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        } else if ("yearly".equals(period)) {
            // Filter for the current year
            dateFilter = "AND YEAR(activity_date) = YEAR(CURDATE())";
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            String query = "SELECT activity_date, activity_name, activity_type, hours, points " +
                           "FROM MentorActivities WHERE mentor_email = ? " + dateFilter + " ORDER BY activity_date DESC";
            
            ps = conn.prepareStatement(query);
            ps.setString(1, mentorEmail);
            rs = ps.executeQuery();

            try (FileWriter fw = new FileWriter(filename)) {
                fw.append("Date,Activity Name,Activity Type,Hours,Points\n");
                int totalHours = 0;
                int totalPoints = 0;
                
                while (rs.next()) {
                    fw.append(rs.getString("activity_date")).append(",");
                    fw.append("\"").append(rs.getString("activity_name")).append("\",");
                    fw.append(rs.getString("activity_type")).append(",");
                    fw.append(String.valueOf(rs.getInt("hours"))).append(",");
                    fw.append(String.valueOf(rs.getInt("points"))).append("\n");
                    
                    totalHours += rs.getInt("hours");
                    totalPoints += rs.getInt("points");
                }
                
                fw.append("\nTotal Hours,").append(String.valueOf(totalHours)).append("\n");
                fw.append("Total Points,").append(String.valueOf(totalPoints)).append("\n");
                
                JOptionPane.showMessageDialog(this, period.substring(0, 1).toUpperCase() + period.substring(1) + " Report generated successfully to " + filename, "Report Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing report to file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }
    
    // Custom Panel for better form spacing
    private class newFormPanel extends JPanel {
        newFormPanel() {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
        }
    }
}
