import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axiosInstance';

export const fetchAdminStats = createAsyncThunk(
    'admin/fetchStats',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/admin/stats');
            return response.data.data || response.data; // Handling potential wrapper
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const fetchDoctors = createAsyncThunk(
    'admin/fetchDoctors',
    async (_, { rejectWithValue }) => {
        try {
            const response = await api.get('/admin/doctors');
            return response.data.data || response.data || [];
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const approveDoctor = createAsyncThunk(
    'admin/approveDoctor',
    async (id, { rejectWithValue, dispatch }) => {
        try {
            await api.post(`/admin/doctors/${id}/approve`);
            return id;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const suspendUser = createAsyncThunk(
    'admin/suspendUser',
    async (id, { rejectWithValue }) => {
        try {
            await api.post(`/admin/users/${id}/suspend`);
            return id;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

export const activateUser = createAsyncThunk(
    'admin/activateUser',
    async (id, { rejectWithValue }) => {
        try {
            await api.post(`/admin/users/${id}/activate`);
            return id;
        } catch (error) {
            return rejectWithValue(error.response?.data || error.message);
        }
    }
);

const initialState = {
    stats: null,
    doctors: [],
    users: [],
    loading: false,
    error: null,
    successMessage: null,
};

const adminSlice = createSlice({
    name: 'admin',
    initialState,
    reducers: {
        clearAdminMessages(state) {
            state.error = null;
            state.successMessage = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchAdminStats.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(fetchAdminStats.fulfilled, (state, action) => {
                state.loading = false;
                state.stats = action.payload;
            })
            .addCase(fetchAdminStats.rejected, (state, action) => { state.loading = false; state.error = action.payload; })

            .addCase(fetchDoctors.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(fetchDoctors.fulfilled, (state, action) => {
                state.loading = false;
                state.doctors = action.payload;
            })
            .addCase(fetchDoctors.rejected, (state, action) => { state.loading = false; state.error = action.payload; })

            .addCase(approveDoctor.fulfilled, (state, action) => {
                state.successMessage = 'Doctor approved';
                // Optimistic update
                const doc = state.doctors.find(d => d.id === action.payload);
                if (doc) doc.status = 'ACTIVE'; // or 'APPROVED' depending on backend enum
            })
            .addCase(approveDoctor.rejected, (state, action) => {
                state.error = action.payload;
            });
    },
});

export const { clearAdminMessages } = adminSlice.actions;
export default adminSlice.reducer;
