import React from 'react';
import { useMsal } from '@azure/msal-react';

export const Navbar: React.FC = () => {
  const { instance, accounts } = useMsal();
  const currentAccount = accounts[0] || instance.getActiveAccount();

  const handleLogout = () => {
    instance.logoutRedirect();
  };

  return (
    <header className="navbar">
      <div className="navbar-brand">Panel de Control</div>
      <div className="navbar-user">
        <span>Hola, <strong>{currentAccount?.name || currentAccount?.username}</strong></span>
        <button className="btn-logout" onClick={handleLogout}>
          Cerrar Sesión
        </button>
      </div>
    </header>
  );
};