// Valeur par défaut utilisée en développement local (ng serve) et écrasée au
// démarrage du conteneur Docker par docker-entrypoint.sh à partir de env.template.js.
window.__env = {
  apiBaseUrl: "http://localhost:8080/api/v1",
};
