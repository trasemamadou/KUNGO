const apiBaseUrl = window.__env?.apiBaseUrl || 'http://localhost:8080/api/v1';

export const environment = {
  production: false,
  apiBaseUrl,
  wsUrl: `${apiBaseUrl}/ws`,
};
