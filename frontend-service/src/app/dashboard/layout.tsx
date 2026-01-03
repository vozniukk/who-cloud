'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { getToken, getUserFromToken, clearToken, hasMinimumRole, User } from '@/lib/auth';

export default function DashboardLayout({
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
    }
    setIsLoading(false);
  }, []);

  const handleLogout = () => {
    clearToken();
    router.push('/');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-gradient-to-b from-blue-800 to-blue-900 text-white flex flex-col shadow-2xl">
        <div className="p-6 border-b border-blue-700">
          <h1 className="text-2xl font-bold">WHO Cloud</h1>
          <p className="text-blue-300 text-sm mt-1">Equipment Manager</p>
        </div>

        <nav className="flex-1 p-4 space-y-2">
          <Link href="/dashboard" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
            <span className="text-lg">🏠 Dashboard</span>
          </Link>

          {user && hasMinimumRole(user, 'USER') && (
            <>
              <Link href="/dashboard/equipment" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
                <span className="text-lg">💼 Equipment</span>
              </Link>
              <Link href="/dashboard/custodians" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
                <span className="text-lg">👥 Custodians</span>
              </Link>
              <Link href="/dashboard/custodian-statuses" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
                <span className="text-lg">🏷️ Custodian Statuses</span>
              </Link>
              <Link href="/dashboard/contract-types" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
                <span className="text-lg">📄 Contract Types</span>
              </Link>
            </>
          )}

          {user && hasMinimumRole(user, 'MODERATOR') && (
            <Link href="/dashboard/reports" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
              <span className="text-lg">📊 Reports</span>
            </Link>
          )}

          {user && user.role === 'ADMIN' && (
            <Link href="/admin" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
              <span className="text-lg">⚙️ Admin Panel</span>
            </Link>
          )}

          <Link href="/dashboard/profile" className="block px-4 py-3 rounded-lg hover:bg-blue-700 transition-colors">
            <span className="text-lg">👤 Profile</span>
          </Link>
        </nav>

        {/* User info */}
        <div className="p-4 border-t border-blue-700">
          {user && (
            <div className="mb-4">
              <p className="text-sm text-blue-300">Logged in as:</p>
              <p className="font-semibold truncate">{user.fullName || user.email}</p>
              <p className="text-xs text-blue-400 mt-1">Role: {user.role}</p>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="w-full px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg transition-colors font-semibold"
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
