import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axiosInstance';

// Async Thunks for Patient
export const fetchPatientAppointments = createAsyncThunk(
    'appointment/fetchPatientAppointments',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/patient/appointments');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const fetchUpcomingAppointments = createAsyncThunk(
    'appointment/fetchUpcomingAppointments',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/patient/appointments/upcoming');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const fetchPatientPrescriptions = createAsyncThunk(
    'appointment/fetchPatientPrescriptions',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/patient/prescriptions');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const fetchPatientFollowUps = createAsyncThunk(
    'appointment/fetchPatientFollowUps',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/patient/follow-ups');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

// Async Thunks for Doctor
export const fetchDoctorAppointments = createAsyncThunk(
    'appointment/fetchDoctorAppointments',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/doctor/appointments');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const fetchTodayAppointments = createAsyncThunk(
    'appointment/fetchTodayAppointments',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/doctor/appointments/today');
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const updateAppointmentStatus = createAsyncThunk(
    'appointment/updateStatus',
    async ({ id, status }, { rejectWithValue }) => {
        try {
            await api.put(`/doctor/appointments/${id}/status`, { status });
            return { id, status };
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const createPrescription = createAsyncThunk(
    'appointment/createPrescription',
    async ({ appointmentId, data }, { rejectWithValue }) => {
        try {
            const response = await api.post(`/doctor/appointments/${appointmentId}/prescriptions`, data);
            return response.data;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

const initialState = {
    appointments: [],
    upcomingAppointments: [],
    todayAppointments: [],
    prescriptions: [],
    followUps: [],
    loading: false,
    error: null,
    successMessage: null,
};

const appointmentSlice = createSlice({
    name: 'appointment',
    initialState,
    reducers: {
        clearAppointmentMessages(state) {
            state.error = null;
            state.successMessage = null;
        },
        clearAppointmentData(state) {
            state.appointments = [];
            state.upcomingAppointments = [];
            state.todayAppointments = [];
            state.prescriptions = [];
            state.followUps = [];
        }
    },
    extraReducers: (builder) => {
        builder
            // Generic loading/error handler wrapper could be better but explicit here for clarity
            .addCase(fetchPatientAppointments.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(fetchPatientAppointments.fulfilled, (state, action) => {
                state.loading = false;
                state.appointments = action.payload || [];
            })
            .addCase(fetchPatientAppointments.rejected, (state, action) => { state.loading = false; state.error = action.payload; })

            .addCase(fetchUpcomingAppointments.fulfilled, (state, action) => {
                state.upcomingAppointments = action.payload || [];
            })

            .addCase(fetchPatientPrescriptions.fulfilled, (state, action) => {
                state.prescriptions = action.payload || [];
            })

            .addCase(fetchPatientFollowUps.fulfilled, (state, action) => {
                state.followUps = action.payload || [];
            })

            .addCase(fetchDoctorAppointments.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(fetchDoctorAppointments.fulfilled, (state, action) => {
                state.loading = false;
                state.appointments = action.payload || [];
            })
            .addCase(fetchDoctorAppointments.rejected, (state, action) => { state.loading = false; state.error = action.payload; })

            .addCase(fetchTodayAppointments.fulfilled, (state, action) => {
                state.todayAppointments = action.payload || [];
            })

            .addCase(updateAppointmentStatus.fulfilled, (state, action) => {
                const { id, status } = action.payload;
                // Update in appointments list
                const apt = state.appointments.find(a => a.id === id);
                if (apt) apt.status = status;

                // Update in today's list
                const todayApt = state.todayAppointments.find(a => a.id === id);
                if (todayApt) todayApt.status = status;

                state.successMessage = 'Appointment status updated';
            })

            .addCase(createPrescription.fulfilled, (state, action) => {
                state.successMessage = 'Prescription created successfully';
                // Optionally append to prescriptions list if relevant view is active
            });
    },
});

export const { clearAppointmentMessages, clearAppointmentData } = appointmentSlice.actions;
export default appointmentSlice.reducer;
