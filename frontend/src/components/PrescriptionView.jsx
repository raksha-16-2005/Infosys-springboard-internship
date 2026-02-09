import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { FiX, FiPrinter } from 'react-icons/fi';
import '../styles/prescription.css';

export default function PrescriptionView({ prescription, patient, doctor, onClose }) {
  const [isPrinting, setIsPrinting] = useState(false);

  if (!prescription) {
    return null;
  }

  const handlePrint = () => {
    setIsPrinting(true);
    setTimeout(() => {
      window.print();
      setIsPrinting(false);
    }, 100);
  };

  let medicines = [];
  try {
    if (typeof prescription.medicinesJson === 'string') {
      medicines = JSON.parse(prescription.medicinesJson);
    } else if (Array.isArray(prescription.medicinesJson)) {
      medicines = prescription.medicinesJson;
    }
  } catch (error) {
    console.error('Failed to parse medicines:', error);
    medicines = [];
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { day: 'numeric', month: 'long', year: 'numeric' });
  };

  const prescriptionDate = prescription.appointmentId ? formatDate(prescription.createdAt) : formatDate(new Date());

  return (
    <AnimatePresence>
      <motion.div
        className="prescription-overlay"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        onClick={onClose}
      >
        <motion.div
          className="prescription-modal"
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.95, opacity: 0 }}
          onClick={(e) => e.stopPropagation()}
        >
          <div className="prescription-header">
            <h2>Medical Prescription</h2>
            <button className="close-button" onClick={onClose}>
              <FiX size={24} />
            </button>
          </div>

          <div className="prescription-content">
            <div className="prescription-document">
              <div className="hospital-header">
                <div className="hospital-info">
                  <h1>MedVault Hospital</h1>
                  <p>Complete Healthcare Solution</p>
                  <p className="contact">Emergency: +91-XXXX-XXXX-XX</p>
                </div>
                <div className="doc-number">
                  <p>Rx: {prescription.id || 'N/A'}</p>
                  <p>{prescriptionDate}</p>
                </div>
              </div>

              <hr className="divider" />

              <div className="patient-info-grid">
                <div className="info-block">
                  <label>Patient Name</label>
                  <p>{patient?.fullName || 'N/A'}</p>
                </div>
                <div className="info-block">
                  <label>Age</label>
                  <p>{patient?.age || 'N/A'} years</p>
                </div>
                <div className="info-block">
                  <label>Gender</label>
                  <p>{patient?.gender || 'N/A'}</p>
                </div>
                <div className="info-block">
                  <label>Contact</label>
                  <p>{patient?.mobileNumber || 'N/A'}</p>
                </div>
              </div>

              <hr className="divider" />

              <div className="doctor-consultation">
                <div className="consultation-header">
                  <h3>Doctor Consultation</h3>
                </div>
                <div className="doctor-info">
                  <p><strong>Doctor:</strong> {doctor?.fullName || 'N/A'}</p>
                  <p><strong>Specialization:</strong> {doctor?.specialization || 'N/A'}</p>
                  <p><strong>Hospital:</strong> {doctor?.hospitalName || 'N/A'}</p>
                </div>
              </div>

              <div className="diagnosis-section">
                <h4>Diagnosis</h4>
                <div className="diagnosis-box">
                  {prescription.diagnosis || 'No diagnosis recorded'}
                </div>
              </div>

              {medicines.length > 0 && (
                <div className="medicines-section">
                  <h4>Medications</h4>
                  <table className="medicines-table">
                    <thead>
                      <tr>
                        <th>Medicine Name</th>
                        <th>Dosage</th>
                        <th>Frequency</th>
                        <th>Duration</th>
                      </tr>
                    </thead>
                    <tbody>
                      {medicines.map((med, idx) => (
                        <tr key={idx}>
                          <td>{med.name || '-'}</td>
                          <td>{med.dosage || '-'}</td>
                          <td>{med.frequency || '-'}</td>
                          <td>{med.duration || '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {prescription.testsRecommended && (
                <div className="tests-section">
                  <h4>Recommended Tests</h4>
                  <p>{prescription.testsRecommended}</p>
                </div>
              )}

              {prescription.followUpDate && (
                <div className="followup-section">
                  <h4>Follow-up Date</h4>
                  <p>{formatDate(prescription.followUpDate)}</p>
                </div>
              )}

              {prescription.notes && (
                <div className="notes-section">
                  <h4>Additional Notes</h4>
                  <p>{prescription.notes}</p>
                </div>
              )}

              <hr className="divider" />

              <div className="signature-section">
                <div className="signature-block">
                  <div className="signature-line"></div>
                  <p>Doctor Signature</p>
                </div>
                <div className="signature-block">
                  <p>Date: {prescriptionDate}</p>
                </div>
              </div>

              <p className="footer-text">
                This is a digital prescription. Please keep it for your records.
              </p>
            </div>
          </div>

          <div className="prescription-footer">
            <button
              className="print-button"
              onClick={handlePrint}
              disabled={isPrinting}
            >
              <FiPrinter size={18} />
              {isPrinting ? 'Printing...' : 'Print Prescription'}
            </button>
            <button className="close-btn" onClick={onClose}>
              Close
            </button>
          </div>
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
}
