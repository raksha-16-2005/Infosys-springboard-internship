START TRANSACTION;

SET @patient_id := (SELECT id FROM users WHERE email='praneshsekar07@gmail.com' LIMIT 1);
SET @doctor_id := (SELECT id FROM users WHERE email='doctorpranesh580@gmail.com' LIMIT 1);
SET @admin_id := (SELECT id FROM users WHERE email='praneshsekar52@gmail.com' LIMIT 1);
SET @patient_profile_id := (SELECT id FROM patient_profile WHERE user_id=@patient_id LIMIT 1);
SET @doctor_profile_id := (SELECT id FROM doctor_profile WHERE user_id=@doctor_id LIMIT 1);

INSERT INTO patient_profile (user_id, full_name, age, gender, phone, blood_group, address, emergency_contact, medical_history)
VALUES (
  @patient_id,
  'Pranesh Sekar',
  23,
  'Male',
  '9876543210',
  'B+',
  'Chennai, Tamil Nadu',
  'Lakshmi Sekar - 9876500001',
  'Seasonal allergy, mild gastritis, vitamin D deficiency history'
)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  age = VALUES(age),
  gender = VALUES(gender),
  phone = VALUES(phone),
  blood_group = VALUES(blood_group),
  address = VALUES(address),
  emergency_contact = VALUES(emergency_contact),
  medical_history = VALUES(medical_history);

INSERT INTO doctor_profile (user_id, full_name, phone, specialization, qualification, hospital_name, experience_years, consultation_fee, bio, available_slots)
VALUES (
  @doctor_id,
  'Dr. Pranesh R',
  '9000011122',
  'General Medicine',
  'MBBS, MD (Internal Medicine)',
  'MedVault Multispeciality Clinic',
  8,
  650.00,
  'Focused on preventive medicine, chronic care follow-up, and patient education.',
  'MON-FRI 10:00-13:00, 17:00-20:00'
)
ON DUPLICATE KEY UPDATE
  full_name = VALUES(full_name),
  phone = VALUES(phone),
  specialization = VALUES(specialization),
  qualification = VALUES(qualification),
  hospital_name = VALUES(hospital_name),
  experience_years = VALUES(experience_years),
  consultation_fee = VALUES(consultation_fee),
  bio = VALUES(bio),
  available_slots = VALUES(available_slots);

INSERT INTO patient_consents (patient_id, doctor_id, consent_granted, granted_at, reason)
SELECT @patient_id, @doctor_id, b'1', NOW(), 'Primary treating doctor access for ongoing consultation and record review'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM patient_consents pc
  WHERE pc.patient_id = @patient_id
    AND pc.doctor_id = @doctor_id
    AND pc.consent_granted = b'1'
    AND pc.revoked_at IS NULL
);

INSERT INTO appointments (
  doctor_id,
  patient_id,
  appointment_date,
  symptoms,
  notes,
  status,
  created_at,
  consultation_notes,
  doctor_remarks,
  reminder_sent
)
VALUES
  (
    @doctor_profile_id,
    @patient_profile_id,
    DATE_SUB(NOW(), INTERVAL 12 DAY),
    'Fever, sore throat, fatigue',
    'Initial consultation visit',
    'COMPLETED',
    DATE_SUB(NOW(), INTERVAL 13 DAY),
    'Hydration, antipyretics, rest advised',
    'Improved in 3 days',
    b'1'
  ),
  (
    @doctor_profile_id,
    @patient_profile_id,
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    'Acidity, bloating after meals',
    'Follow-up for gastric discomfort',
    'COMPLETED',
    DATE_SUB(NOW(), INTERVAL 6 DAY),
    'Diet correction and short-term PPIs prescribed',
    'Avoid oily/spicy food',
    b'1'
  ),
  (
    @doctor_profile_id,
    @patient_profile_id,
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    'Routine preventive checkup',
    'Annual health review booking',
    'ACCEPTED',
    NOW(),
    'Pending consultation',
    NULL,
    b'0'
  );

SET @appt_old_1 := (
  SELECT id
  FROM appointments
  WHERE patient_id = @patient_profile_id
    AND doctor_id = @doctor_profile_id
    AND status = 'COMPLETED'
  ORDER BY appointment_date ASC
  LIMIT 1
);

SET @appt_old_2 := (
  SELECT id
  FROM appointments
  WHERE patient_id = @patient_profile_id
    AND doctor_id = @doctor_profile_id
    AND status = 'COMPLETED'
  ORDER BY appointment_date DESC
  LIMIT 1
);

INSERT INTO prescriptions (appointment_id, diagnosis, medicines_json, tests_recommended, follow_up_date, notes, created_at)
SELECT
  @appt_old_1,
  'Viral pharyngitis with mild dehydration',
  '[{"name":"Paracetamol 650mg","dosage":"1 tablet","frequency":"SOS up to 3/day","duration":"3 days"},{"name":"Levocetirizine 5mg","dosage":"1 tablet","frequency":"Night","duration":"5 days"}]',
  'CBC, CRP if fever persists beyond 4 days',
  DATE_ADD(CURDATE(), INTERVAL 10 DAY),
  'Warm fluids and adequate sleep advised',
  DATE_SUB(NOW(), INTERVAL 12 DAY)
FROM DUAL
WHERE @appt_old_1 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM prescriptions p
    WHERE p.appointment_id = @appt_old_1
  );

INSERT INTO prescriptions (appointment_id, diagnosis, medicines_json, tests_recommended, follow_up_date, notes, created_at)
SELECT
  @appt_old_2,
  'Acid peptic symptoms likely due to irregular meals',
  '[{"name":"Pantoprazole 40mg","dosage":"1 tablet","frequency":"Before breakfast","duration":"10 days"},{"name":"Antacid syrup","dosage":"10 ml","frequency":"After meals","duration":"7 days"}]',
  'LFT and H. pylori stool antigen if symptoms recur',
  DATE_ADD(CURDATE(), INTERVAL 15 DAY),
  'Small frequent meals and reduced caffeine',
  DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM DUAL
WHERE @appt_old_2 IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM prescriptions p
    WHERE p.appointment_id = @appt_old_2
  );

INSERT INTO medical_record (patient_id, filename, file_path, content, doctor_name, uploaded_at)
VALUES
  (
    @patient_id,
    'prescription_fever_visit.pdf',
    '/demo/prescriptions/prescription_fever_visit.pdf',
    'Prescription summary for fever and sore throat visit',
    'prescription',
    DATE_SUB(NOW(), INTERVAL 12 DAY)
  ),
  (
    @patient_id,
    'lab_report_cbc_apr2026.pdf',
    '/demo/reports/lab_report_cbc_apr2026.pdf',
    'CBC lab report showing mild elevated WBC',
    'lab-report',
    DATE_SUB(NOW(), INTERVAL 10 DAY)
  ),
  (
    @patient_id,
    'diagnosis_gastritis_note.txt',
    '/demo/diagnosis/diagnosis_gastritis_note.txt',
    'Doctor diagnosis note for gastritis symptoms and care plan',
    'diagnosis',
    DATE_SUB(NOW(), INTERVAL 5 DAY)
  ),
  (
    @patient_id,
    'followup_prescription_gastro.pdf',
    '/demo/prescriptions/followup_prescription_gastro.pdf',
    'Follow-up prescription for gastric discomfort',
    'prescription',
    DATE_SUB(NOW(), INTERVAL 4 DAY)
  );

INSERT INTO notifications (user_id, message, type, is_read, created_at)
VALUES
  (
    @patient_id,
    'Your appointment request with Dr. Pranesh R has been accepted for upcoming health review.',
    'APPOINTMENT_CONFIRMED',
    b'0',
    NOW()
  ),
  (
    @doctor_id,
    'New patient follow-up case has been added to your dashboard records.',
    'SYSTEM_NOTIFICATION',
    b'0',
    NOW()
  ),
  (
    @admin_id,
    'Patient praneshsekar07@gmail.com accessed the system. Login activity recorded.',
    'SYSTEM_NOTIFICATION',
    b'0',
    NOW()
  );

COMMIT;
