"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import { ApiUser, clearSession, getToken, setSession } from "./api";

interface AuthContextValue {
  user: ApiUser | null;
  loading: boolean;
  login: (token: string, user: ApiUser) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  login: () => {},
  logout: () => {},
});

function readUserFromStorage(): ApiUser | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem("user");
    return raw ? (JSON.parse(raw) as ApiUser) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<ApiUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (getToken()) {
      setUser(readUserFromStorage());
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === "user" || e.key === "token" || e.key === null) {
        if (getToken()) {
          setUser(readUserFromStorage());
        } else {
          setUser(null);
        }
      }
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const login = useCallback((token: string, apiUser: ApiUser) => {
    setSession(token, apiUser);
    setUser(apiUser);
    window.dispatchEvent(new StorageEvent("storage", { key: "user" }));
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
    window.dispatchEvent(new StorageEvent("storage", { key: "user" }));
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  return useContext(AuthContext);
}
