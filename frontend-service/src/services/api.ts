import { DatabaseStats } from '@/types/database';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://api-gateway:8080/api';

export async function getDatabaseStats(): Promise<DatabaseStats> {
  const response = await fetch(`${API_BASE_URL}/public/database-stats`, {
    cache: 'no-store' // Always fetch fresh data
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch database stats: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data; // ApiResponse wrapper
}
