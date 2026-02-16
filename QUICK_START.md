# MedVault Quick Start Guide

## 🚀 System Overview

**MedVault** is a professional Patient-Doctor Appointment and Prescription Management System built with:
- **Backend**: Spring Boot 3.x with JPA/Hibernate (Java)
- **Frontend**: React 18 with Vite (TypeScript/JSX)
- **Database**: MySQL 8.0+
- **Port Configuration**: Backend (8081), Frontend (5173)

---

## 🔧 Prerequisites

Before starting, ensure you have installed:
- **Java 17+** (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **Node.js 18+** (`node -v`)
- **npm 9+** (`npm -v`)
- **MySQL 8.0+** (`mysql --version`)

---

## 📦 Database Setup

### 1. Create Database
```sql
-- Login to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE MED_VAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use database
USE MED_VAULT;

-- Verify
SHOW TABLES;
```

### 2. Verify Connection in application.properties
```properties
# File: demo/src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/MED_VAULT
spring.datasource.username=root
spring.datasource.password=0088  # Change if your MySQL password differs
```

### 3. Start MySQL Service (if not running)
```bash
# macOS with Homebrew
brew services start mysql

# Or verify it's running
mysql.server status
```

---

## 🏗️ Backend Setup & Start

### Step 1: Navigate to Backend Directory
```bash
cd MED_VAULT/demo
```

### Step 2: Clean Build
```bash
mvn clean install
```

### Step 3: Start Spring Boot Server
```bash
mvn spring-boot:run
```

### Expected Output
```
[INFO] ...
[INFO] Tomcat started on port(s): 8081 (http)
[INFO] Started InfosysApplication in 5.234 seconds
```

### ✅ Verification
Backend is ready when you see:
```
Tomcat started on port(s): 8081
```

**Test endpoint**: Open browser and visit:
```
http://localhost:8081/api/doctors
```
Should return JSON array of doctors (200 response)

---

## 💻 Frontend Setup & Start

### Step 1: Navigate to Frontend Directory
```bash
cd MED_VAULT/frontend
```

### Step 2: Install Dependencies (first time only)
```bash
npm install
```

Expected packages installed:
- react 18.2.0
- react-router-dom 6.22.1
- axios 1.6.7
- framer-motion 10.16.16
- react-calendar 4.2.1
- react-icons 5.0.1

### Step 3: Start Development Server
```bash
npm run dev
```

### Expected Output
```
VITE v5.x.x
ready in xxx ms

➜  Local:   http://localhost:5173/
➜  press h + enter to show help
```

### ✅ Verification
Frontend is ready when you see localhost:5173 ready message.

Open browser: `http://localhost:5173`
- Should show MedVault landing page
- **Login** button should be clickable

---

## 🔑 Test Credentials

### Patient Accounts
```
Username: patient1
Password: password123
Role: PATIENT

Username: patient2
Password: password123
Role: PATIENT
```

### Doctor Accounts
```
Username: doctor1
Password: password123
Role: DOCTOR

Username: doctor2
Password: password123
Role: DOCTOR

Username: dr_smith
Password: password123
Role: DOCTOR
```

---

## ✅ Integration Testing Checklist

### 1. Authentication Flow
- [ ] Open http://localhost:5173
- [ ] Click "Login" button
- [ ] Enter doctor1 / password123
- [ ] Click "Sign In"
- [ ] Should redirect to /doctor route

### 2. Doctor Dashboard
- [ ] View "Today's Appointments" card
- [ ] View appointment list in "Overview" tab
- [ ] Click an appointment to select it
- [ ] See "Add Prescription" button appears
- [ ] Click appointment status dropdown (SCHEDULED, COMPLETED, etc.)
- [ ] Update status and see success message

### 3. Prescription Management
- [ ] With appointment selected, click "Add Prescription"
- [ ] Modal form appears with fields:
  - [ ] Diagnosis (textarea)
  - [ ] Medicines (JSON preview)
  - [ ] Tests Recommended (textarea)
  - [ ] Follow-up Date (date picker)
  - [ ] Notes (textarea)
- [ ] Fill out prescription form
- [ ] Click "Save Prescription"
- [ ] See success message

### 4. Patient Dashboard
- [ ] Logout and login as patient1 / password123
- [ ] Dashboard shows:
  - [ ] Upcoming Appointments count
  - [ ] Total Visits count
  - [ ] Follow-ups count
  - [ ] Prescriptions count
- [ ] Click "Appointments" tab
- [ ] View table of all appointments
- [ ] Click appointment row to see details

### 5. Prescription Viewing
- [ ] In Patient Dashboard, go to "Prescriptions" tab
- [ ] Click a prescription card
- [ ] Modal opens with professional prescription layout:
  - [ ] Hospital header with MedVault logo
  - [ ] Patient information section
  - [ ] Doctor information section
  - [ ] Diagnosis clearly visible
  - [ ] Medicines table with columns: Name, Dosage, Frequency, Duration
  - [ ] Tests list
  - [ ] Follow-up date
  - [ ] Signature area
- [ ] Click "Print" button
- [ ] Print preview should look professional (A4 size)

### 6. Calendar View
- [ ] In Patient Dashboard, check if there's a Calendar tab or button
- [ ] Calendar should show:
  - [ ] Month view with navigation
  - [ ] Dots on dates with appointments
  - [ ] Timeline view for selected date
  - [ ] Appointment details below calendar

### 7. Profile Management
- [ ] Both Patient and Doctor dashboards have Profile tab
- [ ] Can edit profile information
- [ ] Click "Save" and see success message
- [ ] Refresh and verify changes persisted

---

## 🐛 Troubleshooting

### Issue: "Cannot GET /api/doctors" (Backend not responding)

**Solution:**
1. Verify MySQL is running
```bash
mysql.server status
```

2. Check if Spring Boot server is running
```bash
# Should see: Tomcat started on port(s): 8081
```

3. Check application.properties database credentials
```properties
spring.datasource.password=0088  # Verify this matches your MySQL password
```

4. Restart Spring Boot:
```bash
# Stop current process (Ctrl+C) and run:
mvn spring-boot:run
```

---

### Issue: "React component not found" or "Module not found" errors

**Solution:**
1. Verify all new components exist:
```bash
ls -la frontend/src/components/
# Should see: PatientDashboardNew.jsx, DoctorDashboardNew.jsx, 
#            PatientCalendar.jsx, PrescriptionView.jsx
```

2. Verify all CSS files exist:
```bash
ls -la frontend/src/styles/
# Should see: global.css, dashboard.css, calendar.css, prescription.css
```

3. Clear npm cache and reinstall:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

---

### Issue: "CORS error" or "Failed to fetch API"

**Solution:**
1. Verify backend CORS is enabled:
   - Check SecurityConfig.java has corsConfigurationSource() bean
   - Should allow: localhost:5173, localhost:3000

2. Verify JWT token is being sent:
   - Open browser DevTools (F12)
   - Go to Network tab
   - Click an API call
   - Check "Authorization" header contains: Bearer [token]

3. Verify token is saved after login:
```javascript
// In browser console:
localStorage.getItem('token')
// Should return a JWT token string
```

---

### Issue: "Database tables not created" or "Hibernate DDL errors"

**Solution:**
1. Verify ddl-auto setting in application.properties:
```properties
spring.jpa.hibernate.ddl-auto=update  # Should be 'update' for auto-creation
```

2. Check Hibernate logs in Spring Boot console for errors

3. Manually verify tables exist:
```sql
USE MED_VAULT;
SHOW TABLES;
# Should see: appointment, prescription, doctor_profile, patient_profile, user, etc.
```

4. If tables missing, run full rebuild:
```bash
# Stop Spring Boot (Ctrl+C)
# Delete tables from MySQL:
# mysql> DROP DATABASE MED_VAULT;
# mysql> CREATE DATABASE MED_VAULT;
# Then restart Spring Boot to recreate tables
```

---

### Issue: "Appointment not showing in doctor dashboard"

**Solution:**
1. Verify appointment exists in database:
```sql
SELECT * FROM appointment WHERE doctor_id = 1 LIMIT 5;
```

2. Verify appointment date is today or future:
```sql
SELECT appointment_date FROM appointment WHERE id = 1;
```

3. Clear browser localStorage and re-login:
```javascript
// In browser console:
localStorage.clear()
// Refresh and login again
```

---

### Issue: Prescription modal not opening

**Solution:**
1. Verify an appointment is selected:
   - Click an appointment row to select it
   - Should see blue highlight on appointment row
   - "Add Prescription" button should be enabled

2. Verify PrescriptionView component is imported:
   - Check DoctorDashboardNew.jsx imports PrescriptionView
   - Check that prescription.css is imported in App.jsx

3. Check browser console (F12) for React errors
   - Look for "PrescriptionView is not defined" or similar
   - See "Troubleshooting Components" section if error found

---

## 📊 API Endpoints Quick Reference

### Patient Endpoints
```
GET    /api/patient/profile
GET    /api/patient/appointments
POST   /api/patient/appointments

GET    /api/patient/appointments/upcoming     [NEW]
GET    /api/patient/appointments/{id}         [NEW]
GET    /api/patient/prescriptions/{apptId}    [NEW]
GET    /api/patient/follow-ups                [NEW]
```

### Doctor Endpoints
```
GET    /api/doctor/profile
GET    /api/doctor/appointments
POST   /api/doctor/appointments/{id}/prescriptions

GET    /api/doctor/appointments/today         [NEW]
GET    /api/doctor/appointments/status/{s}   [NEW]
GET    /api/doctor/appointments/{id}          [NEW]
PUT    /api/doctor/appointments/{id}/status   [NEW]
PUT    /api/doctor/appointments/{id}/consultation [NEW]
```

### Test with cURL
```bash
# List all doctors (no auth required)
curl http://localhost:8081/api/doctors

# Get patient profile (requires valid JWT token)
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     http://localhost:8081/api/patient/profile
```

---

## 📚 Project Structure Reference

```
MED_VAULT/
├── demo/                          # Spring Boot Backend
│   ├── src/main/java/...         # Java source code
│   ├── src/main/resources/
│   │   └── application.properties # Database & JWT config
│   ├── pom.xml                   # Maven dependencies
│   └── mvnw                      # Maven wrapper (run: ./mvnw clean install)
│
└── frontend/                      # React Frontend
    ├── src/
    │   ├── components/           # React components (*.jsx)
    │   ├── styles/              # CSS files (*.css)
    │   ├── App.jsx              # Main app component
    │   └── main.jsx             # React entry point
    ├── package.json             # npm dependencies
    ├── vite.config.js           # Vite configuration
    └── index.html               # HTML template
```

---

## 🎨 Design System Reference

### Colors
- **Primary**: #0ea5e9 (Sky Blue)
- **Primary Dark**: #0284c7
- **Accent**: #14b8a6 (Teal)
- **Success**: #10b981
- **Danger**: #ef4444
- **Warning**: #f59e0b

### Spacing (rem units)
- Small: 0.5rem
- Medium: 1rem
- Large: 1.5rem
- Extra Large: 2rem

### Border Radius
- Small: 8px
- Medium: 12px
- Large: 20px (badges)

---

## 📖 Component Documentation

### PatientDashboardNew.jsx
**Props**: None (uses React Context/localStorage for user data)
**Tabs**: Overview, Appointments, Prescriptions, Profile
**Key Features**:
- Stat cards with counts
- Appointment list/table
- Prescription grid
- Profile edit form
- PrescriptionView modal integration

### DoctorDashboardNew.jsx
**Props**: None
**Tabs**: Overview, Appointments, Patients, Profile
**Key Features**:
- Today's appointment list
- Appointment detail panel
- Status dropdown selector
- Prescription creation modal
- Consultation notes form

### PatientCalendar.jsx
**Props**: appointments (array)
**Features**:
- React-calendar month view
- Date selection
- Appointment timeline
- Responsive layout

### PrescriptionView.jsx
**Props**: 
- prescription (object)
- onClose (function)
**Features**:
- Professional hospital layout
- Medicines table with JSON parsing
- Print functionality
- Signature area

---

## 🔄 Development Workflow

### Making Changes to Backend
```bash
cd demo
# Edit Java files
mvn clean install
# Spring Boot auto-restarts on file changes (with DevTools)
```

### Making Changes to Frontend
```bash
cd frontend
# Edit React/CSS files
# Vite hot-reloads automatically
# Save file → browser refreshes in <200ms
```

### Running Both Simultaneously (Recommended)
```bash
# Terminal 1: Backend
cd demo && mvn spring-boot:run

# Terminal 2: Frontend
cd frontend && npm run dev

# Access: http://localhost:5173 (React Vite)
# Backend: http://localhost:8081/api/* (Spring Boot)
```

---

## 📱 Responsive Design Breakpoints

| Breakpoint | Device | Layout |
|-----------|--------|--------|
| 320px | Mobile (small) | Single column, stacked |
| 425px | Mobile (large) | Single column, full width |
| 768px | Tablet | 2-3 column grid, side nav |
| 1024px | Desktop | Full layout, multi-column |
| 1920px | Large desktop | Maximum width 1400px, centered |

---

## 🎯 Next Steps After Getting System Running

1. **Create Test Data**
   - Login as doctor
   - Create appointments with patients
   - Add prescriptions

2. **Test Email Integration**
   - Configure actual Gmail SMTP in application.properties
   - Test appointment confirmation emails

3. **Production Deployment**
   - Build frontend: `npm run build` → creates dist/ folder
   - Build backend: `mvn clean install` → creates JAR file
   - Deploy to cloud (AWS, Azure, Heroku, etc.)

4. **Performance Optimization**
   - Add pagination to lists
   - Implement caching for frequently accessed data
   - Optimize database queries

5. **Security Hardening**
   - Enable two-factor authentication
   - Add rate limiting to API
   - Implement audit logging

---

## 📞 Common Questions (FAQ)

**Q: How do I change the database password?**
A: Edit `demo/src/main/resources/application.properties` and change `spring.datasource.password=0088` to your MySQL password.

**Q: Can I run just the backend or frontend separately?**
A: Yes, they're independent. Backend serves REST API on port 8081. Frontend (Vite) browser client on port 5173. They communicate via HTTP/CORS.

**Q: How are medicines stored in prescriptions?**
A: As JSON array in `medicinesJson` field. Example:
```json
[
  {"name": "Amoxicillin", "dosage": "500mg", "frequency": "3x daily", "duration": "7 days"}
]
```

**Q: Can I deploy just the frontend without the backend?**
A: No, the frontend requires the backend API to work. You must deploy both.

**Q: How do I reset the database?**
A: Stop Spring Boot, run `DROP DATABASE MED_VAULT;` in MySQL, then restart Spring Boot to recreate tables.

---

## ✅ Final Checklist

Before going to production:
- [ ] Both backend and frontend start without errors
- [ ] Can login as patient and doctor
- [ ] Can view appointments and prescriptions
- [ ] Can create and update prescriptions
- [ ] Can print prescriptions
- [ ] Mobile responsive design verified
- [ ] API endpoints tested with Postman
- [ ] Database backups configured
- [ ] HTTPS/SSL configured (for production)
- [ ] Logging and monitoring set up

---

**Congratulations!** Your MedVault system is ready to use. 🎉

For more details, see `INTEGRATION_COMPLETE.md`
