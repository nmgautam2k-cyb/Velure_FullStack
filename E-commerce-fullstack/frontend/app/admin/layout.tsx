"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import Link from "next/link";
import { getUser, isAdmin, logout } from "../../lib/auth";

const NAV_ITEMS = [
  { href: "/admin", label: "Dashboard", icon: "📊", exact: true },
  { href: "/admin/products", label: "Products", icon: "🛍️" },
  { href: "/admin/orders", label: "Orders", icon: "📦" },
  { href: "/admin/users", label: "Users", icon: "👥" },
];

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const isLoginPage = pathname === "/admin/login";

  // 🔐 PROTECT ADMIN ROUTES — but never the admin login page itself, or no one
  // could ever reach it to sign in in the first place.
  useEffect(() => {
    if (isLoginPage) return;
    if (!isAdmin()) {
      router.push(`/admin/login?redirect=${encodeURIComponent(pathname)}`);
    }
  }, [router, isLoginPage, pathname]);

  // The login page renders its own full-screen auth layout; skip the admin chrome for it.
  if (isLoginPage) {
    return <>{children}</>;
  }

  const user = getUser();
  const initial = user?.fullName?.charAt(0)?.toUpperCase() || "A";

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div>
          <div className="admin-sidebar-brand">VELURE</div>
          <div className="admin-sidebar-tag">Admin Console</div>
        </div>

        <nav className="admin-nav">
          {NAV_ITEMS.map((item) => {
            const active = item.exact ? pathname === item.href : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`admin-nav-link${active ? " active" : ""}`}
              >
                <span aria-hidden="true">{item.icon}</span>
                <span className="label">{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="admin-sidebar-footer">
          <button className="admin-logout-btn" onClick={() => {
            logout();
            router.push("/admin/login");
          }}>
            🚪 <span className="label">Sign Out</span>
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <div className="admin-topbar">
          <div>
            <div className="admin-page-title">
              {NAV_ITEMS.find((i) => (i.exact ? pathname === i.href : pathname.startsWith(i.href)))?.label ?? "Admin"}
            </div>
            <div className="admin-page-subtitle">Manage your VELURE storefront</div>
          </div>
          <div className="admin-admin-chip">
            <span className="admin-admin-avatar">{initial}</span>
            <span style={{ fontSize: "13px", fontWeight: 600, color: "#2c3e50" }}>
              {user?.fullName || "Administrator"}
            </span>
          </div>
        </div>

        {children}
      </main>
    </div>
  );
}