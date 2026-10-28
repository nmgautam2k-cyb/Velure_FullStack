"use client";

import { useEffect, useState, useRef } from "react";
import { ApiProduct, apiFetch, errorMessage, fetchAllPages, uploadImage } from "../../../lib/api";

type Category = "tshirts" | "shirts" | "pants";

export default function ProductsPage() {
  const [products, setProducts] = useState<ApiProduct[]>([]);
  const [formData, setFormData] = useState({
    title: "",
    price: "",
    stockQuantity: "50",
    category: "tshirts" as Category,
    image: ""
  });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchProducts = async () => {
    try {
      setProducts(await fetchAllPages<ApiProduct>("/api/products"));
    } catch (error) {
      console.error("Failed to fetch products:", error);
      alert(errorMessage(error));
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const resetForm = () => {
    setFormData({
      title: "",
      price: "",
      stockQuantity: "50",
      category: "tshirts",
      image: ""
    });
    setEditingId(null);
    setImagePreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  // Images are uploaded to the backend (stored on disk); the database keeps only the URL.
  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      try {
        const url = await uploadImage(file);
        setFormData(prev => ({ ...prev, image: url }));
        setImagePreview(url);
      } catch (error) {
        alert(errorMessage(error));
        if (fileInputRef.current) fileInputRef.current.value = "";
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const productData = {
        title: formData.title,
        price: parseFloat(formData.price),
        stockQuantity: parseInt(formData.stockQuantity, 10),
        category: formData.category,
        image: formData.image || "/placeholder.png"
      };

      if (editingId) {
        await apiFetch(`/api/products/${editingId}`, { method: "PUT", body: JSON.stringify(productData) });
      } else {
        await apiFetch("/api/products", { method: "POST", body: JSON.stringify(productData) });
      }

      resetForm();
      fetchProducts();
    } catch (error) {
      console.error("Failed to save product:", error);
      alert(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (product: ApiProduct) => {
    setFormData({
      title: product.title,
      price: product.price.toString(),
      stockQuantity: product.stockQuantity.toString(),
      category: product.category as Category,
      image: product.image
    });
    setEditingId(product.id);
    setImagePreview(product.image);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleDelete = async (id: number) => {
    if (confirm("Are you sure you want to delete this product?")) {
      try {
        await apiFetch(`/api/products/${id}`, { method: "DELETE" });
        fetchProducts();
      } catch (error) {
        console.error("Failed to delete product:", error);
        alert(errorMessage(error)); // e.g. the product is part of past orders
      }
    }
  };

  const filteredProducts = (category: string) =>
    products.filter(p => p.category === category);

  return (
    <div>
      {/* Add/Edit Product Form */}
      <div className="admin-panel" style={{ marginBottom: "28px" }}>
        <div className="admin-panel-title">{editingId ? "Edit Product" : "Add New Product"}</div>

        <form onSubmit={handleSubmit}>
          <div className="admin-form-grid">
            <div className="admin-field">
              <label>Product Title</label>
              <input
                className="admin-input"
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                required
              />
            </div>

            <div className="admin-field">
              <label>Price (₹)</label>
              <input
                className="admin-input"
                type="number"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                required
                min="0"
                step="0.01"
              />
            </div>

            <div className="admin-field">
              <label>Stock Quantity</label>
              <input
                className="admin-input"
                type="number"
                value={formData.stockQuantity}
                onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
                required
                min="0"
                step="1"
              />
            </div>

            <div className="admin-field">
              <label>Category</label>
              <select
                className="admin-select"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value as Category })}
              >
                <option value="tshirts">T-Shirts</option>
                <option value="shirts">Shirts</option>
                <option value="pants">Pants</option>
              </select>
            </div>

            <div className="admin-field">
              <label>Product Image</label>
              <input
                className="admin-input"
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                ref={fileInputRef}
                style={{ padding: "8px" }}
              />
              {imagePreview && (
                <img src={imagePreview} alt="Preview" className="admin-thumb" style={{ width: 70, height: 70, marginTop: 4 }} />
              )}
            </div>
          </div>

          <div style={{ display: "flex", gap: "10px", marginTop: "6px" }}>
            <button type="submit" disabled={loading} className="admin-btn admin-btn-primary">
              {loading ? "Saving…" : (editingId ? "Update Product" : "Add Product")}
            </button>

            {editingId && (
              <button type="button" onClick={resetForm} className="admin-btn admin-btn-secondary">
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Products List by Category */}
      {(["tshirts", "shirts", "pants"] as const).map(category => (
        <div key={category} style={{ marginBottom: "28px" }}>
          <h3 style={{ textTransform: "capitalize", fontSize: "16px", color: "#2c3e50", marginBottom: "14px" }}>
            {category}
          </h3>

          {filteredProducts(category).length === 0 ? (
            <div className="admin-panel admin-empty">
              <span className="admin-empty-icon">🛍️</span>
              No {category} found.
            </div>
          ) : (
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "18px" }}>
              {filteredProducts(category).map((product) => (
                <div key={product.id} className="admin-panel" style={{ padding: "16px" }}>
                  <img
                    src={product.image}
                    alt={product.title}
                    style={{ width: "100%", height: "150px", objectFit: "cover", borderRadius: "10px", marginBottom: "12px" }}
                  />

                  <h4 style={{ fontSize: "14.5px", color: "#2c3e50", marginBottom: "4px" }}>{product.title}</h4>
                  <p style={{ color: "#4776E6", fontWeight: 700, margin: "4px 0" }}>₹{product.price}</p>
                  <span className={`admin-badge ${product.stockQuantity > 0 ? "admin-badge-instock" : "admin-badge-outofstock"}`}>
                    {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : "Out of stock"}
                  </span>

                  <div style={{ marginTop: "12px", display: "flex", gap: "8px" }}>
                    <button onClick={() => handleEdit(product)} className="admin-btn admin-btn-secondary" style={{ flex: 1, padding: "8px 10px", fontSize: "13px" }}>
                      Edit
                    </button>
                    <button onClick={() => handleDelete(product.id)} className="admin-btn admin-btn-danger" style={{ flex: 1, padding: "8px 10px", fontSize: "13px" }}>
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
