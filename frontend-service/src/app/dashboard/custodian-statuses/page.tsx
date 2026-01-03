'use client';

import { useEffect, useState } from 'react';
import { getToken } from '@/lib/auth';

interface CustodianStatus {
  id: number;
  name: string;
  translations?: Record<string, string>;
  createdAt: string;
  updatedAt: string | null;
}

export default function CustodianStatusesPage() {
  const [statuses, setStatuses] = useState<CustodianStatus[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  // Form state
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    name: '',
  });

  useEffect(() => {
    loadStatuses();
  }, []);

  const loadStatuses = async () => {
    try {
      setIsLoading(true);
      const token = getToken();
      
      if (!token) {
        setError('Not authenticated');
        return;
      }

      const response = await fetch('http://localhost:8080/api/business-1/custodian-statuses', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to load custodian statuses');
      }

      const data = await response.json();
      setStatuses(data || []);
      setError(null);
    } catch (err) {
      console.error('Error loading statuses:', err);
      setError('Failed to load custodian statuses');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const token = getToken();
      if (!token) {
        setError('Not authenticated');
        return;
      }

      const url = editingId
        ? `http://localhost:8080/api/business-1/custodian-statuses/${editingId}`
        : 'http://localhost:8080/api/business-1/custodian-statuses';
      
      const method = editingId ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: formData.name })
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to save status');
      }

      setSuccessMessage(editingId ? 'Status updated successfully' : 'Status created successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      
      setShowForm(false);
      setEditingId(null);
      resetForm();
      await loadStatuses();
    } catch (err: any) {
      console.error('Error saving status:', err);
      setError(err.message || 'Failed to save status');
      setTimeout(() => setError(null), 5000);
    }
  };

  const handleEdit = (status: CustodianStatus) => {
    setEditingId(status.id);
    setFormData({
      name: status.name,
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    try {
      const token = getToken();
      if (!token) {
        setError('Not authenticated');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/business-1/custodian-statuses/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to delete status');
      }

      setSuccessMessage('Status deleted successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      setDeleteConfirmId(null);
      await loadStatuses();
    } catch (err) {
      console.error('Error deleting status:', err);
      setError('Failed to delete status');
      setTimeout(() => setError(null), 5000);
    }
  };

  const resetForm = () => {
    setFormData({ name: '' });
    setEditingId(null);
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-4 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-4xl font-bold text-gray-800">Custodian Statuses</h1>
          <p className="text-gray-600 mt-2">Manage custodian status types</p>
        </div>
        <button
          onClick={() => {
            resetForm();
            setShowForm(!showForm);
          }}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
        >
          {showForm ? '✕ Cancel' : '+ Add Status'}
        </button>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded">
          <p className="text-green-700">✓ {successMessage}</p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
          <p className="text-red-700">✕ {error}</p>
        </div>
      )}

      {/* Create/Edit Form */}
      {showForm && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-6">
            {editingId ? 'Edit Status' : 'Create New Status'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Status Name *
              </label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter status name (e.g., Active, On Leave, Terminated)"
              />
            </div>

            <div className="flex justify-end gap-4">
              <button
                type="button"
                onClick={() => {
                  setShowForm(false);
                  resetForm();
                }}
                className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-semibold"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
              >
                {editingId ? 'Update Status' : 'Create Status'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Statuses Table */}
      <div className="bg-white rounded-xl shadow-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Name</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Created At</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Updated At</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {statuses.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-500">
                    <div className="text-6xl mb-4">🏷️</div>
                    <p className="text-lg font-semibold">No statuses found</p>
                    <p className="text-sm mt-2">Click "Add Status" to create the first one</p>
                  </td>
                </tr>
              ) : (
                statuses.map((status) => (
                  <tr key={status.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      #{status.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">
                      {status.name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(status.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {status.updatedAt ? new Date(status.updatedAt).toLocaleDateString() : '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleEdit(status)}
                          className="text-blue-600 hover:text-blue-900 font-semibold"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => setDeleteConfirmId(status.id)}
                          className="text-red-600 hover:text-red-900 font-semibold"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirmId !== null && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
            <div className="text-center">
              <div className="text-6xl mb-4">⚠️</div>
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Delete Status?</h3>
              <p className="text-gray-600 mb-6">
                This will permanently delete this status. This action cannot be undone.
              </p>
              <div className="flex gap-4 justify-center">
                <button
                  onClick={() => setDeleteConfirmId(null)}
                  className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-semibold"
                >
                  Cancel
                </button>
                <button
                  onClick={() => handleDelete(deleteConfirmId)}
                  className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-semibold"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
