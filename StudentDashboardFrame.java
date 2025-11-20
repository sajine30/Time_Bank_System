import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class StudentDashboardFrame extends JFrame {
    
    private final String studentEmail;
    private final String studentName;
    
    // UI Components for Log Activity
    private JTextField activityNameField, activityTypeField, dateField, remarksField;
    private JComboBox<String> statusComboBox;
    private JCheckBox certificateCheckBox;
    
    // Constants for aesthetics
    private static final Color HEADER_BG = new Color(46, 204, 113); // Green
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);

    public StudentDashboardFrame(String email, String name) {
        this.studentEmail = email;
        this.studentName = name;
        
        setTitle("Student Dashboard - " + name);
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(BOLD_FONT);

        // 1. Log Activity Tab
        tabbedPane.addTab("Log Activity", createLogActivityPanel());
        
        // 2. View Performance Tab
        tabbedPane.addTab("View Performance", createPerformancePanel());

        add(tabbedPane, BorderLayout.CENTER);
        setVisible(true);
        
        // Initial data load for performance view
        loadPerformanceData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)tabbedPane.getComponentAt(1)).getComponent(0)).getViewport().getView()).getModel());
    }

    // --- Panel Creation Methods ---

    private JPanel createLogActivityPanel() {
        JPanel panel = new newFormPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Log Completed Student Activity", SwingConstants.CENTER);
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
        
        // Status (Dropdown)
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Status:"), gbc);
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed"});
        gbc.gridx = 1; panel.add(statusComboBox, gbc);
        
        // Certificate (Checkbox)
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Certificate Available:"), gbc);
        certificateCheckBox = new JCheckBox("Yes");
        gbc.gridx = 1; panel.add(certificateCheckBox, gbc);

        // Remarks
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Remarks:"), gbc);
        remarksField = new JTextField(20); 
        gbc.gridx = 1; panel.add(remarksField, gbc);

        // Log Button
        JButton logButton = new JButton("Log Activity");
        logButton.setBackground(HEADER_BG);
        logButton.setForeground(TEXT_COLOR);
        logButton.setFont(BOLD_FONT);
        logButton.addActionListener(this::logStudentActivity);
        gbc.gridwidth = 2;
        gbc.gridx = 0; gbc.gridy = 7; panel.add(logButton, gbc);
        
        // Wrap the form panel in a center alignment
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(panel);
        return wrapper;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Your Activity Log and Performance", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] columnNames = {"Date", "Activity Name", "Type", "Status", "Certificate", "Remarks"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable activityTable = new JTable(model);
        activityTable.getTableHeader().setFont(BOLD_FONT);
        panel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshBtn = new JButton("Refresh Performance Data");
        refreshBtn.addActionListener(e -> loadPerformanceData(model));
        controlPanel.add(refreshBtn);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // --- Data/Action Methods ---

    private void logStudentActivity(ActionEvent e) {
        String name = activityNameField.getText();
        String type = activityTypeField.getText();
        String dateStr = dateField.getText();
        String status = (String) statusComboBox.getSelectedItem();
        String certificate = certificateCheckBox.isSelected() ? "Yes" : "No";
        String remarks = remarksField.getText();

        if (name.isEmpty() || type.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Activity Name, Type, and Date are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            java.sql.Date.valueOf(dateStr); // Check for valid date format
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in YYYY-MM-DD format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseManager.getConnection();
            String query = "INSERT INTO StudentsActivity (student_email, activity_name, activity_type, log_date, status, certificate_path, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, studentEmail);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setDate(4, java.sql.Date.valueOf(dateStr));
            ps.setString(5, status);
            ps.setString(6, certificate); // Storing Yes/No for simplicity
            ps.setString(7, remarks);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Activity logged successfully! Status: " + status, "Success", JOptionPane.INFORMATION_MESSAGE);
            
            activityNameField.setText("");
            activityTypeField.setText("");
            dateField.setText("");
            remarksField.setText("");
            certificateCheckBox.setSelected(false);
            
            // Refresh performance data
            loadPerformanceData((DefaultTableModel)((JTable)((JScrollPane)((JPanel)((JTabbedPane)this.getContentPane().getComponent(0)).getComponentAt(1)).getComponent(0)).getViewport().getView()).getModel());


        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }

    private void loadPerformanceData(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseManager.getConnection();
            String query = "SELECT log_date, activity_name, activity_type, status, certificate_path, remarks " +
                           "FROM StudentsActivity WHERE student_email = ? ORDER BY log_date DESC";
            
            ps = conn.prepareStatement(query);
            ps.setString(1, studentEmail);
            rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDate("log_date").toString(),
                    rs.getString("activity_name"),
                    rs.getString("activity_type"),
                    rs.getString("status"),
                    rs.getString("certificate_path"),
                    rs.getString("remarks")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading performance data: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
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
