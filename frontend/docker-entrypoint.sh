#!/bin/sh
# Exécuté automatiquement par l'image nginx officielle au démarrage du conteneur
# (placé dans /docker-entrypoint.d/, convention native de l'image nginx:alpine).
# Régénère env.js à partir des variables d'environnement, pour permettre de
# changer l'URL de l'API sans reconstruire l'image Docker.
set -e

: "${API_BASE_URL:=http://localhost:8080/api/v1}"
export API_BASE_URL

envsubst '${API_BASE_URL}' < /usr/share/nginx/html/env.template.js > /usr/share/nginx/html/env.js
