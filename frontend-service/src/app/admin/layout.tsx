'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { getToken, getUserFromToken, clearToken, User } from '@/lib/auth';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    if (token) {
      const userData = getUserFromToken(token);
      setUser(userData);
      
      // Double-check admin access
      if (userData?.role !== 'ADMIN') {
        router.push('/dashboard?error=insufficient_permissions');
        return;
      }
    } else {
      router.push('/?error=auth_required');
      return;
    }
    setIsLoading(false);
  }, [router]);

  const handleLogout = () => {
    clearToken();
    router.push('/');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-gradient-to-b from-red-800 to-red-900 text-white flex flex-col shadow-2xl">
        <div className="p-6 border-b border-red-700">
          <h1 className="text-2xl font-bold">⚙️ Admin Panel</h1>
          <p className="text-red-300 text-sm mt-1">System Management</p>
        </div>

        <nav className="flex-1 p-4 space-y-2">
          <Link href="/admin" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
            <span className="text-lg">📊 Overview</span>
          </Link>
          <Link href="/admin/users" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
            <span className="text-lg">👥 User Management</span>
          </Link>
          <Link href="/admin/roles" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
            <span className="text-lg">🔐 Roles & Permissions</span>
          </Link>
          <Link href="/admin/system" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
            <span className="text-lg">⚙️ System Settings</span>
          </Link>
          <Link href="/admin/logs" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
            <span className="text-lg">📜 Audit Logs</span>
          </Link>
          
          <div className="pt-4 border-t border-red-700">
            <Link href="/dashboard" className="block px-4 py-3 rounded-lg hover:bg-red-700 transition-colors">
              <span className="text-lg">← Back to Dashboard</span>
            </Link>
          </div>
        </nav>

        {/* User info */}
        <div className="p-4 border-t border-red-700">
          {user && (
            <div className="mb-4">
              <p className="text-sm text-red-300">Admin:</p>
              <p className="font-semibold truncate">{user.fullName || user.email}</p>
              <p className="text-xs text-red-400 mt-1">Role: {user.role}</p>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="w-full px-4 py-2 bg-gray-800 hover:bg-gray-900 rounded-lg transition-colors font-semibold"
          >
            🚪 Logout
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <div className="container mx-auto p-8">
          {children}
        </div>
      </main>
    </div>
  );
}
