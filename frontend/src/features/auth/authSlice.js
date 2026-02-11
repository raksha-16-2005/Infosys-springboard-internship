import { createSlice } from '@reduxjs/toolkit';

const tokenFromStorage = localStorage.getItem('token');
const userFromStorage = (() => {
  try {
    const v = localStorage.getItem('user');
    return v ? JSON.parse(v) : null;
  } catch {
    return null;
  }
})();

const initialState = {
  user: userFromStorage,
  token: tokenFromStorage || null,
  role: userFromStorage?.role || null,
  isAuthenticated: !!tokenFromStorage,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginStart(state) {
      state.loading = true;
      state.error = null;
    },
    loginSuccess(state, action) {
      const payload = action.payload;
      state.loading = false;
      state.error = null;
      state.token = payload.token;
      state.user = payload;
      state.role = payload.role;
      state.isAuthenticated = true;
      localStorage.setItem('token', payload.token);
      localStorage.setItem('user', JSON.stringify(payload));
    },
    loginFailure(state, action) {
      state.loading = false;
      state.error = action.payload || 'Login failed';
    },
    logout(state) {
      state.user = null;
      state.token = null;
      state.role = null;
      state.isAuthenticated = false;
      state.error = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    setUser(state, action) {
      state.user = { ...(state.user || {}), ...action.payload };
      localStorage.setItem('user', JSON.stringify(state.user));
    },
  },
});

export const { loginStart, loginSuccess, loginFailure, logout, setUser } = authSlice.actions;
export default authSlice.reducer;

