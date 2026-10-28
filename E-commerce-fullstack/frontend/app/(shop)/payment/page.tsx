"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch, ApiOrder, errorMessage, getCart, getToken, PAYMENT_METHODS } from "../../../lib/api";

export default function PaymentPage() {
  const [total, setTotal] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const router = useRouter();

  useEffect(() => {
    // Check if user is logged in
    if (!getToken()) {
      alert("Please login or register to complete your order");
      router.push("/login?redirect=/payment");
      return;
    }

    if (!sessionStorage.getItem("shippingAddress")) {
      router.push("/checkout");
      return;
    }

    getCart()
      .then((cart) => {
        if (cart.items.length === 0) {
          router.push("/cart");
          return;
        }
        setTotal(cart.total); // from the server, never computed in the browser
      })
      .catch((err) => alert(errorMessage(err)));
  }, []);

  const handlePayment = async (method: string) => {
    if (submitting) return;

    if (!getToken()) {
      alert("Please login to place order");
      router.push("/login?redirect=/payment");
      return;
    }

    const shippingAddress = sessionStorage.getItem("shippingAddress");
    if (!shippingAddress) {
      router.push("/checkout");
      return;
    }

    try {
      setSubmitting(true);

      // The server rebuilds the order from the saved cart, re-reads prices from MySQL,
      // checks/decrements stock and empties the cart, all in one transaction.
      const order = await apiFetch<ApiOrder>("/api/orders", {
        method: "POST",
        body: JSON.stringify({ shippingAddress, paymentMethod: PAYMENT_METHODS[method] }),
      });

      sessionStorage.removeItem("shippingAddress");
      router.push(`/payment-success?order=${order.id}&method=${PAYMENT_METHODS[method]}`);
    } catch (error) {
      console.error("Failed to place order:", error);
      alert(errorMessage(error));
      setSubmitting(false);
    }
  };

  return (
    <div className="page" style={{ maxWidth: "600px", margin: "0 auto", textAlign: "center" }}>
      <h2 className="title">CHECKOUT</h2>

      <div style={{ background: "#f9f9f9", padding: "40px", border: "1px solid #ddd", marginBottom: "30px" }}>
        <h3 style={{ fontSize: "20px", marginBottom: "10px" }}>Order Total</h3>
        <p style={{ fontSize: "36px", fontWeight: "bold" }}>₹{total}</p>
      </div>

      <h3 style={{ marginBottom: "20px", textTransform: "uppercase", letterSpacing: "2px" }}>Select Payment Method</h3>

      <div style={{ display: "flex", flexDirection: "column", gap: "15px" }}>
        <button
          onClick={() => handlePayment("Google Pay")}
          disabled={submitting}
          style={{ background: "#202124", padding: "18px", fontSize: "16px", display: "flex", alignItems: "center", justifyContent: "center", gap: "10px" }}
        >
          {/* Simple GPay text representation */}
          <span style={{fontWeight: "bold", fontSize: "18px"}}>GPay</span> • Google Pay
        </button>

        <button
          onClick={() => handlePayment("PhonePe")}
          disabled={submitting}
          style={{ background: "#5f259f", padding: "18px", fontSize: "16px", display: "flex", alignItems: "center", justifyContent: "center", gap: "10px" }}
        >
          <span style={{fontWeight: "bold", fontSize: "18px", color: "white"}}>पे</span> PhonePe
        </button>

        <button
          onClick={() => handlePayment("Cash on Delivery")}
          disabled={submitting}
          style={{ background: "white", color: "black", border: "2px solid black", padding: "18px", fontSize: "16px" }}
        >
          Cash on Delivery
        </button>
      </div>

      <p style={{ marginTop: "25px", fontSize: "13px", color: "#666" }}>
        Test mode: Google Pay and PhonePe are not connected to a payment gateway yet, so no money is charged.
        The order is recorded as a test payment. Choose Cash on Delivery to pay when it arrives.
      </p>
    </div>
  );
}
