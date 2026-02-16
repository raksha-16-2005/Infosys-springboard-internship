import React, { useState } from 'react';
import Calendar from 'react-calendar';
import { FiClock, FiMapPin, FiUser } from 'react-icons/fi';
import 'react-calendar/dist/Calendar.css';
import '../styles/calendar.css';

export default function PatientCalendar({ appointments = [] }) {
  const [selectedDate, setSelectedDate] = useState(new Date());

  const getAppointmentsForDate = (date) => {
    return appointments.filter(apt => {
      const aptDate = new Date(apt.appointmentDate).toDateString();
      return aptDate === date.toDateString();
    });
  };

  const getTileClass = (date) => {
    const hasAppointment = getAppointmentsForDate(date).length > 0;
    return hasAppointment ? 'calendar-day-with-events' : '';
  };

  const formatTime = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  };

  const formatFullDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
  };

  const badgeClass = (status) => status.toLowerCase().replace(/_/g, '-');

  const dayAppointments = getAppointmentsForDate(selectedDate);

  return (
    <div className="calendar-container">
      <div className="calendar-section">
        <Calendar
          value={selectedDate}
          onChange={setSelectedDate}
          tileClassName={({ date }) => getTileClass(date)}
        />
      </div>

      <div className="appointments-timeline">
        <h3 className="timeline-title">
          {formatFullDate(selectedDate.toString())}
        </h3>

        {dayAppointments.length > 0 ? (
          <div className="timeline">
            {dayAppointments.map((apt) => (
              <div key={apt.id} className="timeline-item">
                <div className="timeline-marker"></div>
                <div className="timeline-content">
                  <div className="appointment-header">
                    <strong>{apt.doctor?.fullName}</strong>
                    <span className={`badge badge-${badgeClass(apt.status)}`}>
                      {apt.status}
                    </span>
                  </div>
                  <p className="appointment-specialty">{apt.doctor?.specialization}</p>
                  <div className="appointment-meta">
                    <div className="meta-item">
                      <FiClock size={14} />
                      <span>{formatTime(apt.appointmentDate)}</span>
                    </div>
                    {apt.doctor?.hospitalName && (
                      <div className="meta-item">
                        <FiMapPin size={14} />
                        <span>{apt.doctor.hospitalName}</span>
                      </div>
                    )}
                  </div>
                  {apt.symptoms && (
                    <div className="appointment-details">
                      <p><strong>Symptoms:</strong> {apt.symptoms}</p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <p>No appointments scheduled for this date</p>
          </div>
        )}
      </div>
    </div>
  );
}
