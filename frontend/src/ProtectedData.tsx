import React, { useState } from 'react';
import { useApi } from './useApi';

export const ProtectedData = () => {
  const { fetchWithToken } = useApi();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const consultarApi = async () => {
    setLoading(true);
    setError(null);
    try {
      // Intenta llamar al backend en el puerto 8080
      const response = await fetchWithToken('http://localhost:8080/api/datos-protegidos');
      
      if (!response.ok) {
        throw new Error(`El servidor respondió con código HTTP: ${response.status}`);
      }

      const json = await response.json();
      setData(json);
    } catch (err: any) {
      setError(err.message || 'Error al conectar con la API');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #ccc', borderRadius: '8px' }}>
      <h3>Prueba de API Protegida (Scopes / Bearer Token)</h3>
      <button onClick={consultarApi} disabled={loading} style={{ padding: '8px 16px', cursor: 'pointer' }}>
        {loading ? 'Consultando...' : 'Consultar Backend (http://localhost:8080)'}
      </button>

      {error && (
        <div style={{ marginTop: '15px', color: '#c00', background: '#ffebee', padding: '10px', borderRadius: '4px' }}>
          <strong>Error de conexión:</strong> {error}
        </div>
      )}

      {data && (
        <pre style={{ marginTop: '15px', background: '#f4f4f4', padding: '10px', borderRadius: '4px', overflowX: 'auto' }}>
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  );
};