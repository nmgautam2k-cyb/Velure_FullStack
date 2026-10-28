"use client";
import ProductGrid from "../../components/ProductGrid";

export default function ShirtsPage() {
  return (
    <ProductGrid
      category="shirts"
      title="Shirts"
      searchPlaceholder="Search shirts..."
    />
  );
}