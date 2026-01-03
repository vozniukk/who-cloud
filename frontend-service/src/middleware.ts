import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { getTokenFromCookies, getUserFromToken, hasRole } from './lib/auth';

/**
 * Middleware для защиты роутов по ролям
 */
export function middleware(request: NextRequest) {
  const token = request.cookies.get('auth_token')?.value;
  const user = getUserFromToken(token);

  const pathname = request.nextUrl.pathname;

  // Protected routes that require authentication
  const protectedRoutes = ['/dashboard', '/admin', '/profile'];
  const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route));

  // If accessing protected route without token, redirect to home
  if (isProtectedRoute && !user) {
    const url = request.nextUrl.clone();
    url.pathname = '/';
    url.searchParams.set('error', 'auth_required');
    return NextResponse.redirect(url);
  }

  // Admin-only routes
  if (pathname.startsWith('/admin')) {
    if (!hasRole(user, 'ADMIN')) {
      const url = request.nextUrl.clone();
      url.pathname = '/dashboard';
      url.searchParams.set('error', 'insufficient_permissions');
      return NextResponse.redirect(url);
    }
  }

  // If authenticated user tries to access home, redirect to dashboard
  if (pathname === '/' && user && request.nextUrl.searchParams.get('token')) {
    const url = request.nextUrl.clone();
    url.pathname = '/dashboard';
    url.searchParams.delete('token');
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

// Configure which routes use this middleware
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    '/((?!_next/static|_next/image|favicon.ico|public).*)',
  ],
};
