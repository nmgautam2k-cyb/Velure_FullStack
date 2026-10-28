"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "../../lib/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    setMenuOpen(false);
    logout();
    router.push("/");
  };

  return (
    <nav className="navbar">
      <div className="logo">
        <Link href="/">VELURE</Link>
      </div>

      <div className="nav-right">
        <Link href="/">Home</Link>
        <Link href="/shirts">Shirts</Link>
        <Link href="/pants">Pants</Link>
        <Link href="/tshirts">T-Shirts</Link>
        <Link href="/cart">Cart</Link>
        <Link href="/wishlist">Wishlist</Link>
        <Link href="/orders">Orders</Link>

        {user ? (
          <div className="user-menu-wrapper">
            <button
              className="user-menu-btn"
              onClick={() => setMenuOpen((o) => !o)}
              aria-haspopup="true"
              aria-expanded={menuOpen}
            >
              <span className="user-avatar">
                {user.fullName.charAt(0).toUpperCase()}
              </span>
              <span className="user-name">{user.fullName.split(" ")[0]}</span>
              <span className="chevron">{menuOpen ? "▲" : "▼"}</span>
            </button>

            {menuOpen && (
              <div className="user-dropdown" role="menu">
                <div className="user-dropdown-header">
                  <span className="user-dropdown-avatar">
                    {user.fullName.charAt(0).toUpperCase()}
                  </span>
                  <div>
                    <p className="user-dropdown-name">{user.fullName}</p>
                    <p className="user-dropdown-email">{user.email}</p>
                  </div>
                </div>
                <div className="user-dropdown-divider" />
                <Link
                  href="/orders"
                  className="user-dropdown-item"
                  onClick={() => setMenuOpen(false)}
                >
                  📦 My Orders
                </Link>
                <Link
                  href="/wishlist"
                  className="user-dropdown-item"
                  onClick={() => setMenuOpen(false)}
                >
                  ♥ Wishlist
                </Link>
                <Link
                  href="/cart"
                  className="user-dropdown-item"
                  onClick={() => setMenuOpen(false)}
                >
                  🛒 My Cart
                </Link>
                <div className="user-dropdown-divider" />
                <button
                  className="user-dropdown-item user-dropdown-logout"
                  onClick={handleLogout}
                >
                  🚪 Sign Out
                </button>
              </div>
            )}
          </div>
        ) : (
          <>
            <Link href="/login" className="nav-sign-in">Sign In</Link>
            <Link href="/register" className="nav-sign-up">Sign Up</Link>
          </>
        )}
      </div>
    </nav>
  );
}