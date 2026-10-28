"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  ApiCart,
  errorMessage,
  getCart,
  getToken,
  removeCartItem,
  updateCartItem,
} from "../../../lib/api";

// Shape kept from the original localStorage cart so the markup below is unchanged.
interface CartViewItem {
  id: number; // cart item id
  title: string;
  image: string;
  price: number;
  qty: number;
  stock: number;
}

const toView = (cart: ApiCart): { items: CartViewItem[]; total: number } => ({
  total: cart.total,
  items: cart.items.map((i) => ({
    id: i.id,
    title: i.title,
    image: i.image,
    price: i.unitPrice,
    qty: i.quantity,
    stock: i.stockQuantity,
  })),
});

export default function Cart() {
  const [cart, setCart] = useState<CartViewItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();

  const apply = (c: ApiCart) => {
    const view = toView(c);
    setCart(view.items);
    setTotal(view.total); // the total always comes from the server
  };

  useEffect(() => {
    if (!getToken()) {
      alert("Please login or register to view your cart");
      router.push("/login?redirect=/cart");
      return;
    }
    getCart()
      .then(apply)
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const handleProceedToPayment = () => {
    if (!getToken()) {
      alert("Please login or register to proceed to payment");
      router.push("/login?redirect=/checkout");
      return;
    }
    router.push("/checkout");
  };

  const removeItem = async (id: number) => {
    try {
      setBusy(true);
      apply(await removeCartItem(id));
    } catch (err) {
      alert(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const changeQty = async (id: number, qty: number) => {
    if (qty < 1) return;
    try {
      setBusy(true);
      apply(await updateCartItem(id, qty));
    } catch (err) {
      alert(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const qtyButton = {
    width: "auto",
    padding: "0 9px",
    margin: "0 6px",
    borderRadius: "4px",
    lineHeight: "22px",
  } as const;

  return (
    <div className="page cart-container">
      <h2 className="title">My Cart</h2>

      {loading ? (
        <p className="empty-cart-message">Loading your cart...</p>
      ) : error ? (
        <p className="empty-cart-message">⚠️ {error}</p>
      ) : cart.length === 0 ? (
        <p className="empty-cart-message">Your cart is empty.</p>
      ) : (
        <>
          <div className="cart-list">
            {cart.map((item) => (
              <div key={item.id} className="cart-card">
                <div className="cart-card-content">
                  <div className="cart-image-wrapper">
                    <img src={item.image || '/placeholder.png'} alt={item.title} className="cart-card-image" />
                  </div>
                  <div className="cart-card-details">
                    <h4>{item.title}</h4>
                    <p className="cart-card-price">
                      Qty:
                      <button
                        style={qtyButton}
                        disabled={busy || item.qty <= 1}
                        onClick={() => changeQty(item.id, item.qty - 1)}
                        aria-label="Decrease quantity"
                      >
                        −
                      </button>
                      {item.qty}
                      <button
                        style={qtyButton}
                        disabled={busy || item.qty >= item.stock}
                        onClick={() => changeQty(item.id, item.qty + 1)}
                        aria-label="Increase quantity"
                      >
                        +
                      </button>
                      &times; ₹{item.price}
                    </p>
                  </div>
                </div>
                <div className="cart-card-actions">
                  <p className="cart-card-total">₹{item.price * item.qty}</p>
                  <button
                    onClick={() => removeItem(item.id)}
                    className="remove-button"
                    disabled={busy}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <h3 className="cart-summary-title">Total: ₹{total}</h3>
            <button onClick={handleProceedToPayment} className="proceed-to-payment-button">
              PROCEED TO PAYMENT
            </button>
          </div>
        </>
      )}
    </div>
  );
}
