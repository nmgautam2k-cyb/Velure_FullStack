"use client";

import { useEffect, useState } from "react";
import { ApiOrder, apiFetch, errorMessage, fetchAllPages } from "../../../lib/api";

const STATUS_BADGE: Record<string, string> = {
  pending: "admin-badge-pending",
  processing: "admin-badge-processing",
  shipped: "admin-badge-shipped",
  delivered: "admin-badge-delivered",
  cancelled: "admin-badge-cancelled",
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<ApiOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        setOrders(await fetchAllPages<ApiOrder>("/api/admin/orders"));
      } catch (err) {
        console.error("Failed to fetch data:", err);
        setError(errorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const updateOrderStatus = async (orderId: number, newStatus: string) => {
    try {
      const updated = await apiFetch<ApiOrder>(`/api/admin/orders/${orderId}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: newStatus }),
      });
      setOrders(orders.map(order => order.id === orderId ? updated : order));
    } catch (err) {
      console.error("Failed to update order status:", err);
      alert(errorMessage(err));
    }
  };

  const deleteOrder = async (orderId: number) => {
    if (confirm("Are you sure you want to delete this order?")) {
      try {
        await apiFetch(`/api/admin/orders/${orderId}`, { method: "DELETE" });
        setOrders(orders.filter(order => order.id !== orderId));
      } catch (err) {
        console.error("Failed to delete order:", err);
        alert(errorMessage(err));
      }
    }
  };

  if (loading) {
    return (
      <div className="admin-panel admin-loading">
        <div className="spinner" style={{ width: 20, height: 20 }} />
        Loading orders…
      </div>
    );
  }

  if (error) {
    return <div className="admin-error-box">⚠️ {error}</div>;
  }

  return (
    <div>
      {orders.length === 0 ? (
        <div className="admin-panel admin-empty">
          <span className="admin-empty-icon">📦</span>
          No orders yet.
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          {orders.map((order) => (
            <div key={order.id} className="admin-panel">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "20px", flexWrap: "wrap", marginBottom: "16px" }}>
                <div>
                  <h3 style={{ fontSize: "16px", color: "#2c3e50", marginBottom: "6px" }}>Order #{order.id}</h3>
                  <p style={{ fontSize: "14px", color: "#3a4457" }}><strong>Customer:</strong> {order.customerName}</p>
                  <p style={{ fontSize: "13px", color: "#7a8699" }}>{order.customerEmail}</p>
                  <p style={{ fontSize: "13px", color: "#7a8699" }}>Ship to: {order.shippingAddress}</p>
                  <p style={{ fontSize: "13px", color: "#7a8699" }}>
                    Payment: {order.paymentMethodLabel} ({order.paymentStatus === "TEST_SIMULATED" ? "test, no charge" : order.paymentStatus.toLowerCase()})
                  </p>
                  <p style={{ fontSize: "14px", color: "#2c3e50", marginTop: "6px" }}>
                    <strong>Total: ₹{order.totalAmount}</strong>
                  </p>
                </div>

                <div style={{ display: "flex", flexDirection: "column", gap: "10px", alignItems: "flex-end", minWidth: "160px" }}>
                  <span className={`admin-badge ${STATUS_BADGE[order.status] ?? "admin-badge-customer"}`}>
                    {order.status}
                  </span>

                  <select
                    className="admin-select"
                    value={order.status}
                    onChange={(e) => updateOrderStatus(order.id, e.target.value)}
                    style={{ width: "160px" }}
                  >
                    <option value="pending">Pending</option>
                    <option value="processing">Processing</option>
                    <option value="shipped">Shipped</option>
                    <option value="delivered">Delivered</option>
                    <option value="cancelled">Cancelled</option>
                  </select>

                  <button className="admin-btn admin-btn-danger" onClick={() => deleteOrder(order.id)}>
                    Delete Order
                  </button>
                </div>
              </div>

              <div>
                <h4 style={{ fontSize: "13px", color: "#7a8699", textTransform: "uppercase", letterSpacing: "0.4px", marginBottom: "10px" }}>
                  Order Items
                </h4>
                {order.items.length === 0 ? (
                  <p style={{ fontSize: "14px", color: "#8a94a6" }}>No items in this order.</p>
                ) : (
                  <div style={{ display: "grid", gap: "10px" }}>
                    {order.items.map((item) => (
                      <div key={item.id} className="admin-row" style={{ justifyContent: "flex-start", gap: "14px" }}>
                        <img src={item.image} alt={item.title} className="admin-thumb" />
                        <div style={{ flex: 1 }}>
                          <p style={{ fontWeight: 600, color: "#2c3e50", margin: 0 }}>{item.title}</p>
                          <p style={{ margin: "3px 0 0", color: "#7a8699", fontSize: "13px" }}>Quantity: {item.quantity}</p>
                        </div>
                        <strong style={{ color: "#4776E6" }}>₹{item.lineTotal}</strong>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
