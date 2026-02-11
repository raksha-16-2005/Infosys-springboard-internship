import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import userReducer from '../features/user/userSlice';
import appointmentReducer from '../features/appointment/appointmentSlice';
import adminReducer from '../features/admin/adminSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    user: userReducer,
    appointment: appointmentReducer,
    admin: adminReducer,
  },
});

export default store;

