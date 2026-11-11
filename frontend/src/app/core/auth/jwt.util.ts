export interface JwtClaims {
  sub: string; // email
  uid: string; // user id
  role: string;
  authorities: string;
  iat: number;
  exp: number;
}

/** Décode le payload d'un JWT sans vérifier la signature (uniquement pour lecture UI côté client). */
export function decodeJwt(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    );
    return JSON.parse(json) as JwtClaims;
  } catch {
    return null;
  }
}

export function isJwtExpired(token: string): boolean {
  const claims = decodeJwt(token);
  if (!claims) return true;
  return claims.exp * 1000 < Date.now();
}
