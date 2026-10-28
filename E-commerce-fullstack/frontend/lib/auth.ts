import { ApiUser, clearSession, getToken } from "./api";

export const getUser = (): ApiUser | null => {
  if (typeof window === "undefined") return null;

  try {
    return JSON.parse(localStorage.getItem("user") || "null") as ApiUser | null;
  } catch {
    console.error("Invalid user data");
    return null;
  }
};

// NOTE: these helpers only decide what the UI shows. The real authorization check happens on the
// server for every API call (JWT + role), so editing localStorage cannot grant access to anything.
export const isLoggedIn = () => {
  return !!getToken() && !!getUser();
};

export const isAdmin = () => {
  const user = getUser();
  return !!getToken() && user?.role === "admin";
};

export const isCustomer = () => {
  const user = getUser();
  return !!getToken() && user?.role === "customer";
};

export const logout = () => {
  if (typeof window !== "undefined") {
    clearSession();
    window.location.href = "/login";
  }
};

/* OPTIONAL HELPERS (GOOD FOR FUTURE) */

interface RouterLike {
  push: (href: string) => void;
}

export const requireAdmin = (router: RouterLike) => {
  if (!isAdmin()) {
    router.push("/login?redirect=/admin");
  }
};

export const requireAuth = (router: RouterLike) => {
  if (!isLoggedIn()) {
    router.push("/login");
  }
};
