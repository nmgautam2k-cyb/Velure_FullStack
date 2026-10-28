"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

export default function PaymentSuccessPage() {
  const [orderId, setOrderId] = useState("");
  const [method, setMethod] = useState("");

  useEffect(() => {
    // Read the query string on the client (avoids needing a Suspense boundary).
    const params = new URLSearchParams(window.location.search);
    setOrderId(params.get("order") || "");
    setMethod(params.get("method") || "");
  }, []);

  const isCod = method === "COD";

  return (
    <div className="page" style={{ maxWidth: "600px", margin: "0 auto", textAlign: "center", padding: "40px 20px" }}>
      <div style={{
        background: "#d4edda",
        border: "1px solid #c3e6cb",
        borderRadius: "8px",
        padding: "40px 30px",
        marginBottom: "30px"
      }}>
        <div style={{
          width: "80px",
          height: "80px",
          background: "#28a745",
          borderRadius: "50%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          margin: "0 auto 20px",
          fontSize: "40px",
          color: "white"
        }}>
          ✓
        </div>
        <h1 style={{ color: "#155724", marginBottom: "10px" }}>Order Placed!</h1>
        {orderId && <p style={{ color: "#155724", fontSize: "16px" }}>Order #{orderId}</p>}
        <p style={{ color: "#155724", fontSize: "16px" }}>
          {isCod
            ? "Your order has been placed. Please keep the cash ready for delivery."
            : "Your order has been placed as a test payment. No real money was charged."}
          {" "}Thank you for shopping with us!
        </p>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
        <Link
          href="/orders"
          style={{
            background: "#000",
            color: "#fff",
            padding: "15px 30px",
            textDecoration: "none",
            borderRadius: "4px",
            fontSize: "16px",
            fontWeight: "500"
          }}
        >
          View My Orders
        </Link>

        <Link
          href="/"
          style={{
            background: "#fff",
            color: "#000",
            padding: "15px 30px",
            textDecoration: "none",
            borderRadius: "4px",
            fontSize: "16px",
            fontWeight: "500",
            border: "2px solid #000"
          }}
        >
          Continue Shopping
        </Link>
      </div>
    </div>
  );
}
