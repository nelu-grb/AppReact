import { useState } from 'react';
import { AuthenticatedTemplate, UnauthenticatedTemplate } from '@azure/msal-react';
import { useApi } from './useApi';

export function ProtectedData() {
  const { fetchWithToken } = useApi();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const handleFetchData = async () => {
    setLoading(true);
    try {
      const res = await fetchWithToken('https://graph.microsoft.com/v1.0/me');
      const json = await res.json();
      setData(json);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <AuthenticatedTemplate>
        <button onClick={handleFetchData} disabled={loading}>
          {loading ? 'Consultando...' : 'Obtener Datos del Usuario vía API'}
        </button>
        {data && <pre>{JSON.stringify(data, null, 2)}</pre>}
      </AuthenticatedTemplate>

      <UnauthenticatedTemplate>
        <p>⚠️ Acceso denegado. Debes iniciar sesión para consultar este recurso.</p>
      </UnauthenticatedTemplate>
    </div>
  );
}
