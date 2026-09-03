import React, { useState } from 'react';
import { useApi } from './useApi';

export const ProtectedData: React.FC = () => {
  const { fetchWithToken } = useApi();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const consultarApi = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetchWithToken('http://localhost:8081/api/ordenes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ detalle: 'Prueba desde React' })
      });

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
    <div className="card">
      <h3 className="card-title">Prueba de API Protegida (Scopes / Bearer Token)</h3>
      <p className="card-subtitle">
        Petición POST autenticada al microservicio de órdenes usando el token de Azure Entra ID.
      </p>

      <button className="btn-primary" onClick={consultarApi} disabled={loading}>
        {loading ? 'Consultando...' : 'Consultar Backend (http://localhost:8081)'}
      </button>

      {error && (
        <div className="error-banner">
          <strong>Error de conexión:</strong> {error}
        </div>
      )}

      {data && (
        <pre className="code-block">
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  );
};