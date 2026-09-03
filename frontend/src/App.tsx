import React, { useEffect } from 'react';
import { 
  AuthenticatedTemplate, 
  UnauthenticatedTemplate, 
  useMsal 
} from '@azure/msal-react';
import { ProtectedData } from './ProtectedData';
import { Navbar } from './Navbar';
import { loginRequest } from './authConfig';
import './App.css';

const LoginScreen: React.FC = () => {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance.loginRedirect(loginRequest);
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <h1 style={{ marginTop: 0, fontSize: '1.75rem' }}>Bienvenido</h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
          Debes iniciar sesión con tu cuenta corporativa para continuar.
        </p>
        <button className="btn-primary" onClick={handleLogin} style={{ width: '100%' }}>
          Iniciar Sesión con Microsoft
        </button>
      </div>
    </div>
  );
};

export default function App() {
  const { instance } = useMsal();

  useEffect(() => {
    instance.handleRedirectPromise().catch((error) => {
      console.error("Error al procesar redirect:", error);
    });
  }, [instance]);

  return (
    <div className="app-container">
      <AuthenticatedTemplate>
        <Navbar />
        <main className="main-wrapper">
          <ProtectedData />
        </main>
      </AuthenticatedTemplate>

      <UnauthenticatedTemplate>
        <LoginScreen />
      </UnauthenticatedTemplate>
    </div>
  );
}