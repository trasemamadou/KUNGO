// Généré au démarrage du conteneur (voir docker-entrypoint.sh) à partir des
// variables d'environnement Docker, pour permettre de changer l'URL de l'API
// sans reconstruire l'image (configuration runtime plutôt que build-time).
window.__env = {
  apiBaseUrl: "${API_BASE_URL}",
};
