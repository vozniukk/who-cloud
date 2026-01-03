import { getDatabaseStats } from '@/services/api';
import { DatabaseStats } from '@/types/database';
import Link from 'next/link';

export default async function Home() {
  let stats: DatabaseStats | null = null;
  let error: string | null = null;

  try {
    stats = await getDatabaseStats();
  } catch (e) {
    error = e instanceof Error ? e.message : 'Unknown error';
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 to-blue-500">
      {/* Header */}
      <header className="bg-white/95 backdrop-blur shadow-lg sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-3">
              <span className="text-3xl">🏥</span>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-purple-600 to-blue-500 bg-clip-text text-transparent">
                WHO Cloud Platform
              </h1>
            </div>
            <nav className="flex gap-6">
              <Link href="/" className="text-gray-700 hover:text-purple-600 font-medium transition">Home</Link>
              <Link href="/api/public/welcome" className="text-gray-700 hover:text-purple-600 font-medium transition">API</Link>
              <a href="/login/oauth2/authorization/google" className="bg-gradient-to-r from-purple-600 to-blue-500 text-white px-6 py-3 rounded-lg hover:shadow-lg transition font-semibold">
                🔐 Sign In with Google
              </a>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Hero Section */}
        <div className="text-center text-white mb-12">
          <h2 className="text-5xl font-bold mb-4">Database Statistics Dashboard</h2>
          <p className="text-xl opacity-90">Real-time monitoring of microservices data</p>
        </div>

        {/* Stats Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8 mb-8">
          {error ? (
            <div className="text-center py-12">
              <div className="text-6xl mb-4">⚠️</div>
              <h3 className="text-2xl font-bold text-red-600 mb-2">Error Loading Stats</h3>
              <p className="text-gray-600">{error}</p>
            </div>
          ) : stats ? (
            <>
              {/* Summary */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-6 border border-purple-200">
                  <div className="text-purple-600 text-4xl mb-2">📊</div>
                  <div className="text-3xl font-bold text-purple-900">{stats.totalTables}</div>
                  <div className="text-purple-700 font-medium">Total Tables</div>
                </div>
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 border border-blue-200">
                  <div className="text-blue-600 text-4xl mb-2">📝</div>
                  <div className="text-3xl font-bold text-blue-900">
                    {stats.tables.reduce((sum, t) => sum + t.recordCount, 0).toLocaleString()}
                  </div>
                  <div className="text-blue-700 font-medium">Total Records</div>
                </div>
                <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-6 border border-green-200">
                  <div className="text-green-600 text-4xl mb-2">🕐</div>
                  <div className="text-lg font-bold text-green-900">
                    {new Date(stats.timestamp).toLocaleTimeString()}
                  </div>
                  <div className="text-green-700 font-medium">Last Updated</div>
                </div>
              </div>

              {/* Tables List */}
              <div>
                <h3 className="text-2xl font-bold mb-4 text-gray-800">Tables Breakdown</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {stats.tables.map((table) => (
                    <div
                      key={table.tableName}
                      className="bg-gradient-to-br from-gray-50 to-gray-100 rounded-lg p-4 border border-gray-200 hover:shadow-md transition"
                    >
                      <div className="flex justify-between items-center">
                        <div>
                          <div className="font-mono text-sm text-gray-600 mb-1">TABLE</div>
                          <div className="font-bold text-gray-900">{table.tableName}</div>
                        </div>
                        <div className="text-right">
                          <div className="text-2xl font-bold text-purple-600">
                            {table.recordCount.toLocaleString()}
                          </div>
                          <div className="text-xs text-gray-500">records</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <div className="animate-spin text-6xl mb-4">⏳</div>
              <p className="text-gray-600">Loading database statistics...</p>
            </div>
          )}
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white/90 backdrop-blur rounded-xl p-6 shadow-lg">
            <div className="text-4xl mb-3">🔒</div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Secure OAuth 2.0</h3>
            <p className="text-gray-600">JWT-based authentication with Google integration</p>
          </div>
          <div className="bg-white/90 backdrop-blur rounded-xl p-6 shadow-lg">
            <div className="text-4xl mb-3">⚡</div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">12 Microservices</h3>
            <p className="text-gray-600">Spring Boot architecture with PostgreSQL</p>
          </div>
          <div className="bg-white/90 backdrop-blur rounded-xl p-6 shadow-lg">
            <div className="text-4xl mb-3">🚀</div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Next.js Frontend</h3>
            <p className="text-gray-600">Server-side rendering with TypeScript</p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-900 text-white mt-16 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p className="mb-2">© 2026 WHO Cloud Platform. All rights reserved.</p>
          <p className="text-gray-400 text-sm">
            Powered by Next.js, Spring Boot, PostgreSQL & Docker
          </p>
        </div>
      </footer>
    </div>
  );
}
