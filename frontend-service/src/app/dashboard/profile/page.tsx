'use client';

import { useEffect, useState } from 'react';
import { getToken, getUserFromToken, User } from '@/lib/auth';

export default function ProfilePage() {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string>('');
  const [decodedToken, setDecodedToken] = useState<any>(null);
  const [authInfo, setAuthInfo] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const jwtToken = getToken();
    if (jwtToken) {
      setToken(jwtToken);
      const userData = getUserFromToken(jwtToken);
      setUser(userData);

      // Decode full JWT payload for debug info
      try {
        const parts = jwtToken.split('.');
        if (parts.length === 3) {
          const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
          setDecodedToken(payload);
        }
      } catch (error) {
        console.error('Failed to decode token:', error);
      }

      // Fetch detailed auth info from auth-service
      fetch('/api/auth/user-info', {
        headers: {
          'Authorization': `Bearer ${jwtToken}`
        }
      })
        .then(res => res.json())
        .then(data => {
          // Handle ApiResponse wrapper
          if (data.success && data.data) {
            setAuthInfo(data.data);
          } else {
            setAuthInfo(data);
          }
        })
        .catch(err => console.error('Failed to load auth info:', err))
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="bg-red-50 border-l-4 border-red-400 p-6 rounded-lg">
        <p className="text-red-700">Not authenticated</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-4xl font-bold text-gray-800 mb-8">User Profile</h1>

      {/* User info card */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Account Information</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-500 font-semibold">Full Name</p>
            <p className="text-lg text-gray-800">{user.fullName || 'N/A'}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 font-semibold">Email</p>
            <p className="text-lg text-gray-800">{user.email}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500 font-semibold">Role</p>
            <p className="text-lg">
              <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
                user.role === 'ADMIN' ? 'bg-red-100 text-red-800' :
                user.role === 'MODERATOR' ? 'bg-purple-100 text-purple-800' :
                user.role === 'USER' ? 'bg-blue-100 text-blue-800' :
                'bg-gray-100 text-gray-800'
              }`}>
                {user.role}
              </span>
            </p>
          </div>
          <div>
            <p className="text-sm text-gray-500 font-semibold">Google ID</p>
            <p className="text-lg text-gray-800 font-mono text-xs">{user.googleId || 'N/A'}</p>
          </div>
        </div>
      </div>

      {/* OAuth info */}
      {authInfo && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">OAuth Details</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-500 font-semibold">Provider</p>
              <p className="text-lg text-gray-800">{authInfo.provider || 'Google'}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 font-semibold">Account Created</p>
              <p className="text-lg text-gray-800">
                {authInfo.createdAt ? new Date(authInfo.createdAt).toLocaleDateString() : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 font-semibold">Last Login</p>
              <p className="text-lg text-gray-800">
                {authInfo.lastLogin ? new Date(authInfo.lastLogin).toLocaleString() : 'N/A'}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 font-semibold">Email Verified</p>
              <p className="text-lg text-gray-800">
                {authInfo.emailVerified ? '✅ Yes' : '❌ No'}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* JWT Debug info */}
      <div className="bg-gray-900 rounded-xl shadow-lg p-6 text-white">
        <h2 className="text-2xl font-bold mb-4">🔐 JWT Debug Information</h2>
        
        <div className="mb-4">
          <p className="text-sm text-gray-400 font-semibold mb-2">Decoded Payload:</p>
          <pre className="bg-gray-800 p-4 rounded-lg overflow-auto text-xs">
            {JSON.stringify(decodedToken, null, 2)}
          </pre>
        </div>

        <div>
          <p className="text-sm text-gray-400 font-semibold mb-2">Raw Token:</p>
          <div className="bg-gray-800 p-4 rounded-lg overflow-auto">
            <p className="text-xs font-mono break-all">{token}</p>
          </div>
        </div>

        {decodedToken && (
          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-400 font-semibold">Issued At (iat)</p>
              <p className="text-sm">{new Date(decodedToken.iat * 1000).toLocaleString()}</p>
            </div>
            <div>
              <p className="text-sm text-gray-400 font-semibold">Expires At (exp)</p>
              <p className="text-sm">{new Date(decodedToken.exp * 1000).toLocaleString()}</p>
            </div>
          </div>
        )}
      </div>

      {/* Permissions */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Permissions</h2>
        <div className="space-y-2">
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <span className="font-semibold">View Equipment</span>
            <span>{user.role !== 'GUEST' ? '✅' : '❌'}</span>
          </div>
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <span className="font-semibold">Edit Equipment</span>
            <span>{['USER', 'MODERATOR', 'ADMIN'].includes(user.role) ? '✅' : '❌'}</span>
          </div>
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <span className="font-semibold">Generate Reports</span>
            <span>{['MODERATOR', 'ADMIN'].includes(user.role) ? '✅' : '❌'}</span>
          </div>
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <span className="font-semibold">Admin Panel Access</span>
            <span>{user.role === 'ADMIN' ? '✅' : '❌'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
