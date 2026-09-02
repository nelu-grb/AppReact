// src/Navbar.tsx (o dentro de App.tsx)
import React from 'react';
import { useAuth } from './useAuth';


export const AppLayout = () => {
  const { isLoggedIn, login, logout } = useAuth();

  return (
    <div>
      <nav style={{ padding: '12px', borderBottom: '1px solid #ccc', marginBottom: '20px' }}>
        {!isLoggedIn && (
          <button onClick={login}>Iniciar sesión</button>
        )}

        {isLoggedIn && (
          <button onClick={logout}>Cerrar sesión</button>
        )}
      </nav>

      {/* Equivalente a <router-outlet></router-outlet> */}
      <main>
        {/* Si usas React Router: <Outlet /> */}
        {/* O contenido dinámico/rutas de tu aplicación */}
      </main>
    </div>
  );
};

export default AppLayout;