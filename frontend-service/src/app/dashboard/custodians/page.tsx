'use client';

import { useEffect, useState } from 'react';
import { getToken } from '@/lib/auth';

interface Custodian {
  id: number;
  authUserId: number | null;
  firstName: string;
  lastName: string;
  fullName: string;
  position: string;
  phone: string;
  identificationNumber: string;
  hireDate: string;
  contractEndDate: string | null;
  contractTypeId: number;
  contractTypeName: string;
  statusId: number;
  statusName: string;
  equipmentCount: number;
  createdAt: string;
  updatedAt: string | null;
}

interface ContractType {
  id: number;
  name: string;
}

interface CustodianStatus {
  id: number;
  name: string;
  description: string;
}

export default function CustodiansPage() {
  const [custodians, setCustodians] = useState<Custodian[]>([]);
  const [contractTypes, setContractTypes] = useState<ContractType[]>([]);
  const [statuses, setStatuses] = useState<CustodianStatus[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState<number | null>(null);

  // Form state for creating/editing custodian
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    position: '',
    phone: '',
    identificationNumber: '',
    hireDate: '',
    contractEndDate: '',
    contractTypeId: '',
    statusId: '',
  });

  // Quick add modals
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [showContractTypeModal, setShowContractTypeModal] = useState(false);
  const [quickAddStatusName, setQuickAddStatusName] = useState('');
  const [quickAddContractTypeName, setQuickAddContractTypeName] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const token = getToken();
      
      if (!token) {
        setError('Not authenticated');
        return;
      }

      // Load custodians
      const custodiansResponse = await fetch('http://localhost:8080/api/business-1/custodians?page=0&size=100', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!custodiansResponse.ok) {
        throw new Error('Failed to load custodians');
      }

      const custodiansData = await custodiansResponse.json();
      setCustodians(custodiansData.content || []);

      // Load contract types
      const contractTypesResponse = await fetch('http://localhost:8080/api/business-1/contract-types', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (contractTypesResponse.ok) {
        const contractTypesData = await contractTypesResponse.json();
        setContractTypes(contractTypesData || []);
      }

      // Load statuses
      const statusesResponse = await fetch('http://localhost:8080/api/business-1/custodian-statuses', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (statusesResponse.ok) {
        const statusesData = await statusesResponse.json();
        setStatuses(statusesData || []);
      }

      setError(null);
    } catch (err) {
      console.error('Error loading data:', err);
      setError('Failed to load data');
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
        ? `http://localhost:8080/api/business-1/custodians/${editingId}`
        : 'http://localhost:8080/api/business-1/custodians';
      
      const method = editingId ? 'PUT' : 'POST';

      const payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        position: formData.position || null,
        phone: formData.phone || null,
        identificationNumber: formData.identificationNumber,
        hireDate: formData.hireDate,
        contractEndDate: formData.contractEndDate || null,
        contractTypeId: parseInt(formData.contractTypeId),
        statusId: parseInt(formData.statusId),
      };

      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to save custodian');
      }

      setSuccessMessage(editingId ? 'Custodian updated successfully' : 'Custodian created successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      
      setShowForm(false);
      setEditingId(null);
      resetForm();
      await loadData();
    } catch (err: any) {
      console.error('Error saving custodian:', err);
      setError(err.message || 'Failed to save custodian');
      setTimeout(() => setError(null), 5000);
    }
  };

  const handleEdit = (custodian: Custodian) => {
    setEditingId(custodian.id);
    setFormData({
      firstName: custodian.firstName,
      lastName: custodian.lastName,
      position: custodian.position || '',
      phone: custodian.phone || '',
      identificationNumber: custodian.identificationNumber,
      hireDate: custodian.hireDate,
      contractEndDate: custodian.contractEndDate || '',
      contractTypeId: custodian.contractTypeId.toString(),
      statusId: custodian.statusId.toString(),
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

      const response = await fetch(`http://localhost:8080/api/business-1/custodians/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to deactivate custodian');
      }

      setSuccessMessage('Custodian deactivated successfully');
      setTimeout(() => setSuccessMessage(null), 3000);
      setDeleteConfirmId(null);
      await loadData();
    } catch (err) {
      console.error('Error deleting custodian:', err);
      setError('Failed to deactivate custodian');
      setTimeout(() => setError(null), 5000);
    }
  };

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      position: '',
      phone: '',
      identificationNumber: '',
      hireDate: '',
      contractEndDate: '',
      contractTypeId: '',
      statusId: '',
    });
    setEditingId(null);
  };

  const handleQuickAddStatus = async () => {
    if (!quickAddStatusName.trim()) return;
    
    try {
      const token = getToken();
      if (!token) {
        setError('Not authenticated');
        return;
      }

      const response = await fetch('http://localhost:8080/api/business-1/custodian-statuses', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: quickAddStatusName })
      });

      if (!response.ok) {
        throw new Error('Failed to create status');
      }

      const newStatus = await response.json();
      setStatuses([...statuses, newStatus]);
      setFormData({ ...formData, statusId: newStatus.id.toString() });
      setQuickAddStatusName('');
      setShowStatusModal(false);
      setSuccessMessage('Status created and selected!');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error creating status:', err);
      setError('Failed to create status');
      setTimeout(() => setError(null), 5000);
    }
  };

  const handleQuickAddContractType = async () => {
    if (!quickAddContractTypeName.trim()) return;
    
    try {
      const token = getToken();
      if (!token) {
        setError('Not authenticated');
        return;
      }

      const response = await fetch('http://localhost:8080/api/business-1/contract-types', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: quickAddContractTypeName })
      });

      if (!response.ok) {
        throw new Error('Failed to create contract type');
      }

      const newContractType = await response.json();
      setContractTypes([...contractTypes, newContractType]);
      setFormData({ ...formData, contractTypeId: newContractType.id.toString() });
      setQuickAddContractTypeName('');
      setShowContractTypeModal(false);
      setSuccessMessage('Contract type created and selected!');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error('Error creating contract type:', err);
      setError('Failed to create contract type');
      setTimeout(() => setError(null), 5000);
    }
  };

  const getStatusBadgeColor = (statusName: string) => {
    switch (statusName.toLowerCase()) {
      case 'active':
        return 'bg-green-100 text-green-800';
      case 'on leave':
        return 'bg-yellow-100 text-yellow-800';
      case 'terminated':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
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
          <h1 className="text-4xl font-bold text-gray-800">Custodian Management</h1>
          <p className="text-gray-600 mt-2">Manage equipment custodians and their details</p>
        </div>
        <button
          onClick={() => {
            resetForm();
            setShowForm(!showForm);
          }}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
        >
          {showForm ? '✕ Cancel' : '+ Add Custodian'}
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
            {editingId ? 'Edit Custodian' : 'Create New Custodian'}
          </h2>
          <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                First Name *
              </label>
              <input
                type="text"
                required
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter first name"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Last Name *
              </label>
              <input
                type="text"
                required
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter last name"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Identification Number *
              </label>
              <input
                type="text"
                required
                value={formData.identificationNumber}
                onChange={(e) => setFormData({ ...formData, identificationNumber: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter ID number"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Position
              </label>
              <input
                type="text"
                value={formData.position}
                onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter position"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Phone
              </label>
              <input
                type="tel"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter phone number"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Hire Date *
              </label>
              <input
                type="date"
                required
                value={formData.hireDate}
                onChange={(e) => setFormData({ ...formData, hireDate: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Contract End Date
              </label>
              <input
                type="date"
                value={formData.contractEndDate}
                onChange={(e) => setFormData({ ...formData, contractEndDate: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-semibold text-gray-700">
                  Contract Type *
                </label>
                <button
                  type="button"
                  onClick={() => setShowContractTypeModal(true)}
                  className="text-xs text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1"
                >
                  <span>+</span> Add New
                </button>
              </div>
              <select
                required
                value={formData.contractTypeId}
                onChange={(e) => setFormData({ ...formData, contractTypeId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select contract type</option>
                {contractTypes.map(type => (
                  <option key={type.id} value={type.id}>{type.name}</option>
                ))}
              </select>
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="block text-sm font-semibold text-gray-700">
                  Status *
                </label>
                <button
                  type="button"
                  onClick={() => setShowStatusModal(true)}
                  className="text-xs text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1"
                >
                  <span>+</span> Add New
                </button>
              </div>
              <select
                required
                value={formData.statusId}
                onChange={(e) => setFormData({ ...formData, statusId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Select status</option>
                {statuses.map(status => (
                  <option key={status.id} value={status.id}>{status.name}</option>
                ))}
              </select>
            </div>

            <div className="md:col-span-2 flex justify-end gap-4">
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
                {editingId ? 'Update Custodian' : 'Create Custodian'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm font-semibold uppercase">Total Custodians</p>
              <p className="text-3xl font-bold text-blue-600">{custodians.length}</p>
            </div>
            <div className="text-5xl">👥</div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm font-semibold uppercase">Active</p>
              <p className="text-3xl font-bold text-green-600">
                {custodians.filter(c => c.statusName.toLowerCase() === 'active').length}
              </p>
            </div>
            <div className="text-5xl">✓</div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm font-semibold uppercase">On Leave</p>
              <p className="text-3xl font-bold text-yellow-600">
                {custodians.filter(c => c.statusName.toLowerCase() === 'on leave').length}
              </p>
            </div>
            <div className="text-5xl">⏸</div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-500 text-sm font-semibold uppercase">Total Equipment</p>
              <p className="text-3xl font-bold text-purple-600">
                {custodians.reduce((sum, c) => sum + (c.equipmentCount || 0), 0)}
              </p>
            </div>
            <div className="text-5xl">💼</div>
          </div>
        </div>
      </div>

      {/* Custodians Table */}
      <div className="bg-white rounded-xl shadow-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Full Name</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">ID Number</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Position</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Phone</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Contract Type</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Equipment</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {custodians.length === 0 ? (
                <tr>
                  <td colSpan={9} className="px-6 py-12 text-center text-gray-500">
                    <div className="text-6xl mb-4">📋</div>
                    <p className="text-lg font-semibold">No custodians found</p>
                    <p className="text-sm mt-2">Click "Add Custodian" to create the first one</p>
                  </td>
                </tr>
              ) : (
                custodians.map((custodian) => (
                  <tr key={custodian.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      #{custodian.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{custodian.fullName}</div>
                      {custodian.authUserId && (
                        <div className="text-xs text-gray-500">Auth ID: {custodian.authUserId}</div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {custodian.identificationNumber}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {custodian.position || '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {custodian.phone || '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {custodian.contractTypeName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadgeColor(custodian.statusName)}`}>
                        {custodian.statusName}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span className="font-semibold">{custodian.equipmentCount || 0}</span> items
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleEdit(custodian)}
                          className="text-blue-600 hover:text-blue-900 font-semibold"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => setDeleteConfirmId(custodian.id)}
                          className="text-red-600 hover:text-red-900 font-semibold"
                        >
                          Deactivate
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
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Deactivate Custodian?</h3>
              <p className="text-gray-600 mb-6">
                This will deactivate the custodian. Equipment assigned to this custodian will remain unchanged.
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
                  Deactivate
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Add Status Modal */}
      {showStatusModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
            <h3 className="text-2xl font-bold text-gray-900 mb-4">Add New Status</h3>
            <p className="text-gray-600 mb-6">Create a new custodian status and it will be automatically selected.</p>
            <input
              type="text"
              value={quickAddStatusName}
              onChange={(e) => setQuickAddStatusName(e.target.value)}
              placeholder="Enter status name (e.g., Active, On Leave)"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent mb-6"
              onKeyDown={(e) => {
                if (e.key === 'Enter' && quickAddStatusName.trim()) {
                  handleQuickAddStatus();
                }
              }}
            />
            <div className="flex gap-4 justify-end">
              <button
                onClick={() => {
                  setShowStatusModal(false);
                  setQuickAddStatusName('');
                }}
                className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-semibold"
              >
                Cancel
              </button>
              <button
                onClick={handleQuickAddStatus}
                disabled={!quickAddStatusName.trim()}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Create & Select
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Quick Add Contract Type Modal */}
      {showContractTypeModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
            <h3 className="text-2xl font-bold text-gray-900 mb-4">Add New Contract Type</h3>
            <p className="text-gray-600 mb-6">Create a new contract type and it will be automatically selected.</p>
            <input
              type="text"
              value={quickAddContractTypeName}
              onChange={(e) => setQuickAddContractTypeName(e.target.value)}
              placeholder="Enter contract type (e.g., Permanent, Temporary)"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent mb-6"
              onKeyDown={(e) => {
                if (e.key === 'Enter' && quickAddContractTypeName.trim()) {
                  handleQuickAddContractType();
                }
              }}
            />
            <div className="flex gap-4 justify-end">
              <button
                onClick={() => {
                  setShowContractTypeModal(false);
                  setQuickAddContractTypeName('');
                }}
                className="px-6 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-semibold"
              >
                Cancel
              </button>
              <button
                onClick={handleQuickAddContractType}
                disabled={!quickAddContractTypeName.trim()}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Create & Select
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
