"use client";

/**
 * Reusable product grid used by /shirts, /tshirts, /pants.
 * Handles loading, error, empty, and product states uniformly.
 */

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import FilterBar from "./FilterBar";
import {
  addToCart as apiAddToCart,
  addToWishlist as apiAddToWishlist,
  ApiProduct,
  errorMessage,
  getProductsByCategory,
  getToken,
} from "../../lib/api";

interface Props {
  category: string;       // slug: "shirts" | "tshirts" | "pants"
  title: string;          // Display name: "Shirts" etc.
  searchPlaceholder: string;
}

export default function ProductGrid({ category, title, searchPlaceholder }: Props) {
  const router = useRouter();
  const [products, setProducts] = useState<ApiProduct[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [sort, setSort] = useState("newest");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [wishlistIds, setWishlistIds] = useState<Set<number>>(new Set());
  const [cartBusy, setCartBusy] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    getProductsByCategory(category, sort)
      .then((data) => { if (!cancelled) setProducts(data); })
      .catch((err) => { if (!cancelled) { setError(errorMessage(err)); setProducts([]); } })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [category, sort]);

  const addToCart = async (product: ApiProduct) => {
    if (!getToken()) {
      alert("Please sign in to add items to your cart.");
      router.push(`/login?redirect=/${category}`);
      return;
    }
    try {
      setCartBusy(product.id);
      await apiAddToCart(product.id, 1);
      // Brief success flash then reset
      setTimeout(() => setCartBusy(null), 1000);
    } catch (err) {
      alert(errorMessage(err));
      setCartBusy(null);
    }
  };

  const toggleWishlist = async (product: ApiProduct) => {
    if (!getToken()) {
      alert("Please sign in to use your wishlist.");
      router.push(`/login?redirect=/${category}`);
      return;
    }
    try {
      if (wishlistIds.has(product.id)) {
        // Already in wishlist — remove would need removeFromWishlist; for now just re-add is safe
        setWishlistIds((prev) => { const s = new Set(prev); s.delete(product.id); return s; });
      } else {
        await apiAddToWishlist(product.id);
        setWishlistIds((prev) => new Set(prev).add(product.id));
      }
    } catch (err) {
      alert(errorMessage(err));
    }
  };

  const visible = products.filter((p) =>
    p.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="page">
      {/* Page heading */}
      <h1 className="title" style={{ fontSize: "32px", marginBottom: "8px" }}>{title}</h1>
      <p style={{ textAlign: "center", color: "#888", marginBottom: "30px", fontSize: "14px" }}>
        {loading ? "\u00a0" : `${products.length} product${products.length !== 1 ? "s" : ""} available`}
      </p>

      {/* Search + sort bar */}
      <div style={{ display: "flex", gap: "12px", marginBottom: "8px", flexWrap: "wrap" }}>
        <input
          type="text"
          placeholder={searchPlaceholder}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{
            flex: 1,
            minWidth: "200px",
            padding: "12px 16px",
            borderRadius: "10px",
            border: "1px solid #ccc",
            background: "rgba(255,255,255,0.8)",
            fontSize: "15px",
            fontFamily: "inherit",
            outline: "none",
          }}
          aria-label={`Search ${title}`}
        />
        <FilterBar setFilter={setSort} />
      </div>

      {/* States */}
      {loading ? (
        <div className="state-center">
          <div className="spinner" />
          <p style={{ color: "#888", marginTop: "16px" }}>Loading {title.toLowerCase()}…</p>
        </div>
      ) : error ? (
        <div className="state-box state-error">
          <span className="state-icon">⚠️</span>
          <h3>Could not load products</h3>
          <p>{error}</p>
          <button
            onClick={() => setSort(sort)} /* re-triggers useEffect */
            style={{ marginTop: "16px", padding: "10px 24px", borderRadius: "8px", cursor: "pointer" }}
          >
            Try Again
          </button>
        </div>
      ) : visible.length === 0 ? (
        <div className="state-box">
          <span className="state-icon">🛍️</span>
          <h3>{searchQuery ? "No results found" : "No products yet"}</h3>
          <p style={{ color: "#888", fontSize: "14px" }}>
            {searchQuery ? `No ${title.toLowerCase()} matching "${searchQuery}".` : `Check back soon — new ${title.toLowerCase()} are on the way!`}
          </p>
          {searchQuery && (
            <button
              onClick={() => setSearchQuery("")}
              style={{ marginTop: "12px", padding: "8px 20px", borderRadius: "8px", cursor: "pointer" }}
            >
              Clear Search
            </button>
          )}
        </div>
      ) : (
        <div className="product-grid">
          {visible.map((p) => {
            const inWishlist = wishlistIds.has(p.id);
            const busy = cartBusy === p.id;
            return (
              <div key={p.id} className="card" style={{ padding: "0 0 16px" }}>
                {/* Wishlist heart */}
                <span
                  className={`heart${inWishlist ? " active" : ""}`}
                  onClick={() => toggleWishlist(p)}
                  title={inWishlist ? "Remove from wishlist" : "Add to wishlist"}
                  role="button"
                  aria-label="Toggle wishlist"
                >
                  {inWishlist ? "♥" : "♡"}
                </span>

                {/* Product image */}
                <img
                  src={p.image || "/placeholder.png"}
                  alt={p.title}
                  onError={(e) => { (e.target as HTMLImageElement).src = "/placeholder.png"; }}
                />

                {/* Details */}
                <div style={{ padding: "0 16px" }}>
                  <h4 style={{ marginTop: "12px", fontSize: "15px", fontWeight: 600, lineHeight: 1.3 }}>
                    {p.title}
                  </h4>
                  {p.description && (
                    <p style={{ fontSize: "12px", color: "#888", margin: "6px 0 0", lineHeight: 1.4,
                      overflow: "hidden", display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical" }}>
                      {p.description}
                    </p>
                  )}
                  <p style={{ fontWeight: 700, fontSize: "16px", color: "#4776E6", margin: "10px 0 12px" }}>
                    ₹{Number(p.price).toLocaleString("en-IN")}
                  </p>
                  <button
                    onClick={() => addToCart(p)}
                    disabled={p.stockQuantity <= 0 || busy}
                    style={{ width: "100%", padding: "10px", borderRadius: "8px",
                      background: busy ? "#6aad6a" : p.stockQuantity <= 0 ? "#ccc" : undefined,
                      cursor: p.stockQuantity <= 0 ? "not-allowed" : "pointer",
                    }}
                  >
                    {busy ? "✓ Added!" : p.stockQuantity <= 0 ? "Out of Stock" : "Add to Cart"}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}