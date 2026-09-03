import { useMsal } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';

// Scope exacto de tu Backend registrado en Azure
const API_SCOPES = ['api://ed8a85ef-f2d4-48c5-a17e-b69abfc4694e/OT.create'];

export function useApi() {
  const { instance, accounts } = useMsal();

  const fetchWithToken = async (url: string, options: RequestInit = {}) => {
    const account = accounts[0] || instance.getActiveAccount();
    if (!account) throw new Error('No hay una cuenta activa');

    const tokenRequest = {
      scopes: API_SCOPES,
      account,
    };

    let accessToken = '';

    try {
      // 1. Intento silencioso
      const response = await instance.acquireTokenSilent(tokenRequest);
      accessToken = response.accessToken;
    } catch (error: any) {
      // 2. Si falla por timeout o necesita interacción del usuario, abre popup
      if (error instanceof InteractionRequiredAuthError || error.message?.includes('timed_out')) {
        const response = await instance.acquireTokenPopup(tokenRequest);
        accessToken = response.accessToken;
      } else {
        throw error;
      }
    }

    // 3. Petición HTTP combinando los headers
    const headers = new Headers(options.headers || {});
    headers.set('Authorization', `Bearer ${accessToken}`);

    return fetch(url, {
      ...options,
      headers,
    });
  };

  return { fetchWithToken };
}