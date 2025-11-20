import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Main application class handling the initial user type selection and login.
 * It also defines the global styling constants.
 */
public class TimeBankApp extends JFrame {

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219); // Blue
    private static final Color ACCENT_COLOR = new Color(46, 204, 113); // Green

    private JRadioButton studentRadio, mentorRadio;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public TimeBankApp() {
        setTitle("TimeBank System - Login");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        
        // Main panel with padding and background color
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Title Panel
        JLabel titleLabel = new JLabel("TimeBank Login", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBackground(Color.WHITE);

        // User Type Selection
        studentRadio = new JRadioButton("Student");
        mentorRadio = new JRadioButton("Mentor");
        ButtonGroup userTypeGroup = new ButtonGroup();
        userTypeGroup.add(studentRadio);
        userTypeGroup.add(mentorRadio);
        studentRadio.setSelected(true); // Default selection
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        radioPanel.add(studentRadio);
        radioPanel.add(mentorRadio);
        radioPanel.setBackground(Color.WHITE);
        formPanel.add(radioPanel);

        // Email field
        formPanel.add(new JLabel("Email:", JLabel.LEFT)).setFont(LABEL_FONT);
        emailField = new JTextField(20);
        formPanel.add(emailField);

        // Password field
        formPanel.add(new JLabel("Password:", JLabel.LEFT)).setFont(LABEL_FONT);
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setBackground(Color.WHITE);

        loginButton = new JButton("Login");
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(LABEL_FONT);
        loginButton.addActionListener(this::handleLogin);
        
        registerButton = new JButton("Register");
        registerButton.setBackground(ACCENT_COLOR);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(LABEL_FONT);
        registerButton.addActionListener(this::handleRegistration);
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Handles the login button action.
     */
    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        boolean isStudent = studentRadio.isSelected();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String tableName = isStudent ? "Students" : "Mentors";

        try {
            conn = DatabaseManager.getConnection();
            String query = "SELECT * FROM " + tableName + " WHERE email = ? AND password = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, password); // Note: Simple password check, use secure hashing in production!
            
            rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + rs.getString("name") + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose(); // Close login window

                if (isStudent) {
                    new StudentDashboardFrame(email, rs.getString("name"));
                } else {
                    new MentorDashboardFrame(email, rs.getString("name"));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or user type.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during login: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DatabaseManager.close(rs);
            DatabaseManager.close(ps);
            DatabaseManager.close(conn);
        }
    }

    /**
     * Handles the registration button action.
     */
    private void handleRegistration(ActionEvent e) {
        this.dispose(); // Close login window
        if (studentRadio.isSelected()) {
            new StudentRegistrationFrame();
        } else {
            new MentorRegistrationFrame();
        }
    }
    
    // Main method
    public static void main(String[] args) {
        // Use the event-dispatching thread for GUI operations
        SwingUtilities.invokeLater(TimeBankApp::new);
    }

    // --- Helper classes defined here to simplify project structure ---

    /**
     * Student Registration Form
     */
    private class StudentRegistrationFrame extends JFrame {
        private JTextField nameField, emailField, deptField, yearField, contactField;
        private JPasswordField passwordField;

        public StudentRegistrationFrame() {
            setTitle("Student Registration");
            setSize(450, 450);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            panel.add(new JLabel("Name:")); nameField = new JTextField(20); panel.add(nameField);
            panel.add(new JLabel("Email:")); emailField = new JTextField(20); panel.add(emailField);
            panel.add(new JLabel("Password:")); passwordField = new JPasswordField(20); panel.add(passwordField);
            panel.add(new JLabel("Department:")); deptField = new JTextField(20); panel.add(deptField);
            panel.add(new JLabel("Year (e.g., 2025):")); yearField = new JTextField(20); panel.add(yearField);
            panel.add(new JLabel("Contact No:")); contactField = new JTextField(20); panel.add(contactField);

            JButton registerBtn = new JButton("Register Student");
            registerBtn.setBackground(ACCENT_COLOR);
            registerBtn.setForeground(Color.WHITE);
            registerBtn.addActionListener(this::registerStudent);
            
            JButton backBtn = new JButton("Back to Login");
            backBtn.addActionListener(e -> { this.dispose(); new TimeBankApp(); });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(backBtn);
            buttonPanel.add(registerBtn);
            
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            setVisible(true);
        }

        private void registerStudent(ActionEvent e) {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String department = deptField.getText();
            String year = yearField.getText();
            String contact = contactField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Email, and Password are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DatabaseManager.getConnection();
                String query = "INSERT INTO Students (name, email, password, department, year, contact_no) VALUES (?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, department);
                ps.setString(5, year);
                ps.setString(6, contact);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                this.dispose(); // Close registration and go back to login
                new TimeBankApp();

            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                    JOptionPane.showMessageDialog(this, "Email already registered.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            } finally {
                DatabaseManager.close(ps);
                DatabaseManager.close(conn);
            }
        }
    }
    
    /**
     * Mentor Registration Form
     */
    private class MentorRegistrationFrame extends JFrame {
        private JTextField nameField, emailField, skillsField, availabilityField, contactField;
        private JPasswordField passwordField;

        public MentorRegistrationFrame() {
            setTitle("Mentor Registration");
            setSize(450, 450);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            panel.add(new JLabel("Name:")); nameField = new JTextField(20); panel.add(nameField);
            panel.add(new JLabel("Email:")); emailField = new JTextField(20); panel.add(emailField);
            panel.add(new JLabel("Password:")); passwordField = new JPasswordField(20); panel.add(passwordField);
            panel.add(new JLabel("Skills (comma-separated):")); skillsField = new JTextField(20); panel.add(skillsField);
            panel.add(new JLabel("Availability:")); availabilityField = new JTextField(20); panel.add(availabilityField);
            panel.add(new JLabel("Contact No:")); contactField = new JTextField(20); panel.add(contactField);

            JButton registerBtn = new JButton("Register Mentor");
            registerBtn.setBackground(ACCENT_COLOR);
            registerBtn.setForeground(Color.WHITE);
            registerBtn.addActionListener(this::registerMentor);
            
            JButton backBtn = new JButton("Back to Login");
            backBtn.addActionListener(e -> { this.dispose(); new TimeBankApp(); });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(backBtn);
            buttonPanel.add(registerBtn);
            
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            setVisible(true);
        }

        private void registerMentor(ActionEvent e) {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String skills = skillsField.getText();
            String availability = availabilityField.getText();
            String contact = contactField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Email, and Password are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = DatabaseManager.getConnection();
                String query = "INSERT INTO Mentors (name, email, password, skills, availability, contact_no) VALUES (?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, skills);
                ps.setString(5, availability);
                ps.setString(6, contact);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Mentor Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                this.dispose(); // Close registration and go back to login
                new TimeBankApp();

            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                    JOptionPane.showMessageDialog(this, "Email already registered.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            } finally {
                DatabaseManager.close(ps);
                DatabaseManager.close(conn);
            }
        }
    }
}
