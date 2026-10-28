"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { errorMessage, getCart, getToken } from "../../../lib/api";

export default function Checkout() {
  const [total, setTotal] = useState(0);
  const [itemCount, setItemCount] = useState(0);
  const [address, setAddress] = useState("");
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // 🔥 CHECK LOGIN FIRST
    if (!getToken()) {
      alert("Please login or register to continue");
      router.push("/login?redirect=/checkout"); // 🔥 redirect back after login
      return;
    }

    // ✅ LOAD CART TOTAL FROM THE SERVER
    getCart()
      .then((cart) => {
        if (cart.items.length === 0) {
          alert("Your cart is empty");
          router.push("/cart");
          return;
        }
        setTotal(cart.total);
        setItemCount(cart.itemCount);
      })
      .catch((err) => alert(errorMessage(err)))
      .finally(() => setLoading(false));

    // restore an address typed earlier in this session
    setAddress(sessionStorage.getItem("shippingAddress") || "");
  }, []);

  // The order itself is created on the payment step (one order, with the chosen payment method).
  const continueToPayment = () => {
    if (!getToken()) {
      alert("Please login or register first");
      router.push("/login?redirect=/checkout");
      return;
    }

    const trimmed = address.trim();
    if (!trimmed) return alert("Enter address");
    if (trimmed.length < 10) return alert("Please enter your full delivery address (at least 10 characters)");

    sessionStorage.setItem("shippingAddress", trimmed);
    router.push("/payment");
  };

  return (
    <div className="page">
      <h2 className="title">Checkout 🧾</h2>

      <div style={{ maxWidth: "500px", margin: "auto" }}>
        <textarea
          placeholder="Enter delivery address..."
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          maxLength={1000}
          style={{ width: "100%", padding: "10px", height: "100px" }}
        />

        <h3>{loading ? "Loading total..." : `Total: ₹${total} (${itemCount} item${itemCount === 1 ? "" : "s"})`}</h3>

        <button onClick={continueToPayment} disabled={loading}>Continue to Payment</button>
      </div>
    </div>
  );
}
