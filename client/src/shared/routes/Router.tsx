import { BrowserRouter, Routes, Route } from 'react-router-dom';
import PrivateRoute from './PrivateRoute';
import LoginPage from '../../pages/LoginPage';
import OAuthCallbackPage from '../../pages/OAuthCallbackPage';
import HomePage from '../../pages/HomePage';
import RecordingDetailPage from '../../pages/RecordingDetailPage';
import SettingsPage from '../../pages/SettingsPage';

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <HomePage />
            </PrivateRoute>
          }
        />
        <Route
          path="/recordings/:id"
          element={
            <PrivateRoute>
              <RecordingDetailPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <PrivateRoute>
              <SettingsPage />
            </PrivateRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
};

export default Router;
