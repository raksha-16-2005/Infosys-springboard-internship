import React, { useState } from 'react';
import axios from 'axios';
import { motion } from 'framer-motion';

const initialForm = {
  appointmentId: '',
  newDate: '',
  newTime: '',
  reason: ''
};

export default function AdminRescheduleForm({ onRescheduled }) {
  const token = localStorage.getItem('token');
  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [feedback, setFeedback] = useState({ type: '', text: '' });

  const setField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFeedback({ type: '', text: '' });
    setIsSubmitting(true);

    try {
      const dateTime = new Date(`${form.newDate}T${form.newTime}:00`);
      await axios.put(
        `/api/admin/appointments/${form.appointmentId}/reschedule`,
        {
          appointmentDate: dateTime.toISOString(),
          reason: form.reason
        },
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      setFeedback({ type: 'success', text: 'Appointment rescheduled successfully.' });
      setForm(initialForm);
      if (onRescheduled) {
        onRescheduled();
      }
    } catch (error) {
      const message = error?.response?.data;
      setFeedback({
        type: 'error',
        text: typeof message === 'string' ? message : 'Unable to reschedule appointment.'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <motion.section
      className="admin-reschedule-card"
      initial={{ opacity: 0, y: 28 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.42, ease: 'easeOut' }}
    >
      <div className="admin-reschedule-head">
        <h2>Reschedule Appointment</h2>
        <p className="muted">Update schedule instantly with patient-safe visibility.</p>
      </div>

      <form className="admin-reschedule-form" onSubmit={handleSubmit}>
        <label>
          Appointment ID
          <input
            type="number"
            value={form.appointmentId}
            onChange={(event) => setField('appointmentId', event.target.value)}
            placeholder="Enter appointment ID"
            required
          />
        </label>

        <div className="admin-reschedule-row">
          <label>
            New Date
            <input
              type="date"
              value={form.newDate}
              onChange={(event) => setField('newDate', event.target.value)}
              required
            />
          </label>

          <label>
            New Time
            <input
              type="time"
              value={form.newTime}
              onChange={(event) => setField('newTime', event.target.value)}
              required
            />
          </label>
        </div>

        <label>
          Reschedule Reason
          <textarea
            rows="3"
            value={form.reason}
            onChange={(event) => setField('reason', event.target.value)}
            placeholder="Add a brief reason"
            required
          />
        </label>

        <motion.button
          type="submit"
          className="admin-gradient-btn"
          whileHover={{ y: -2, scale: 1.01 }}
          whileTap={{ scale: 0.98 }}
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Updating...' : 'Confirm Reschedule'}
        </motion.button>
      </form>

      {feedback.text && (
        <p className={`admin-reschedule-message ${feedback.type}`}>{feedback.text}</p>
      )}
    </motion.section>
  );
}
