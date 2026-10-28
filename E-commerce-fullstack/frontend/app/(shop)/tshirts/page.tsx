"use client";
import ProductGrid from "../../components/ProductGrid";

export default function TShirtsPage() {
  return (
    <ProductGrid
      category="tshirts"
      title="T-Shirts"
      searchPlaceholder="Search t-shirts..."
    />
  );
}