"use client";

export default function FilterBar({ setFilter }: { setFilter: (v: string) => void }) {
  return (
    <div style={{ display: "flex", justifyContent: "flex-end", padding: "0 0 20px", marginBottom: "10px" }}>
      <select
        onChange={(e) => setFilter(e.target.value)}
        style={{
          padding: "10px 16px",
          borderRadius: "10px",
          border: "1px solid #ccc",
          background: "rgba(255,255,255,0.8)",
          fontSize: "14px",
          fontFamily: "inherit",
          cursor: "pointer",
          outline: "none",
        }}
        aria-label="Sort products"
      >
        <option value="newest">Newest First</option>
        <option value="low">Price: Low → High</option>
        <option value="high">Price: High → Low</option>
        <option value="name">Name: A → Z</option>
      </select>
    </div>
  );
}