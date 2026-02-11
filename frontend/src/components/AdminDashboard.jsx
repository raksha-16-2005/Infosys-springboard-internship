import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchAdminStats, fetchDoctors, approveDoctor } from '../features/admin/adminSlice';
import DashboardCard from './dashboard/DashboardCard';

const AdminDashboard = () => {
  const dispatch = useDispatch();
  const { stats, doctors, loading, error, successMessage } = useSelector((state) => state.admin);

  useEffect(() => {
    dispatch(fetchAdminStats());
    dispatch(fetchDoctors());
  }, [dispatch]);

  const handleApproveDoctor = (id) => {
    dispatch(approveDoctor(id));
  };

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <div className="header-content">
          <h1>Admin Dashboard</h1>
          <p className="text-muted">System-wide overview and controls</p>
        </div>
      </div>

      {successMessage && (
        <div className="alert alert-success">
          {successMessage}
        </div>
      )}
      {error && (
        <div className="alert alert-error">
          {typeof error === 'string' ? error : 'An error occurred'}
        </div>
      )}

      <div className="grid grid-4 mb-4">
        <DashboardCard
          title="Total Users"
          value={stats?.totalUsers ?? '-'}
          subtitle="All registered accounts"
        />
        <DashboardCard
          title="Doctors"
          value={stats?.totalDoctors ?? '-'}
          subtitle="Active and pending doctors"
          accent="#0ea5e9"
        />
        <DashboardCard
          title="Patients"
          value={stats?.totalPatients ?? '-'}
          subtitle="Registered patients"
          accent="#22c55e"
        />
        <DashboardCard
          title="Appointments"
          value={stats?.totalAppointments ?? '-'}
          subtitle="Total scheduled"
          accent="#f59e0b"
        />
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">Doctors</h3>
        </div>
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Name</th>
                <th>Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {doctors.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center' }}>
                    No doctors found
                  </td>
                </tr>
              )}
              {doctors.map((doc) => (
                <tr key={doc.id}>
                  <td>{doc.id}</td>
                  <td>{doc.username}</td>
                  <td>{doc.name}</td>
                  <td>{doc.email}</td>
                  <td>{doc.status}</td>
                  <td>
                    {doc.status === 'PENDING' && (
                      <button
                        className="btn btn-primary"
                        disabled={loading}
                        onClick={() => handleApproveDoctor(doc.id)}
                      >
                        Approve
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;

