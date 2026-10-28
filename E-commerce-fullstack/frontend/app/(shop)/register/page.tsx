"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { errorMessage, register } from "../../../lib/api";

export default function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!name.trim()) {
      setError("Full name is required.");
      return;
    }

    if (!email.trim()) {
      setError("Email address is required.");
      return;
    }

    if (!password) {
      setError("Password is required.");
      return;
    }

    if (password.length < 8) {
      setError("Password must be at least 8 characters long.");
      return;
    }

    try {
      setLoading(true);

      // New accounts are always customers; the server ignores any role sent by the browser.
      await register(name, email, password);

      setSuccess("Account created successfully! Redirecting to login…");
      setTimeout(() => router.push("/login"), 1500);
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
        <h2 className="auth-title">Create Account</h2>

        {error && (
          <div className="auth-alert auth-alert-error" role="alert">
            <span className="auth-alert-icon">⚠</span>
            {error}
          </div>
        )}

        {success && (
          <div className="auth-alert auth-alert-success" role="status">
            <span className="auth-alert-icon">✓</span>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">

          <div className="input-group">
            <input
              type="text"
              placeholder=" "
              value={name}
              onChange={(e) => { setName(e.target.value); setError(null); }}
              required
            />
            <label>Full Name</label>
          </div>

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

          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? "Registering…" : "Register"}
          </button>
        </form>

        <p className="auth-text">
          Already have an account? <Link href="/login">Login</Link>
        </p>
      </div>
    </div>
  );
}