-- Drop database if it exists and create a new one
DROP DATABASE IF EXISTS TimeBankDB;
CREATE DATABASE TimeBankDB;
USE TimeBankDB;

-- 1. Mentors Table
CREATE TABLE Mentors (
    mentor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    skills VARCHAR(255),
    availability VARCHAR(100),
    contact_no VARCHAR(20)
);

-- 2. Students Table
CREATE TABLE Students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(50),
    year VARCHAR(20),
    contact_no VARCHAR(20)
);

-- 3. MentorActivities Table (Activities added by Mentors)
CREATE TABLE MentorActivities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    mentor_email VARCHAR(100) NOT NULL,
    activity_name VARCHAR(255) NOT NULL,
    activity_type VARCHAR(100),
    activity_date DATE NOT NULL,
    hours INT NOT NULL,
    points INT NOT NULL, -- Calculated as hours * 10
    FOREIGN KEY (mentor_email) REFERENCES Mentors(email)
);

-- 4. StudentsActivity Table (Activities logged by Students)
CREATE TABLE StudentsActivity (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    student_email VARCHAR(100) NOT NULL,
    activity_name VARCHAR(255) NOT NULL,
    activity_type VARCHAR(100),
    log_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., Pending, Completed, Rejected
    certificate_path VARCHAR(255), -- Path to the certificate file (or just YES/NO for simplicity)
    remarks TEXT,
    FOREIGN KEY (student_email) REFERENCES Students(email)
);

-- 5. Rewards Table
CREATE TABLE Rewards (
    reward_id INT AUTO_INCREMENT PRIMARY KEY,
    reward_name VARCHAR(100) NOT NULL,
    points_cost INT NOT NULL
);

-- Insert some sample rewards
INSERT INTO Rewards (reward_name, points_cost) VALUES
('Coffee Coupon', 50),
('E-book Subscription', 100),
('Mentorship Session', 200),
('Certificate of Excellence', 500);

-- 6. Redemptions Table
CREATE TABLE Redemptions (
    redemption_id INT AUTO_INCREMENT PRIMARY KEY,
    mentor_email VARCHAR(100) NOT NULL,
    reward_id INT NOT NULL,
    points_spent INT NOT NULL,
    redemption_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mentor_email) REFERENCES Mentors(email),
    FOREIGN KEY (reward_id) REFERENCES Rewards(reward_id)
);
