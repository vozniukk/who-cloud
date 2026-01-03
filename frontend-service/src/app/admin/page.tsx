'use client';

import { useEffect, useState } from 'react';
import { getToken } from '@/lib/auth';

export default function AdminPage() {
  const [stats, setStats] = useState<any>(null);
  const [users, setUsers] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = getToken();

    // Load system stats
    Promise.all([
      fetch('/api/database/stats').then(res => res.json()),
      // TODO: Add endpoint to get all users
      // fetch('/api/auth/users', {
      //   headers: { 'Authorization': `Bearer ${token}` }
      // }).then(res => res.json())
    ])
      .then(([statsData]) => {
        setStats(statsData);
        // setUsers(usersData);
      })
      .catch(err => console.error('Failed to load admin data:', err))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-4xl font-bold text-gray-800 mb-2">Admin Dashboard</h1>
        <p className="text-gray-600">System overview and management</p>
      </div>

      {/* Alert */}
      <div className="bg-red-50 border-l-4 border-red-400 p-6 rounded-lg">
        <div className="flex items-start">
          <div className="text-3xl mr-4">⚠️</div>
          <div>
            <h3 className="text-lg font-semibold text-red-800 mb-2">Administrator Access</h3>
            <p className="text-red-700">
              You have full system access. Please be careful when making changes.
            </p>
          </div>
        </div>
      </div>

      {/* System stats */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Total Equipment</p>
                <p className="text-3xl font-bold text-blue-600">{stats.equipmentCount || 0}</p>
              </div>
              <div className="text-5xl">💼</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Total Users</p>
                <p className="text-3xl font-bold text-green-600">{users.length || 0}</p>
              </div>
              <div className="text-5xl">👥</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">Custodians</p>
                <p className="text-3xl font-bold text-purple-600">{stats.custodiansCount || 0}</p>
              </div>
              <div className="text-5xl">📋</div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-500 text-sm font-semibold uppercase">History Records</p>
                <p className="text-3xl font-bold text-orange-600">{stats.historyCount || 0}</p>
              </div>
              <div className="text-5xl">📜</div>
            </div>
          </div>
        </div>
      )}

      {/* Quick actions */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <button className="px-6 py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold">
            ➕ Add New User
          </button>
          <button className="px-6 py-4 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-semibold">
            🔐 Manage Roles
          </button>
          <button className="px-6 py-4 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors font-semibold">
            ⚙️ System Settings
          </button>
          <button className="px-6 py-4 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-semibold">
            📜 View Audit Logs
          </button>
          <button className="px-6 py-4 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-colors font-semibold">
            🗄️ Database Backup
          </button>
          <button className="px-6 py-4 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors font-semibold">
            📊 Generate System Report
          </button>
        </div>
      </div>

      {/* Recent activity */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Recent Activity</h2>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center space-x-4">
              <div className="text-2xl">👤</div>
              <div>
                <p className="font-semibold">New user registered</p>
                <p className="text-sm text-gray-500">user@example.com - 2 hours ago</p>
              </div>
            </div>
            <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-semibold">GUEST</span>
          </div>
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center space-x-4">
              <div className="text-2xl">💼</div>
              <div>
                <p className="font-semibold">Equipment added</p>
                <p className="text-sm text-gray-500">Laptop Dell XPS 15 - 5 hours ago</p>
              </div>
            </div>
            <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-semibold">NEW</span>
          </div>
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center space-x-4">
              <div className="text-2xl">🔐</div>
              <div>
                <p className="font-semibold">Role updated</p>
                <p className="text-sm text-gray-500">john.doe@example.com: GUEST → USER - 1 day ago</p>
              </div>
            </div>
            <span className="px-3 py-1 bg-purple-100 text-purple-800 rounded-full text-sm font-semibold">UPDATED</span>
          </div>
        </div>
      </div>

      {/* System health */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">System Health</h2>
        <div className="space-y-4">
          <div>
            <div className="flex justify-between mb-2">
              <span className="font-semibold">Database</span>
              <span className="text-green-600 font-semibold">✅ Online</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: '95%' }}></div>
            </div>
          </div>
          <div>
            <div className="flex justify-between mb-2">
              <span className="font-semibold">Auth Service</span>
              <span className="text-green-600 font-semibold">✅ Online</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: '98%' }}></div>
            </div>
          </div>
          <div>
            <div className="flex justify-between mb-2">
              <span className="font-semibold">Business Services</span>
              <span className="text-green-600 font-semibold">✅ Online</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: '92%' }}></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
