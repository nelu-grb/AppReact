import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { loginRequest } from './authConfig';
import { ProtectedData } from './ProtectedData';

export default function App() {
  const { instance, accounts, inProgress } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const currentUser = accounts[0];

  const handleLogin = () => {
    if (inProgress === InteractionStatus.None) {
      instance.loginRedirect(loginRequest).catch((e) => console.error(e));
    }
  };

  const handleLogout = () => {
    if (inProgress === InteractionStatus.None) {
      instance.logoutRedirect({ postLogoutRedirectUri: '/' }).catch((e) => console.error(e));
    }
  };

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
      <h1>Portal de Autenticación con Microsoft Entra ID</h1>

      {isAuthenticated ? (
        <div>
          <p>Bienvenido, <strong>{currentUser?.name || currentUser?.username}</strong></p>
          <button onClick={handleLogout}>Cerrar Sesión</button>
          <hr style={{ margin: '1.5rem 0' }} />
          <ProtectedData />
        </div>
      ) : (
        <div>
          <p>Debes iniciar sesión con tu cuenta institucional para continuar.</p>
          <button onClick={handleLogin} disabled={inProgress !== InteractionStatus.None}>
            {inProgress !== InteractionStatus.None ? 'Cargando...' : 'Iniciar Sesión'}
          </button>
        </div>
      )}
    </div>
  );
}
