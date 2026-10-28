"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  addToCart as apiAddToCart,
  ApiProduct,
  errorMessage,
  getToken,
  getWishlist,
  removeFromWishlist,
} from "../../../lib/api";

export default function WishlistPage() {
  const [wishlist, setWishlist] = useState<ApiProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const router = useRouter();

  useEffect(() => {
    if (!getToken()) {
      alert("Please login or register to view your wishlist");
      router.push("/login?redirect=/wishlist");
      return;
    }
    getWishlist()
      .then(setWishlist)
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const removeItem = async (id: number) => {
    try {
      setWishlist(await removeFromWishlist(id));
    } catch (err) {
      alert(errorMessage(err));
    }
  };

  const addToCart = async (product: ApiProduct) => {
    try {
      await apiAddToCart(product.id, 1);
      alert("Added to cart!");
    } catch (err) {
      alert(errorMessage(err));
    }
  };

  return (
    <div className="page">
      <h2 className="title">My Wishlist</h2>

      {loading ? (
        <p style={{textAlign:"center"}}>Loading...</p>
      ) : error ? (
        <p style={{textAlign:"center"}}>⚠️ {error}</p>
      ) : wishlist.length === 0 ? (
        <p style={{textAlign:"center"}}>Your wishlist is empty.</p>
      ) : (
        <div className="product-grid">
          {wishlist.map(p => (
            <div key={p.id} className="card">
              <span className="heart active" onClick={() => removeItem(p.id)}>♥</span>
              <div className="wishlist-image-wrapper">
                <img src={p.image || '/placeholder.png'} alt={p.title} />
              </div>
              <h4 style={{marginTop: "10px"}}>{p.title}</h4>
              <p>₹{p.price}</p>
              <button onClick={() => addToCart(p)} disabled={p.stockQuantity <= 0}>
                {p.stockQuantity <= 0 ? "Out of Stock" : "Add to Cart"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
