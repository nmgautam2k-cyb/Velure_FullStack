"use client";

import { useEffect, useState } from "react";
import { ApiSummary, apiFetch, errorMessage } from "../../lib/api";

export default function AdminDashboard() {
  const [summary, setSummary] = useState<ApiSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        // One aggregate query on the server instead of downloading every product, order and user.
        setSummary(await apiFetch<ApiSummary>("/api/admin/summary"));
      } catch (err) {
        console.error("Failed to fetch dashboard data:", err);
        setError(errorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="admin-panel admin-loading">
        <div className="spinner" style={{ width: 20, height: 20 }} />
        Loading dashboard…
      </div>
    );
  }

  if (error || !summary) {
    return <div className="admin-error-box">⚠️ {error || "Could not load the dashboard."}</div>;
  }

  const s = summary;
  const categories = [
    { label: "T-Shirts", value: s.productsByCategory.tshirts ?? 0 },
    { label: "Shirts", value: s.productsByCategory.shirts ?? 0 },
    { label: "Pants", value: s.productsByCategory.pants ?? 0 },
  ];

  const orderStatuses = [
    { label: "Pending", value: s.ordersByStatus.pending ?? 0, cls: "admin-badge-pending" },
    { label: "Processing", value: s.ordersByStatus.processing ?? 0, cls: "admin-badge-processing" },
    { label: "Shipped", value: s.ordersByStatus.shipped ?? 0, cls: "admin-badge-shipped" },
    { label: "Delivered", value: s.ordersByStatus.delivered ?? 0, cls: "admin-badge-delivered" },
    { label: "Cancelled", value: s.ordersByStatus.cancelled ?? 0, cls: "admin-badge-cancelled" },
  ];

  const stats = [
    { label: "Total Products", value: s.totalProducts, icon: "🛍️" },
    { label: "Total Orders", value: s.totalOrders, icon: "📦" },
    { label: "Total Users", value: s.totalUsers, icon: "👥" },
    { label: "Total Revenue", value: `₹${Number(s.totalRevenue).toLocaleString("en-IN")}`, icon: "💰" },
  ];

  return (
    <div>
      <div className="admin-stats-grid">
        {stats.map((stat) => (
          <div key={stat.label} className="admin-stat-card">
            <div className="admin-stat-icon">{stat.icon}</div>
            <div className="admin-stat-label">{stat.label}</div>
            <div className="admin-stat-value">{stat.value}</div>
          </div>
        ))}
      </div>

      <div className="admin-panel-grid">
        <div className="admin-panel">
          <div className="admin-panel-title">Products by Category</div>
          {categories.map((c) => (
            <div key={c.label} className="admin-row">
              <span>{c.label}</span>
              <strong>{c.value}</strong>
            </div>
          ))}
        </div>

        <div className="admin-panel">
          <div className="admin-panel-title">Orders by Status</div>
          {orderStatuses.map((o) => (
            <div key={o.label} className="admin-row">
              <span>{o.label}</span>
              <span className={`admin-badge ${o.cls}`}>{o.value}</span>
            </div>
          ))}
        </div>

        <div className="admin-panel">
          <div className="admin-panel-title">Users by Role</div>
          <div className="admin-row">
            <span>Admin Users</span>
            <span className="admin-badge admin-badge-admin">{s.adminUsers}</span>
          </div>
          <div className="admin-row">
            <span>Customer Users</span>
            <span className="admin-badge admin-badge-customer">{s.customerUsers}</span>
          </div>
        </div>

        <div className="admin-panel">
          <div className="admin-panel-title">Quick Actions</div>
          <div className="admin-quick-links">
            <a href="/admin/products" className="admin-quick-link">Manage Products</a>
            <a href="/admin/orders" className="admin-quick-link secondary">Manage Orders</a>
            <a href="/admin/users" className="admin-quick-link tertiary">Manage Users</a>
          </div>
        </div>
      </div>
    </div>
  );
}
