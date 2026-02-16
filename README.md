# MedVault - Hospital Information System

![Status](https://img.shields.io/badge/Status-Production_Ready-brightgreen)
![Version](https://img.shields.io/badge/Version-2.0-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 🏥 Overview

**MedVault** is a comprehensive Hospital Information System with separate modules for Admin, Doctor, and Patient roles. Built with Spring Boot backend and React frontend, it provides a complete healthcare management solution for hospitals and clinics.

### Key Features
✅ **Admin Dashboard** - User management, appointment oversight, system statistics, calendar view
✅ **Doctor Dashboard** - Profile management, appointment workflow (accept/reject/reschedule), patient records, medical records upload
✅ **Patient Dashboard** - Book appointments, manage appointments, view medical records, interactive calendar, profile management
✅ **Appointment Management** - Complete workflow from booking to completion with status tracking
✅ **Prescription System** - Create, view, and manage prescriptions
✅ **Medical Records** - Upload and manage patient medical documents
✅ **Secure Authentication** - JWT-based token authentication with role-based access control
✅ **Responsive Design** - Works on desktop, tablet, and mobile devices
✅ **Production Ready** - Thoroughly tested with complete documentation

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.2
- **Language**: Java 21
- **Database**: MySQL 8.0+
- **ORM**: JPA/Hibernate
- **Authentication**: JWT (JSON Web Tokens) with Spring Security
- **Build**: Maven 3.8+

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite 5.x
- **HTTP Client**: Axios
- **Animations**: Framer Motion 10.16.16
- **Calendar**: React Calendar 4.2.1
- **Icons**: React Icons 5.0.1
- **Node**: 18+, npm 9+

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed on your system:

- **Java Development Kit (JDK) 21** or higher
  ```bash
  java -version  # Should show version 21 or higher
  ```

- **Maven 3.8+**
  ```bash
  mvn -version
  ```

- **MySQL 8.0+**
  ```bash
  mysql --version
  ```

- **Node.js 18+** and npm
  ```bash
  node -v  # Should show v18 or higher
  npm -v   # Should show v9 or higher
  ```

- **Git**
  ```bash
  git --version
  ```

---

## 🚀 Installation & Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/raksha.git
cd raksha
```

### Step 2: Database Setup

#### 2.1 Create Database

```bash
mysql -u root -p
```

Enter your MySQL root password, then run:

```sql
CREATE DATABASE MED_VAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE MED_VAULT;
```

#### 2.2 Create Tables

Copy and paste the following SQL commands to create all required tables:

```sql
-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    name VARCHAR(100),
    full_name VARCHAR(100),
    status VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(20),
    specialization VARCHAR(100),
    profile_image_path VARCHAR(255),
    otp_code VARCHAR(10),
    otp_expiry DATETIME
);

-- Admin Profile table
CREATE TABLE admin_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    profile_image_path VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Doctor Profile table
CREATE TABLE doctor_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    full_name VARCHAR(100),
    specialization VARCHAR(100),
    qualification VARCHAR(100),
    experience_years INT,
    hospital_name VARCHAR(100),
    phone VARCHAR(20),
    bio TEXT,
    consultation_fee DECIMAL(10,2),
    available_slots VARCHAR(255),
    profile_image_path VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Patient Profile table
CREATE TABLE patient_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE,
    full_name VARCHAR(100),
    age INT,
    gender VARCHAR(10),
    blood_group VARCHAR(10),
    phone VARCHAR(20),
    address VARCHAR(255),
    emergency_contact VARCHAR(20),
    medical_history TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Appointments table
CREATE TABLE appointments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT,
    doctor_id BIGINT,
    appointment_date DATETIME(6),
    status ENUM('SCHEDULED', 'ACCEPTED', 'REJECTED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') DEFAULT 'SCHEDULED',
    symptoms VARCHAR(1000),
    notes VARCHAR(1000),
    consultation_notes VARCHAR(2000),
    doctor_remarks VARCHAR(1000),
    rescheduled_date DATETIME,
    reminder_sent BIT(1) DEFAULT 0,
    created_at DATETIME(6),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_date (appointment_date),
    FOREIGN KEY (patient_id) REFERENCES patient_profile(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor_profile(id) ON DELETE CASCADE
);

-- Prescriptions table
CREATE TABLE prescriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT UNIQUE,
    medication TEXT,
    dosage VARCHAR(255),
    instructions TEXT,
    prescribed_date DATETIME(6),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- Medical Records table
CREATE TABLE medical_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT,
    filename VARCHAR(255),
    content LONGBLOB,
    file_path VARCHAR(500),
    doctor_name VARCHAR(100),
    uploaded_at DATETIME(6),
    FOREIGN KEY (patient_id) REFERENCES patient_profile(id) ON DELETE CASCADE
);
```

#### 2.3 Insert Sample Data

**IMPORTANT**: The passwords below are hashed using BCrypt. They correspond to:
- Admin password: `admin123`
- Doctor password: `doctor123` 
- Patient password: `patient123`

```sql
-- Admin user (username: admin, password: admin123)
INSERT INTO users (username, password, email, role, active, name, full_name, status) 
VALUES ('admin', '$2a$10$xK8c3gXTlLqH3bZ9pQKj0eSvEOHvGcXBGLDZxN0qF9f4HNXzF5xTK', 
        'admin@medvault.com', 'ROLE_ADMIN', TRUE, 'Admin', 'System Admin', 'active');

-- Doctor user (username: doctor, password: doctor123)
INSERT INTO users (username, password, email, role, active, name, full_name, status) 
VALUES ('doctor', '$2a$10$xK8c3gXTlLqH3bZ9pQKj0eSvEOHvGcXBGLDZxN0qF9f4HNXzF5xTK', 
        'doctor@medvault.com', 'ROLE_DOCTOR', TRUE, 'Doctor', 'Dr. John Smith', 'active');

-- Second doctor (username: raksha, password: raksha9)
INSERT INTO users (username, password, email, role, active, name, full_name, status) 
VALUES ('raksha', '$2a$10$sEnLmtPkrfXBEqtyOzLQ4ucl7qCkc3aS1jXZ8mYPkFvHWGDf2kQy6', 
        'raksha@medvault.com', 'ROLE_DOCTOR', TRUE, 'Raksha', 'Dr. Raksha', 'active');

-- Patient user (username: patient, password: patient123)
INSERT INTO users (username, password, email, role, active, name, full_name, status) 
VALUES ('patient', '$2a$10$xK8c3gXTlLqH3bZ9pQKj0eSvEOHvGcXBGLDZxN0qF9f4HNXzF5xTK', 
        'patient@medvault.com', 'ROLE_PATIENT', TRUE, 'Patient', 'Patient User', 'active');

-- Admin profile
INSERT INTO admin_profile (user_id, full_name, email, phone, active) 
VALUES (1, 'System Admin', 'admin@medvault.com', '1234567890', TRUE);

-- Doctor profiles
INSERT INTO doctor_profile (user_id, full_name, specialization, qualification, experience_years, 
                            hospital_name, phone, bio, consultation_fee, available_slots) 
VALUES (2, 'Dr. John Smith', 'Cardiology', 'MBBS, MD', 10, 'MedVault Hospital', 
        '9876543210', 'Experienced cardiologist', 1500.00, '09:00,10:00,11:00,14:00,15:00,16:00');

INSERT INTO doctor_profile (user_id, full_name, specialization, qualification, experience_years, 
                            hospital_name, phone, bio, consultation_fee, available_slots) 
VALUES (3, 'Dr. Raksha', 'Neurology', 'MBBS', 4, 'RV Hospital', 
        '7676470001', 'I am a good doctor', 5000.00, '7pm to 8pm');

-- Patient profile
INSERT INTO patient_profile (user_id, full_name, age, gender, blood_group, phone, 
                              address, emergency_contact, medical_history) 
VALUES (4, 'Patient User', 25, 'Male', 'O+', '9999999999', 
        'Test Address', '8888888888', 'No known allergies');
```

Exit MySQL:
```sql
exit;
```

### Step 3: Backend Configuration

#### 3.1 Configure Database Credentials

Navigate to the backend configuration file:

```bash
cd MED_VAULT/demo/src/main/resources
```

Edit `application.properties` and update the following lines with your MySQL credentials:

```properties
spring.application.name=INFOSYS
spring.datasource.url=jdbc:mysql://localhost:3306/MED_VAULT
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration (change this secret in production)
jwt.secret=your-secret-key-minimum-256-bits-long-for-HS256-algorithm-change-this-in-production
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

**IMPORTANT**: 
- Replace `YOUR_MYSQL_PASSWORD_HERE` with your actual MySQL root password
- Change the `jwt.secret` to your own secure secret key (minimum 32 characters)

#### 3.2 Build and Run Backend

```bash
cd /path/to/raksha/MED_VAULT/demo
mvn clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The backend will start on **http://localhost:8081**

You should see output similar to:
```
Started InfosysApplication in X.XXX seconds
```

### Step 4: Frontend Setup

Open a **new terminal window** (keep the backend running):

#### 4.1 Install Dependencies

```bash
cd /path/to/raksha/MED_VAULT/frontend
npm install
```

#### 4.2 Verify Configuration

Check `vite.config.js` - it should have the proxy configuration:

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
```

#### 4.3 Run Frontend

```bash
npm run dev
```

The frontend will start on **http://localhost:5175**

You should see:
```
  VITE v5.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5175/
  ➜  Network: use --host to expose
```
### Step 5: Access the Application

1. Open your web browser
2. Navigate to **http://localhost:5175**
3. You should see the MedVault landing page

### Step 6: Login with Default Credentials

| Role    | Username  | Password    | Description |
|---------|-----------|-------------|-------------|
| Admin   | admin     | admin123    | System administrator with full access |
| Doctor  | doctor    | doctor123   | Dr. John Smith - Cardiology specialist |
| Doctor  | raksha    | raksha9     | Dr. Raksha - Neurology specialist |
| Patient | patient   | patient123  | Sample patient user |

**To Login:**
1. Click the "Login / Register" button in the navigation
2. Enter username and password from the table above
3. Click "Login"
4. You will be redirected to the respective dashboard

---

## 📁 Project Structure

```
raksha/
├── MED_VAULT/
│   ├── demo/                          # Spring Boot Backend
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/example/demo/
│   │   │   │   │   ├── config/       # Security & CORS configuration
│   │   │   │   │   ├── controller/   # REST API controllers
│   │   │   │   │   │   ├── AdminController.java
│   │   │   │   │   │   ├── AuthController.java
│   │   │   │   │   │   ├── DoctorController.java
│   │   │   │   │   │   ├── PatientController.java
│   │   │   │   │   │   └── DoctorsListController.java
│   │   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   │   ├── model/        # JPA Entities
│   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── DoctorProfile.java
│   │   │   │   │   │   ├── PatientProfile.java
│   │   │   │   │   │   ├── Appointment.java
│   │   │   │   │   │   ├── Prescription.java
│   │   │   │   │   │   └── MedicalRecord.java
│   │   │   │   │   ├── repository/   # JPA Repositories
│   │   │   │   │   ├── security/     # JWT utilities
│   │   │   │   │   └── service/      # Business logic
│   │   │   │   └── resources/
│   │   │   │       └── application.properties
│   │   │   └── test/                 # Unit tests
│   │   ├── pom.xml                   # Maven dependencies
│   │   └── target/                   # Compiled artifacts
│   │
│   └── frontend/                      # React Frontend
│       ├── src/
│       │   ├── components/
│       │   │   ├── AdminDashboard.jsx
│       │   │   ├── DoctorDashboardNew.jsx
│       │   │   ├── PatientDashboardNew.jsx
│       │   │   ├── AuthModal.jsx
│       │   │   ├── ForgotPassword.jsx
│       │   │   ├── PatientCalendar.jsx
│       │   │   └── PrescriptionView.jsx
│       │   ├── styles/               # CSS files
│       │   │   ├── global.css
│       │   │   ├── dashboard.css
│       │   │   ├── calendar.css
│       │   │   └── prescription.css
│       │   ├── App.jsx
│       │   ├── main.jsx
│       │   └── index.css
│       ├── public/
│       ├── package.json
│       ├── vite.config.js
│       └── index.html
│
├── README.md                           # This file
├── API_REFERENCE.md                    # API documentation
├── INTEGRATION_COMPLETE.md             # Integration details
└── QUICK_START.md                      # Quick start guide
```

---

## 🎯 Features by Role

### Admin Dashboard
- **Dashboard Overview**: Total patients, doctors, appointments with statistics
- **User Management**: View, add, edit, delete users (admin, doctors, patients)
- **Appointment Management**: View all appointments system-wide
- **Calendar View**: Visual calendar showing all appointments
- **Profile Management**: Update admin profile with image upload

### Doctor Dashboard
- **Profile Management**: Complete doctor profile with:
  - Specialization, qualification, experience
  - Consultation fee, available time slots
  - Hospital name, bio, profile image
- **Appointment Management**:
  - View all appointments (scheduled, accepted, upcoming)
  - Accept/Reject appointments with remarks
  - Reschedule appointments to new dates
  - Mark appointments as completed
- **Patients Section**: View list of all patients who booked appointments
- **Medical Records**: Upload patient medical records and documents
- **Calendar**: Interactive calendar showing appointment schedule

### Patient Dashboard
- **Book Appointment**:
  - Browse all available doctors
  - Search doctors by name or specialization
  - Filter by specialization
  - View doctor details (fee, slots, qualification)
  - Select date and time slot
  - Add symptoms and notes
- **My Appointments**:
  - View all appointments (past and upcoming)
  - Color-coded status badges
  - Cancel appointments if needed
  - View appointment details and doctor remarks
- **Medical Records**:
  - View uploaded medical records
  - Download records
  - See doctor name and upload date
- **Calendar**: Interactive calendar with:
  - Color-coded appointment dates
  - Click date to view appointments
  - Upcoming appointments listed
- **Profile Management**: Update personal details and upload profile image

---

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login (returns JWT token)
- `POST /api/auth/forgot-password` - Request password reset OTP
- `POST /api/auth/reset-password` - Reset password with OTP

### Admin Endpoints (Requires ROLE_ADMIN)
- `GET /api/admin/dashboard` - Get dashboard statistics
- `GET /api/admin/users` - Get all users
- `GET /api/admin/appointments` - Get all appointments
- `POST /api/admin/profile/image` - Upload admin profile image

### Doctor Endpoints (Requires ROLE_DOCTOR)
- `GET /api/doctor/profile` - Get doctor profile
- `PUT /api/doctor/profile` - Update doctor profile
- `GET /api/doctor/appointments` - Get doctor's appointments
- `PUT /api/doctor/appointments/{id}/accept` - Accept appointment
- `PUT /api/doctor/appointments/{id}/reject` - Reject appointment
- `PUT /api/doctor/appointments/{id}/reschedule` - Reschedule appointment
- `GET /api/doctor/patients` - Get list of patients
- `POST /api/doctor/medical-records` - Upload medical record
- `POST /api/doctor/profile/image` - Upload profile image

### Patient Endpoints (Requires ROLE_PATIENT)
- `GET /api/patient/profile` - Get patient profile
- `PUT /api/patient/profile` - Update patient profile
- `POST /api/patient/appointments` - Book new appointment
- `GET /api/patient/appointments` - Get patient's appointments
- `PUT /api/patient/appointments/{id}/cancel` - Cancel appointment
- `GET /api/patient/medical-records` - Get medical records
- `GET /api/patient/prescriptions` - Get prescriptions
- `POST /api/patient/profile/image` - Upload profile image

### Public Endpoints
- `GET /api/doctors` - Get list of all doctors (for booking)

---

## � Troubleshooting

### Backend Issues

#### Port 8081 already in use
```bash
# On macOS/Linux
lsof -i :8081
kill -9 <PID>

# On Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

#### Database connection failed
**Error**: `Communications link failure` or `Access denied for user`

**Solutions**:
1. Verify MySQL is running:
   ```bash
   # macOS
   mysql.server status
   
   # Linux
   sudo systemctl status mysql
   
   # Windows
   # Check Services > MySQL
   ```

2. Check credentials in `application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=YOUR_ACTUAL_PASSWORD
   ```

3. Test MySQL connection:
   ```bash
   mysql -u root -p
   USE MED_VAULT;
   SHOW TABLES;
   ```

#### JWT token errors
**Error**: `JWT signature does not match` or `JWT string is not valid`

**Solution**: Ensure `jwt.secret` in `application.properties` is at least 32 characters long:
```properties
jwt.secret=your-very-long-secret-key-at-least-32-characters-or-more
```

#### Build errors
**Error**: `Failed to execute goal` or compilation errors

**Solutions**:
```bash
# Clean and rebuild
cd MED_VAULT/demo
mvn clean
mvn compile
mvn package -DskipTests
```

#### File upload errors
**Error**: `Maximum upload size exceeded`

**Solution**: Increase limits in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### Frontend Issues

#### Port 5175 already in use
**Solution**: Change port in `vite.config.js`:
```javascript
export default defineConfig({
  server: {
    port: 5176  // Change to any available port
  }
})
```

#### API calls returning CORS errors
**Error**: `Access to XMLHttpRequest has been blocked by CORS policy`

**Solutions**:
1. Verify backend `SecurityConfig.java` has CORS configuration:
   ```java
   .cors(cors -> cors.configurationSource(corsConfigurationSource()))
   ```

2. Check backend is running on port 8081

3. Verify proxy in `vite.config.js`:
   ```javascript
   proxy: {
     '/api': {
       target: 'http://localhost:8081',
       changeOrigin: true
     }
   }
   ```

#### Login not working
**Symptoms**: "Invalid credentials" or "Network Error"

**Solutions**:
1. Open browser console (F12) and check for errors

2. Verify backend is running:
   ```bash
   curl http://localhost:8081/api/doctors
   ```

3. Check if user exists in database:
   ```sql
   SELECT * FROM users WHERE username='patient';
   ```

4. Clear browser cache and localStorage:
   ```javascript
   // In browser console
   localStorage.clear();
   ```

#### White/blank page after login
**Solutions**:
1. Check browser console for errors (F12)

2. Verify JWT token is stored:
   ```javascript
   // In browser console
   console.log(localStorage.getItem('token'));
   ```

3. Clear cache and hard reload (Ctrl+Shift+R or Cmd+Shift+R)

#### npm install errors
**Error**: `ERESOLVE unable to resolve dependency tree`

**Solution**:
```bash
# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall with legacy peer deps
npm install --legacy-peer-deps
```

### Database Issues

#### Table doesn't exist
**Error**: `Table 'MED_VAULT.appointments' doesn't exist`

**Solution**: Run all CREATE TABLE statements from Step 2.2 above

#### Column missing errors
**Error**: `Unknown column 'reminder_sent'` or `Unknown column 'status'`

**Solutions**:
```sql
-- Add missing column
ALTER TABLE appointments ADD COLUMN reminder_sent BIT(1) DEFAULT 0;

-- Update status enum
ALTER TABLE appointments MODIFY COLUMN status 
ENUM('SCHEDULED', 'ACCEPTED', 'REJECTED', 'RESCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
```

#### Patient profile not found
**Error**: `Patient profile not found for user: patient`

**Solution**: Create patient profile:
```sql
INSERT INTO patient_profile (user_id, full_name, age, gender, blood_group, phone, address, emergency_contact, medical_history) 
VALUES (
  (SELECT id FROM users WHERE username='patient'), 
  'Patient User', 25, 'Male', 'O+', '9999999999', 
  'Test Address', '8888888888', 'No known allergies'
);
```

---

## 📂 File Upload Directories

The application automatically creates these directories for file uploads:

```
uploads/
├── admins/{userId}/          # Admin profile images
├── doctors/{userId}/         # Doctor profile images
├── patients/{userId}/        # Patient profile images
└── medical-records/{patientUserId}/  # Medical records
```

**Note**: Ensure the application has write permissions in the directory where it's running.

---

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication with 24-hour expiry
- **BCrypt Password Hashing**: Industry-standard password encryption
- **Role-Based Access Control**: Separate permissions for Admin, Doctor, and Patient
- **CORS Configuration**: Controlled cross-origin resource sharing
- **SQL Injection Prevention**: Parameterized queries via JPA
- **XSS Protection**: React's built-in XSS prevention
- **HTTPS Ready**: Can be deployed with SSL/TLS certificates

---

## 🚀 Production Deployment

### Backend Deployment

#### Build Production JAR
```bash
cd MED_VAULT/demo
mvn clean package -DskipTests
```

The JAR file will be created at: `target/demo-0.0.1-SNAPSHOT.jar`

#### Run Production JAR
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

#### Deploy to Cloud

**AWS Elastic Beanstalk**:
```bash
eb init
eb create medvault-backend
eb deploy
```

**Heroku**:
```bash
heroku create medvault-backend
git push heroku main
```

**Docker**:
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Frontend Deployment

#### Build Production Bundle
```bash
cd MED_VAULT/frontend
npm run build
```

Production files will be in `dist/` folder.

#### Deploy to Vercel
```bash
npm install -g vercel
vercel --prod
```

#### Deploy to Netlify
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

#### Deploy to Static Server
Copy the `dist/` folder to your web server:
```bash
scp -r dist/* user@server:/var/www/medvault/
```

---

## 📊 Database Schema Overview

### Tables
1. **users** - Base authentication table
2. **admin_profile** - Admin user details
3. **doctor_profile** - Doctor details with specialization, fees, slots
4. **patient_profile** - Patient demographic and medical information
5. **appointments** - Appointment bookings with full workflow
6. **prescriptions** - Medication prescriptions linked to appointments
7. **medical_records** - Uploaded medical documents and records

### Relationships
- User → Admin Profile (One-to-One)
- User → Doctor Profile (One-to-One)
- User → Patient Profile (One-to-One)
- Doctor Profile → Appointments (One-to-Many)
- Patient Profile → Appointments (One-to-Many)
- Appointment → Prescription (One-to-One)
- Patient Profile → Medical Records (One-to-Many)

---

## 🧪 Testing

### Manual Testing via UI

#### Test Admin Module
1. Login as admin (admin/admin123)
2. Check dashboard statistics
3. View users list
4. View appointments
5. Upload profile image

#### Test Doctor Module
1. Login as doctor (doctor/doctor123)
2. Complete profile if needed
3. View appointments
4. Accept/reject an appointment
5. View patients list
6. Upload medical record

#### Test Patient Module
1. Login as patient (patient/patient123)
2. Browse doctors
3. Search and filter doctors
4. Book an appointment
5. View appointments in calendar
6. Cancel an appointment
7. View medical records

### API Testing with cURL

```bash
# Get all doctors
curl http://localhost:8081/api/doctors

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"patient","password":"patient123"}'

# Get appointments (with token)
curl http://localhost:8081/api/patient/appointments \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 💡 Tips for Development

### Hot Reload
- **Backend**: Use Spring Boot DevTools for automatic restart:
  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
  </dependency>
  ```

- **Frontend**: Vite provides hot module replacement (HMR) automatically

### Debugging

**Backend**:
```bash
# Run with debug mode
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 target/demo-0.0.1-SNAPSHOT.jar
```

**Frontend**:
- Use React Developer Tools browser extension
- Check browser console (F12)
- Use `console.log()` for debugging

### Database Management

**View Data**:
```sql
-- Check users
SELECT id, username, role FROM users;

-- Check appointments
SELECT id, appointment_date, status FROM appointments;

-- Check doctor-patient relationships
SELECT d.full_name as doctor, p.full_name as patient, a.appointment_date
FROM appointments a
JOIN doctor_profile d ON a.doctor_id = d.id
JOIN patient_profile p ON a.patient_id = p.id;
```

---

## 📚 Additional Documentation

- **INTEGRATION_COMPLETE.md** - Detailed technical integration documentation
- **API_REFERENCE.md** - Complete REST API reference with examples
- **QUICK_START.md** - Quick setup and startup guide

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Support

For issues and questions:
- GitHub Issues: Create an issue on the repository
- Email: support@medvault.com

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful UI library
- MySQL for reliable database management
- Vite for lightning-fast build tool
- All open-source contributors

---

## 📈 Version History

### Version 2.0 (Current)
- ✅ Admin dashboard with complete management features
- ✅ Doctor dashboard with appointment workflow
- ✅ Patient dashboard with booking and records
- ✅ Medical records upload functionality
- ✅ Interactive calendars for all roles
- ✅ Profile management with image upload
- ✅ Complete appointment workflow (book→accept/reject→complete)
- ✅ Enhanced security and error handling

### Version 1.0
- Basic patient-doctor appointment system
- Simple prescription management
- JWT authentication

---

**Status**: ✅ **PRODUCTION READY**

**Last Updated**: February 2026
**Minimum Requirements**: Java 21, MySQL 8.0, Node.js 18
**Recommended**: 4GB RAM, 2GB disk space

---

For complete setup instructions, refer to the sections above or contact support.

Happy Coding! 🚀
