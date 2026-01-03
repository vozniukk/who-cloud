/**
 * JWT utilities for authentication
 */

export interface JWTPayload {
  sub: string; // email
  role: string;
  exp: number;
  iat: number;
  fullName?: string;
  googleId?: string;
}

export interface User {
  email: string;
  role: string;
  fullName?: string;
  googleId?: string;
  isAuthenticated: boolean;
}

/**
 * Decode JWT token without verification (client-side)
 * Note: This doesn't verify signature, only parses the payload
 */
export function decodeJWT(token: string): JWTPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;

    const payload = parts[1];
    const decoded = JSON.parse(
      Buffer.from(payload, 'base64').toString('utf-8')
    );

    return decoded as JWTPayload;
  } catch (error) {
    console.error('Failed to decode JWT:', error);
    return null;
  }
}

/**
 * Check if JWT token is expired
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJWT(token);
  if (!payload) return true;

  const now = Math.floor(Date.now() / 1000);
  return payload.exp < now;
}

/**
 * Get user from JWT token
 */
export function getUserFromToken(token: string | undefined): User | null {
  if (!token) return null;

  if (isTokenExpired(token)) {
    return null;
  }

  const payload = decodeJWT(token);
  if (!payload) return null;

  return {
    email: payload.sub,
    role: payload.role,
    fullName: payload.fullName,
    googleId: payload.googleId,
    isAuthenticated: true,
  };
}

/**
 * Check if user has required role
 */
export function hasRole(user: User | null, requiredRole: string | string[]): boolean {
  if (!user) return false;

  const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
  return roles.includes(user.role);
}

/**
 * Role hierarchy check - if user role is higher or equal
 */
const ROLE_HIERARCHY = {
  GUEST: 0,
  USER: 1,
  MODERATOR: 2,
  ADMIN: 3,
};

export function hasMinimumRole(user: User | null, minimumRole: string): boolean {
  if (!user) return false;

  const userLevel = ROLE_HIERARCHY[user.role as keyof typeof ROLE_HIERARCHY] ?? -1;
  const requiredLevel = ROLE_HIERARCHY[minimumRole as keyof typeof ROLE_HIERARCHY] ?? 999;

  return userLevel >= requiredLevel;
}

/**
 * Get token from cookies (server-side)
 */
export function getTokenFromCookies(cookies: string | undefined): string | null {
  if (!cookies) return null;

  const match = cookies.match(/auth_token=([^;]+)/);
  return match ? match[1] : null;
}

/**
 * Save token to localStorage (client-side only)
 */
export function saveToken(token: string): void {
  if (typeof window !== 'undefined') {
    localStorage.setItem('auth_token', token);
    // Also set cookie for server-side access
    document.cookie = `auth_token=${token}; path=/; max-age=${7 * 24 * 60 * 60}`; // 7 days
  }
}

/**
 * Get token from localStorage (client-side only)
 */
export function getToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('auth_token');
  }
  return null;
}

/**
 * Clear token (logout)
 */
export function clearToken(): void {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('auth_token');
    document.cookie = 'auth_token=; path=/; max-age=0';
  }
}
