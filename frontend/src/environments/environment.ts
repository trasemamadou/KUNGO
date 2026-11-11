declare global {
  interface Window {
    __env?: { apiBaseUrl?: string };
  }
}

const apiBaseUrl = window.__env?.apiBaseUrl || 'https://api.kungo.app/api/v1';

export const environment = {
  production: true,
  apiBaseUrl,
  wsUrl: `${apiBaseUrl}/ws`,
};
