'use client';

import { useEffect, useState } from 'react';
import { getToken, getUserFromToken, hasMinimumRole, User } from '@/lib/auth';

export default function DashboardPage() {
  const [user, setUser] = useState<User | null>(null);
  const [stats, setStats] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getToken();
    if (token) {
      const userData = getUserFromToken(token);
      setUser(userData);
    }

    // Load statistics from public-web-service
    fetch('/api/database/stats')
      .then(res => res.json())
      .then(data => setStats(data))
      .catch(err => console.error('Failed to load stats:', err))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-4xl font-bold text-gray-800 mb-8">
        Welcome, {user?.fullName || user?.email}!
      </h1>

      {/* Role-based welcome message */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-xl p-8 mb-8 shadow-lg">
        {user?.role === 'GUEST' && (
          <>
            <h2 className="text-2xl font-bold mb-2">👋 Welcome to WHO Cloud!</h2>
            <p className="text-lg">You have <strong>Guest</strong> access. Contact an administrator to upgrade your permissions.</p>
          </>
        )}
        {user?.role === 'USER' && (
          <>
            <h2 className="text-2xl font-bold mb-2">✨ Welcome back!</h2>
            <p className="text-lg">You have <strong>User</strong> access. You can view and manage equipment and custodians.</p>
          </>
        )}
        {user?.role === 'MODERATOR' && (
          <>
            <h2 className="text-2xl font-bold mb-2">📊 Moderator Dashboard</h2>
            <p className="text-lg">You have <strong>Moderator</strong> access. You can generate reports and moderate content.</p>
          </>
        )}
        {user?.role === 'ADMIN' && (
          <>
            <h2 className="text-2xl font-bold mb-2">⚙️ Administrator Dashboard</h2>
            <p className="text-lg">You have <strong>Full Admin</strong> access to all system features.</p>
          </>
        )}
      </div>

      {/* Statistics cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Equipment</p>
                <p className="text-3xl font-bold text-blue-600">{stats.equipmentCount || 0}</p>
              </div>
              <div className="text-5xl">💼</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Categories</p>
                <p className="text-3xl font-bold text-green-600">{stats.categoriesCount || 0}</p>
              </div>
              <div className="text-5xl">📁</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Custodians</p>
                <p className="text-3xl font-bold text-purple-600">{stats.custodiansCount || 0}</p>
              </div>
              <div className="text-5xl">👥</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">History</p>
                <p className="text-3xl font-bold text-orange-600">{stats.historyCount || 0}</p>
              </div>
              <div className="text-5xl">📜</div>
            </div>
          </div>
        </div>
      )}

      {/* Quick actions based on role */}
      {user && hasMinimumRole(user, 'USER') && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <button className="px-6 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold">
              ➕ Add Equipment
            </button>
            <button className="px-6 py-4 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-semibold">
              👤 Add Custodian
            </button>
            {user && hasMinimumRole(user, 'MODERATOR') && (
              <button className="px-6 py-4 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-semibold">
                📊 Generate Report
              </button>
            )}
          </div>
        </div>
      )}

      {/* Guest message */}
      {user?.role === 'GUEST' && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-6 rounded-lg">
          <div className="flex items-start">
            <div className="text-3xl mr-4">ℹ️</div>
            <div>
              <h3 className="text-lg font-semibold text-yellow-800 mb-2">Limited Access</h3>
              <p className="text-yellow-700">
                Your account has guest permissions. To access equipment management features, 
                please contact your system administrator to upgrade your role.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
