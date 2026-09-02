import React from 'react';
import { 
  AuthenticatedTemplate, 
  UnauthenticatedTemplate, 
  useMsal 
} from '@azure/msal-react';
import { ProtectedData } from './ProtectedData';

const LoginScreen = () => {
  const { instance } = useMsal();

  const handleLogin = () => {
    instance.loginPopup().catch((error) => console.error(error));
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h1>Bienvenido a la Aplicación</h1>
      <p>Debes iniciar sesión con tu cuenta corporativa para continuar.</p>
      <button onClick={handleLogin} style={{ padding: '10px 20px', cursor: 'pointer' }}>
        Iniciar Sesión con Microsoft
      </button>
    </div>
  );
};

const MainContent = () => {
  const { instance, accounts } = useMsal();
  const currentAccount = accounts[0];

  const handleLogout = () => {
    instance.logoutPopup().catch((error) => console.error(error));
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto', fontFamily: 'sans-serif' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #eee', paddingBottom: '15px' }}>
        <h2>Panel Principal</h2>
        <div>
          <span>Hola, <strong>{currentAccount?.name || currentAccount?.username}</strong></span>
          <button onClick={handleLogout} style={{ marginLeft: '15px', padding: '6px 12px', cursor: 'pointer' }}>
            Cerrar Sesión
          </button>
        </div>
      </header>

      <main style={{ marginTop: '20px' }}>
        <p>Tu sesión está activa y validada por Microsoft Entra ID.</p>
        
        {/* Componente para probar peticiones con token */}
        <ProtectedData />
      </main>
    </div>
  );
};

export default function App() {
  return (
    <div>
      <AuthenticatedTemplate>
        <MainContent />
      </AuthenticatedTemplate>

      <UnauthenticatedTemplate>
        <LoginScreen />
      </UnauthenticatedTemplate>
    </div>
  );
}