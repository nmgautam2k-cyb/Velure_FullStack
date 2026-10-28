"use client";

import { Suspense, useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { getUser } from "../../../lib/auth";
import { errorMessage, login } from "../../../lib/api";
import { useAuth } from "../../../lib/AuthContext";

function AdminLoginInner() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const router = useRouter();
  const searchParams = useSearchParams();
  const { login: setAuth } = useAuth();

  useEffect(() => {
    const user = getUser();
    if (user?.role === "admin") {
      const redirect = searchParams.get("redirect") || "/admin";
      router.push(redirect);
    }
  }, [router, searchParams]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const { token, user } = await login(email, password, "admin");
      setAuth(token, user);
      const redirect = searchParams.get("redirect") || "/admin";
      router.push(redirect);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h1 className="logo">VELURE</h1>
      <p className="sub-logo">ADMIN PORTAL</p>

      <div className="auth-card">
        <h2 className="auth-title">Admin Login</h2>

        {error && (
          <div className="auth-alert auth-alert-error" role="alert">
            <span className="auth-alert-icon">⚠</span>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="auth-form">
          <div className="input-group">
            <input
              type="email"
              placeholder=" "
              value={email}
              onChange={(e) => { setEmail(e.target.value); setError(""); }}
              required
            />
            <label>Email</label>
          </div>

          <div className="input-group">
            <input
              type="password"
              placeholder=" "
              value={password}
              onChange={(e) => { setPassword(e.target.value); setError(""); }}
              required
            />
            <label>Password</label>
          </div>

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <p className="auth-text">
          <Link href="/login">Customer Login</Link>
        </p>
      </div>
    </div>
  );
}

export default function Page() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <AdminLoginInner />
    </Suspense>
  );
}