// src/useAuth.ts
import { useMsal, useIsAuthenticated } from '@azure/msal-react';

export const useAuth = () => {
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  // Equivalente a login()
  const login = () => {
    instance.loginRedirect().catch((error) => {
      console.error('Error al iniciar sesión:', error);
    });
  };

  // Equivalente a logout()
  const logout = () => {
    instance.logoutRedirect().catch((error) => {
      console.error('Error al cerrar sesión:', error);
    });
  };

  // Equivalente a isLoggedIn() y datos de la cuenta activa
  const user = accounts[0] || null;

  return {
    isLoggedIn: isAuthenticated,
    user,
    login,
    logout,
  };
};