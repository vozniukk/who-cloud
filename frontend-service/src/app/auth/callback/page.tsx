'use client';

import { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { saveToken, getUserFromToken } from '@/lib/auth';

function AuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [status, setStatus] = useState<'processing' | 'success' | 'error'>('processing');
  const [message, setMessage] = useState('Processing authentication...');

  useEffect(() => {
    // Get token from query params (sent by auth-service after OAuth success)
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (error) {
      setStatus('error');
      setMessage(`Authentication failed: ${error}`);
      setTimeout(() => router.push('/'), 3000);
      return;
    }

    if (!token) {
      setStatus('error');
      setMessage('No authentication token received');
      setTimeout(() => router.push('/'), 3000);
      return;
    }

    // Save token and redirect based on role
    try {
      saveToken(token);
      const user = getUserFromToken(token);

      if (!user) {
        throw new Error('Invalid token');
      }

      setStatus('success');
      setMessage(`Welcome, ${user.fullName || user.email}!`);

      // Redirect based on role
      setTimeout(() => {
        if (user.role === 'ADMIN') {
          router.push('/admin');
        } else {
          router.push('/dashboard');
        }
      }, 1500);

    } catch (error) {
      console.error('Auth callback error:', error);
      setStatus('error');
      setMessage('Failed to process authentication');
      setTimeout(() => router.push('/'), 3000);
    }
  }, [searchParams, router]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 to-blue-500 flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-2xl p-12 max-w-md w-full text-center">
        {status === 'processing' && (
          <>
            <div className="animate-spin rounded-full h-20 w-20 border-b-4 border-purple-600 mx-auto mb-6"></div>
            <h1 className="text-2xl font-bold text-gray-800 mb-2">Authenticating...</h1>
            <p className="text-gray-600">{message}</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="text-6xl mb-6">✅</div>
            <h1 className="text-3xl font-bold text-green-600 mb-2">Success!</h1>
            <p className="text-gray-700 text-lg">{message}</p>
            <p className="text-gray-500 mt-4">Redirecting to your dashboard...</p>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="text-6xl mb-6">❌</div>
            <h1 className="text-3xl font-bold text-red-600 mb-2">Error</h1>
            <p className="text-gray-700 text-lg">{message}</p>
            <p className="text-gray-500 mt-4">Redirecting to home page...</p>
          </>
        )}
      </div>
    </div>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gradient-to-br from-purple-600 to-blue-500 flex items-center justify-center">
        <div className="bg-white rounded-2xl shadow-2xl p-12 max-w-md w-full text-center">
          <div className="animate-spin rounded-full h-20 w-20 border-b-4 border-purple-600 mx-auto mb-6"></div>
          <h1 className="text-2xl font-bold text-gray-800 mb-2">Loading...</h1>
        </div>
      </div>
    }>
      <AuthCallbackContent />
    </Suspense>
  );
}
