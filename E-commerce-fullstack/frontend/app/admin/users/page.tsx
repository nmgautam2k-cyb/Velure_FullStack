"use client";

import { useEffect, useState } from "react";
import { ApiUser, apiFetch, errorMessage, fetchAllPages } from "../../../lib/api";

function UserCard({ user, onEdit, onDelete }: { user: ApiUser; onEdit: (u: ApiUser) => void; onDelete: (id: number) => void }) {
  return (
    <div className="admin-panel" style={{ padding: "16px" }}>
      <h4 style={{ fontSize: "14.5px", color: "#2c3e50", marginBottom: "4px" }}>{user.fullName}</h4>
      <p style={{ margin: "0 0 10px", color: "#7a8699", fontSize: "13px" }}>{user.email}</p>
      <span className={`admin-badge ${user.role === "admin" ? "admin-badge-admin" : "admin-badge-customer"}`}>
        {user.role === "admin" ? "Admin" : "Customer"}
      </span>

      <div style={{ marginTop: "12px", display: "flex", gap: "8px" }}>
        <button onClick={() => onEdit(user)} className="admin-btn admin-btn-secondary" style={{ flex: 1, padding: "8px 10px", fontSize: "13px" }}>
          Edit
        </button>
        <button onClick={() => onDelete(user.id)} className="admin-btn admin-btn-danger" style={{ flex: 1, padding: "8px 10px", fontSize: "13px" }}>
          Delete
        </button>
      </div>
    </div>
  );
}

export default function UsersPage() {
  const [users, setUsers] = useState<ApiUser[]>([]);
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    role: "customer" as "admin" | "customer"
  });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [showAddForm, setShowAddForm] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const fetchUsers = async () => {
    try {
      setUsers(await fetchAllPages<ApiUser>("/api/admin/users"));
    } catch (error) {
      setActionError(errorMessage(error));
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const resetForm = () => {
    setFormData({ name: "", email: "", password: "", role: "customer" });
    setEditingId(null);
    setShowAddForm(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setActionError(null);

    try {
      const userData: Record<string, string> = {
        fullName: formData.name,
        email: formData.email,
        role: formData.role
      };
      // On edit, a blank password means "keep the current one".
      if (formData.password) userData.password = formData.password;

      if (editingId) {
        await apiFetch(`/api/admin/users/${editingId}`, { method: "PUT", body: JSON.stringify(userData) });
      } else {
        await apiFetch("/api/admin/users", { method: "POST", body: JSON.stringify(userData) });
      }

      resetForm();
      fetchUsers();
    } catch (error) {
      setActionError(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (user: ApiUser) => {
    setFormData({
      name: user.fullName,
      email: user.email,
      password: "", // password hashes are never sent to the browser
      role: user.role
    });
    setEditingId(user.id);
    setShowAddForm(true);
  };

  const handleDelete = async (id: number) => {
    if (confirm("Are you sure you want to delete this user?")) {
      setActionError(null);
      try {
        await apiFetch(`/api/admin/users/${id}`, { method: "DELETE" });
        fetchUsers();
      } catch (error) {
        setActionError(errorMessage(error));
      }
    }
  };

  const adminUsers = users.filter(u => u.role === "admin");
  const customerUsers = users.filter(u => u.role === "customer");

  return (
    <div>
      {/* Inline error banner — replaces alert() and prevents Next.js dev overlay crash */}
      {actionError && (
        <div style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          background: "rgba(229,62,62,0.1)",
          border: "1px solid rgba(229,62,62,0.35)",
          borderRadius: "12px",
          padding: "12px 18px",
          marginBottom: "20px",
          color: "#c53030",
          fontSize: "14px",
          fontWeight: 500,
          gap: "12px",
        }}>
          <span>⚠️ {actionError}</span>
          <button
            onClick={() => setActionError(null)}
            style={{
              background: "none",
              border: "none",
              color: "#c53030",
              cursor: "pointer",
              fontSize: "18px",
              lineHeight: 1,
              padding: "0 4px",
              flexShrink: 0,
            }}
            aria-label="Dismiss error"
          >
            ×
          </button>
        </div>
      )}

      {showAddForm ? (
        <div className="admin-panel" style={{ marginBottom: "28px" }}>
          <div className="admin-panel-title">{editingId ? "Edit User" : "Add New User"}</div>

          <form onSubmit={handleSubmit}>
            <div className="admin-form-grid">
              <div className="admin-field">
                <label>Name</label>
                <input
                  className="admin-input"
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </div>

              <div className="admin-field">
                <label>Email</label>
                <input
                  className="admin-input"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>

              <div className="admin-field">
                <label>Password</label>
                <input
                  className="admin-input"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required={!editingId}
                  minLength={8}
                  placeholder={editingId ? "Leave blank to keep current password" : "At least 8 characters"}
                />
              </div>

              <div className="admin-field">
                <label>Role</label>
                <select
                  className="admin-select"
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value as "admin" | "customer" })}
                >
                  <option value="customer">Customer</option>
                  <option value="admin">Admin</option>
                </select>
              </div>
            </div>

            <div style={{ display: "flex", gap: "10px", marginTop: "6px" }}>
              <button type="submit" disabled={loading} className="admin-btn admin-btn-primary">
                {loading ? "Saving…" : (editingId ? "Update User" : "Add User")}
              </button>
              <button type="button" onClick={resetForm} className="admin-btn admin-btn-secondary">
                Cancel
              </button>
            </div>
          </form>
        </div>
      ) : (
        <button onClick={() => setShowAddForm(true)} className="admin-btn admin-btn-primary" style={{ marginBottom: "24px" }}>
          + Add New User
        </button>
      )}

      <div style={{ marginBottom: "28px" }}>
        <h3 style={{ fontSize: "16px", color: "#2c3e50", marginBottom: "14px" }}>Admin Users ({adminUsers.length})</h3>

        {adminUsers.length === 0 ? (
          <div className="admin-panel admin-empty">
            <span className="admin-empty-icon">👤</span>
            No admin users found.
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: "16px" }}>
            {adminUsers.map((user) => (
              <UserCard key={user.id} user={user} onEdit={handleEdit} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>

      <div>
        <h3 style={{ fontSize: "16px", color: "#2c3e50", marginBottom: "14px" }}>Customer Users ({customerUsers.length})</h3>

        {customerUsers.length === 0 ? (
          <div className="admin-panel admin-empty">
            <span className="admin-empty-icon">👤</span>
            No customer users found.
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: "16px" }}>
            {customerUsers.map((user) => (
              <UserCard key={user.id} user={user} onEdit={handleEdit} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
