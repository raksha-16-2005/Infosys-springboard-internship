package com.example.demo.dto;

public class AdminStatsDTO {
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long todaysAppointments;
    private long approvedAppointments;
    private long pendingAppointments;

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getTodaysAppointments() {
        return todaysAppointments;
    }

    public void setTodaysAppointments(long todaysAppointments) {
        this.todaysAppointments = todaysAppointments;
    }

    public long getApprovedAppointments() {
        return approvedAppointments;
    }

    public void setApprovedAppointments(long approvedAppointments) {
        this.approvedAppointments = approvedAppointments;
    }

    public long getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(long pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }
}
