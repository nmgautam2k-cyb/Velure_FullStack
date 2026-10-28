"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getUser } from "../../../lib/auth";
import { ApiOrder, ApiPage, apiFetch, errorMessage, getToken } from "../../../lib/api";

export default function OrdersPage() {
  const [orders, setOrders] = useState<ApiOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const router = useRouter();

  useEffect(() => {
    const user = getUser();

    // ❌ BLOCK ADMIN
    if (user?.role === "admin") {
      alert("Admin cannot view this page");
      router.push("/admin");
      return;
    }

    if (!getToken()) {
      alert("Please login to view your orders");
      router.push("/login?redirect=/orders");
      return;
    }

    // The server returns only the signed-in user's orders (identified by the JWT, not by an id we send).
    const fetchData = async () => {
      try {
        const res = await apiFetch<ApiPage<ApiOrder>>("/api/orders/my-orders?size=100");
        setOrders(res.content);
      } catch (err) {
        console.error("Failed to fetch orders:", err);
        setError(errorMessage(err));
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case "pending": return "#ffc107";
      case "processing": return "#17a2b8";
      case "shipped": return "#007bff";
      case "delivered": return "#28a745";
      case "cancelled": return "#dc3545";
      default: return "#6c757d";
    }
  };

  if (loading) {
    return <div className="page"><h2>Orders</h2><p>Loading orders...</p></div>;
  }

  return (
    <div className="page">
      <h2 className="title">My Orders</h2>

      {orders.length === 0 ? (
        <div style={{ textAlign: "center", padding: "40px", background: "rgba(255,255,255,0.5)", borderRadius: "12px" }}>
          <p style={{ fontSize: "18px", marginBottom: "10px" }}>⚠️ No Orders Found</p>
          <p style={{ color: "#666" }}>{error || "You have not placed any orders yet."}</p>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          {orders.map((order) => (
            <div key={order.id} style={{
              border: "1px solid #ddd",
              borderRadius: "8px",
              padding: "20px",
              backgroundColor: "#f9f9f9"
            }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "start", marginBottom: "15px" }}>
                <div>
                  <h3>Order #{order.id}</h3>
                  <p style={{ fontSize: "14px", color: "#666" }}>
                    Placed on: {new Date(order.createdAt).toLocaleDateString()}
                  </p>
                  <p style={{ fontSize: "14px", color: "#666" }}>
                    Payment: {order.paymentMethodLabel}{order.paymentStatus === "TEST_SIMULATED" ? " (test, no charge)" : order.paymentStatus === "PAID" ? " (paid)" : ""}
                  </p>
                  <p style={{ fontSize: "14px", color: "#666" }}>Ship to: {order.shippingAddress}</p>
                  <p><strong>Total: ₹{order.totalAmount}</strong></p>
                </div>
                
                <div style={{
                  padding: "5px 10px",
                  backgroundColor: getStatusColor(order.status),
                  color: "white",
                  borderRadius: "4px",
                  fontSize: "12px",
                  textTransform: "uppercase",
                  fontWeight: "bold"
                }}>
                  {order.status}
                </div>
              </div>

              <div>
                <h4>Order Items:</h4>
                {order.items.length === 0 ? (
                  <p>No items in this order.</p>
                ) : (
                  <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                    {order.items.map((item) => {
                      return (
                        <div key={item.id} style={{
                          display: "flex",
                          alignItems: "center",
                          gap: "15px",
                          padding: "10px",
                          backgroundColor: "white",
                          borderRadius: "4px",
                          border: "1px solid #eee"
                        }}>
                          <img
                            src={item.image}
                            alt={item.title}
                            style={{
                              width: "60px",
                              height: "60px",
                              objectFit: "cover",
                              borderRadius: "4px"
                            }}
                          />

                          <div style={{ flex: 1 }}>
                            <p style={{ fontWeight: "bold", margin: "0" }}>
                              {item.title}
                            </p>
                            <p style={{ margin: "5px 0", color: "#666", fontSize: "14px" }}>
                              Quantity: {item.quantity}
                            </p>
                            <p style={{ margin: "5px 0", color: "#007bff", fontWeight: "bold" }}>
                              ₹{item.lineTotal}
                            </p>
                          </div>
                        </div>
                      );
                    })}
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