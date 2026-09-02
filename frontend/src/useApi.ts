import { useMsal } from '@azure/msal-react';
import { loginRequest } from './authConfig';

export function useApi() {
  const { instance, accounts } = useMsal();

  const fetchWithToken = async (url: string) => {
    const account = accounts[0] || instance.getActiveAccount();
    if (!account) throw new Error('No hay una cuenta activa');

    // Solicitar token silenciosamente
    const response = await instance.acquireTokenSilent({
      ...loginRequest,
      account,
    });

    // Adjuntar token en el header HTTP
    return fetch(url, {
      headers: {
        Authorization: `Bearer ${response.accessToken}`,
      },
    });
  };

  return { fetchWithToken };
}
