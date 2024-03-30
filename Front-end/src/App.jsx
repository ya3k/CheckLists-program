import React from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import Navbar from './Navbar';
import { AuthProvider } from 'oidc-react';
<<<<<<<< HEAD:Front-end/src/App.jsx
import { useNavigate, Outlet } from 'react-router-dom';

const getOidcConfig = (navigate) => ({
========
import TokenUpdater from './TokenUpdater';
import { Outlet } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const oidcConfig = {
>>>>>>>> origin/sang-fe:front_end/src/App.jsx
  onSignIn: async (userData) => {
    console.log('user data', userData);
    localStorage.setItem('access_token', userData.access_token);
    
    // Navigate to your desired route after login
    navigate('/');
  },
  authority: `${process.env.REACT_APP_KEYCLOAK_URL}/realms/${process.env.REACT_APP_KEYCLOAK_REALM}`,
  clientId: process.env.REACT_APP_KEYCLOAK_CLIENT,
  clientSecret: process.env.REACT_APP_KEYCLOAK_SECRET_CLIENT,
  responseType: 'code',
  redirectUri: window.location.origin,
  // autoSignIn: true,
  automaticSilentRenew: true,
  scope: 'openid profile email',
});

function App() {
  const navigate = useNavigate();
  const oidcConfig = getOidcConfig(navigate);

  return (
    <AuthProvider {...oidcConfig}>
      {/* <TokenUpdater /> */}
      <div className='d-flex bg-light vh-100'>
        <div className='w-auto'>
        <ToastContainer />
        </div>
        <div className='col'>
          <Navbar />
          <Outlet/>
          {/*<TablePrograms />*/}
          {/* <TaskPage/> */}
        </div>
      </div>
    </AuthProvider>
  );
}

export default App;