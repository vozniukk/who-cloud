'use client';

import { useEffect, useState } from 'react';
import { getToken } from '@/lib/auth';

interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  googleId: string | null;
  role: 'GUEST' | 'USER' | 'MODERATOR' | 'ADMIN';
  isEnabled: boolean;
  isAccountNonLocked: boolean;
  createdAt: string;
  lastLogin: string | null;
  provider: string;
}

export default function UserManagementPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const fetchUsers = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      const response = await fetch('http://localhost:8080/api/auth/admin/users', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch users');
      }

      const data = await response.json();
      if (data.success && data.data) {
        setUsers(data.data);
      } else {
        throw new Error('Invalid response format');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load users');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId: number, newRole: string) => {
    try {
      const token = getToken();
      const response = await fetch('http://localhost:8080/api/auth/admin/users/role', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId, role: newRole })
      });

      if (!response.ok) {
        throw new Error('Failed to update role');
      }

      setSuccessMessage(`Role updated successfully for user ID ${userId}`);
      setTimeout(() => setSuccessMessage(null), 3000);
      fetchUsers(); // Refresh list
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update role');
      setTimeout(() => setError(null), 3000);
    }
  };

  const handleStatusToggle = async (userId: number, currentStatus: boolean) => {
    try {
      const token = getToken();
      const response = await fetch('http://localhost:8080/api/auth/admin/users/status', {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId, enabled: !currentStatus })
      });

      if (!response.ok) {
        throw new Error('Failed to update status');
      }

      setSuccessMessage(`User ${!currentStatus ? 'enabled' : 'disabled'} successfully`);
      setTimeout(() => setSuccessMessage(null), 3000);
      fetchUsers(); // Refresh list
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update status');
      setTimeout(() => setError(null), 3000);
    }
  };

  const handleDeleteUser = async (userId: number, username: string) => {
    if (!confirm(`Are you sure you want to delete user "${username}"? This action cannot be undone.`)) {
      return;
    }

    try {
      const token = getToken();
      const response = await fetch(`http://localhost:8080/api/auth/admin/users/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to delete user');
      }

      setSuccessMessage(`User "${username}" deleted successfully`);
      setTimeout(() => setSuccessMessage(null), 3000);
      fetchUsers(); // Refresh list
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete user');
      setTimeout(() => setError(null), 3000);
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800';
      case 'MODERATOR': return 'bg-purple-100 text-purple-800';
      case 'USER': return 'bg-blue-100 text-blue-800';
      case 'GUEST': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-4xl font-bold text-gray-800">User Management</h1>
        <button
          onClick={fetchUsers}
          className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg font-semibold transition-colors"
        >
          🔄 Refresh
        </button>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded-lg">
          <p className="text-green-700">✅ {successMessage}</p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded-lg">
          <p className="text-red-700">❌ {error}</p>
        </div>
      )}

      {/* Users Count */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center gap-4">
          <div className="bg-red-100 p-4 rounded-full">
            <span className="text-3xl">👥</span>
          </div>
          <div>
            <p className="text-gray-500 text-sm font-semibold">Total Users</p>
            <p className="text-3xl font-bold text-gray-800">{users.length}</p>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-xl shadow-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-red-800 text-white">
              <tr>
                <th className="px-6 py-4 text-left font-semibold">ID</th>
                <th className="px-6 py-4 text-left font-semibold">Username</th>
                <th className="px-6 py-4 text-left font-semibold">Full Name</th>
                <th className="px-6 py-4 text-left font-semibold">Email</th>
                <th className="px-6 py-4 text-left font-semibold">Provider</th>
                <th className="px-6 py-4 text-left font-semibold">Role</th>
                <th className="px-6 py-4 text-left font-semibold">Status</th>
                <th className="px-6 py-4 text-left font-semibold">Last Login</th>
                <th className="px-6 py-4 text-center font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-sm text-gray-600">{user.id}</td>
                  <td className="px-6 py-4">
                    <div className="font-semibold text-gray-800">{user.username}</div>
                    {user.googleId && (
                      <div className="text-xs text-gray-500 font-mono">{user.googleId.substring(0, 20)}...</div>
                    )}
                  </td>
                  <td className="px-6 py-4 text-gray-800">{user.fullName}</td>
                  <td className="px-6 py-4 text-gray-600">{user.email}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded text-xs font-semibold ${
                      user.provider === 'Google' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'
                    }`}>
                      {user.provider === 'Google' ? '🌐 Google' : '🔐 Local'}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <select
                      value={user.role}
                      onChange={(e) => handleRoleChange(user.id, e.target.value)}
                      className={`px-3 py-1 rounded text-sm font-semibold border-2 ${getRoleBadgeColor(user.role)} cursor-pointer hover:opacity-80 transition-opacity`}
                    >
                      <option value="GUEST">GUEST</option>
                      <option value="USER">USER</option>
                      <option value="MODERATOR">MODERATOR</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => handleStatusToggle(user.id, user.isEnabled)}
                      className={`px-3 py-1 rounded text-sm font-semibold transition-colors ${
                        user.isEnabled
                          ? 'bg-green-100 text-green-800 hover:bg-green-200'
                          : 'bg-red-100 text-red-800 hover:bg-red-200'
                      }`}
                    >
                      {user.isEnabled ? '✅ Enabled' : '❌ Disabled'}
                    </button>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}
                  </td>
                  <td className="px-6 py-4 text-center">
                    <button
                      onClick={() => handleDeleteUser(user.id, user.username)}
                      className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm font-semibold transition-colors"
                    >
                      🗑️ Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Empty State */}
      {users.length === 0 && !isLoading && (
        <div className="bg-white rounded-xl shadow-lg p-12 text-center">
          <span className="text-6xl mb-4 block">👥</span>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">No Users Found</h2>
          <p className="text-gray-600">There are no users in the system yet.</p>
        </div>
      )}
    </div>
  );
}
