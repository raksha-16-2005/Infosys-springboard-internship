import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axiosInstance';

// Async Thunks
export const fetchUserProfile = createAsyncThunk(
  'user/fetchProfile',
  async (_, { rejectWithValue, getState }) => {
    try {
      const { auth } = getState();
      const role = auth.role || '';
      let endpoint = '';
      
      if (role.includes('PATIENT')) {
        endpoint = '/patient/profile';
      } else if (role.includes('DOCTOR')) {
        endpoint = '/doctor/profile';
      } else {
        // Admin or other roles might not have a specific 'profile' endpoint in this context, 
        // or might share one. For now handling Patient/Doctor.
        return null; 
      }

      const response = await api.get(endpoint);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

export const updateUserProfile = createAsyncThunk(
  'user/updateProfile',
  async (profileData, { rejectWithValue, getState }) => {
    try {
      const { auth } = getState();
      const role = auth.role || '';
      let endpoint = '';

      if (role.includes('PATIENT')) {
        endpoint = '/patient/profile';
      } else if (role.includes('DOCTOR')) {
        endpoint = '/doctor/profile';
      } else {
        return rejectWithValue('User role not supported for profile update');
      }

      const response = await api.put(endpoint, profileData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const initialState = {
  profile: null,
  loading: false,
  error: null,
  successMessage: null,
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    clearUserMessages(state) {
      state.error = null;
      state.successMessage = null;
    },
    clearUserProfile(state) {
        state.profile = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch Profile
      .addCase(fetchUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        state.profile = action.payload;
      })
      .addCase(fetchUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Update Profile
      .addCase(updateUserProfile.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.successMessage = null;
      })
      .addCase(updateUserProfile.fulfilled, (state, action) => {
        state.loading = false;
        // The API might return the updated object or just a success status. 
        // If it returns the object, update the state.
        if (action.payload && typeof action.payload === 'object') {
             // If payload is the stored profile object
             // Merge or replace. Assuming it returns the updated entity or DTO.
             // Based on controllers, it returns UserSummaryDTO or similar.
             // Let's assume for now we might need to re-fetch or merge.
             // If backend returns void/string, we might need to re-fetch.
             // AdminController returns DTO, let's assume Patient/Doctor do too (based on typical patterns).
             // However, safe refetching strategy or spread if payload matches is good.
             // For now, let's update profile if payload looks like a profile
             // OR simply set a success flag and let the component trigger a re-fetch if needed (or rely on this).
             // Actually, looking at the code in dashboards, they re-fetch after update. 
             // We can optimize by updating state directly if payload is the profile.
             // Let's rely on re-fetching or response data.
        }
        state.successMessage = 'Profile updated successfully';
      })
      .addCase(updateUserProfile.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearUserMessages, clearUserProfile } = userSlice.actions;
export default userSlice.reducer;
