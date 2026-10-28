"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { errorMessage, login as apiLogin } from "../../../lib/api";
import { useAuth } from "../../../lib/AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("customer");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const router = useRouter();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!email.trim()) {
      setError("Please enter your email address.");
      return;
    }

    if (!password) {
      setError("Please enter your password.");
      return;
    }

    try {
      setLoading(true);

      // The server checks the password (BCrypt) and that the chosen role matches the account.
      const { token, user } = await apiLogin(email, password, role);

      // Persist to localStorage AND update the AuthContext so Navbar
      // immediately shows the logged-in user without needing a page reload.
      login(token, user);

      const params = new URLSearchParams(window.location.search);
      const redirect = params.get("redirect");

      if (user.role === "admin") {
        router.push("/admin");
      } else {
        router.push(redirect || "/");
      }
    } catch (err) {
      console.error(err);
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h1 className="logo">VELURE</h1>
      <p className="sub-logo">APPAREL</p>

      <div className="auth-card">
        <h2 className="auth-title">Login</h2>

        {error && (
          <div className="auth-alert auth-alert-error" role="alert">
            <span className="auth-alert-icon">⚠</span>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">

          <div className="input-group">
            <input
              type="email"
              placeholder=" "
              value={email}
              onChange={(e) => { setEmail(e.target.value); setError(null); }}
              required
            />
            <label>Email</label>
          </div>

          <div className="input-group">
            <input
              type="password"
              placeholder=" "
              value={password}
              onChange={(e) => { setPassword(e.target.value); setError(null); }}
              required
            />
            <label>Password</label>
          </div>

          <div className="input-group select-group">
            <label className="select-label">Login As</label>
            <select
              value={role}
              onChange={(e) => { setRole(e.target.value); setError(null); }}
            >
              <option value="customer">Customer</option>
              <option value="admin">Admin</option>
            </select>
          </div>

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? "Logging in…" : "Login"}
          </button>
        </form>

        <p className="auth-text">
          Don&apos;t have an account? <Link href="/register">Register</Link>
        </p>
      </div>
    </div>
  );
}