// Central API client for the Spring Boot backend.
// Base URL comes from NEXT_PUBLIC_API_BASE_URL (see .env.example); nothing is hardcoded per-page.

export const API_BASE_URL = (process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

const TOKEN_KEY = "token";
const USER_KEY = "user";

/* ----------------------------- types ----------------------------- */

export interface ApiUser {
  id: number;
  fullName: string;
  email: string;
  phone?: string | null;
  role: "admin" | "customer";
  createdAt: string;
}

export interface ApiProduct {
  id: number;
  title: string;
  description?: string | null;
  price: number;
  stockQuantity: number;
  image: string;
  category: string;
  categoryName: string;
  createdAt: string;
}

export interface ApiPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiCartItem {
  id: number;
  productId: number;
  title: string;
  image: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  stockQuantity: number;
}

export interface ApiCart {
  id: number;
  items: ApiCartItem[];
  itemCount: number;
  total: number;
}

export interface ApiOrderItem {
  id: number;
  productId: number;
  title: string;
  image: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface ApiOrder {
  id: number;
  userId: number;
  customerName: string;
  customerEmail: string;
  status: "pending" | "processing" | "shipped" | "delivered" | "cancelled";
  paymentMethod: string;
  paymentMethodLabel: string;
  paymentStatus: "PENDING" | "TEST_SIMULATED" | "PAID";
  shippingAddress: string;
  totalAmount: number;
  createdAt: string;
  items: ApiOrderItem[];
}

export interface ApiSummary {
  totalProducts: number;
  totalOrders: number;
  totalUsers: number;
  adminUsers: number;
  customerUsers: number;
  productsByCategory: Record<string, number>;
  ordersByStatus: Record<string, number>;
  totalRevenue: number;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: ApiUser;
}

/* ----------------------------- session ----------------------------- */

export const getToken = (): string | null => {
  if (typeof window === "undefined") return null;
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
};

export const setSession = (token: string, user: ApiUser) => {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
};

export const clearSession = () => {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

/* ----------------------------- errors ----------------------------- */

export class ApiError extends Error {
  status: number;
  fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

/** Human-readable text for any thrown value. */
export const errorMessage = (err: unknown): string => {
  if (err instanceof ApiError) return err.message;
  if (err instanceof Error) return err.message;
  return "Something went wrong";
};

/* ----------------------------- fetch wrapper ----------------------------- */

export async function apiFetch<T = unknown>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  const isForm = typeof FormData !== "undefined" && init.body instanceof FormData;
  if (init.body && !isForm && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  } catch {
    throw new ApiError(0, "Cannot reach the server. Please check that the backend is running.");
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  let data: unknown = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!res.ok) {
    // Expired / invalid token on a protected call: drop the session and send the user to log in.
    if (res.status === 401 && token && !path.startsWith("/api/auth/")) {
      clearSession();
      if (typeof window !== "undefined") {
        const back = encodeURIComponent(window.location.pathname);
        window.location.href = `/login?redirect=${back}`;
      }
    }
    const errBody = (data && typeof data === "object" ? data : {}) as { message?: string; fieldErrors?: Record<string, string> };
    throw new ApiError(res.status, errBody.message || `Request failed (${res.status})`, errBody.fieldErrors);
  }
  return data as T;
}

/** Loads every page of a paged list endpoint (page size 100). */
export async function fetchAllPages<T>(path: string): Promise<T[]> {
  const sep = path.includes("?") ? "&" : "?";
  const all: T[] = [];
  for (let page = 0; page < 100; page++) {
    const res = await apiFetch<ApiPage<T>>(`${path}${sep}page=${page}&size=100`);
    all.push(...res.content);
    if (res.last) break;
  }
  return all;
}

/* ----------------------------- feature helpers ----------------------------- */

export const login = (email: string, password: string, role?: string) =>
  apiFetch<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password, role }),
  });

export const register = (fullName: string, email: string, password: string) =>
  apiFetch<ApiUser>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ fullName, email, password }),
  });

export const getProductsByCategory = async (category: string, sort = "newest") =>
  (await apiFetch<ApiPage<ApiProduct>>(`/api/products?category=${encodeURIComponent(category)}&sort=${sort}&size=100`)).content;

export const getCart = () => apiFetch<ApiCart>("/api/cart");

export const addToCart = (productId: number, quantity = 1) =>
  apiFetch<ApiCart>("/api/cart/items", { method: "POST", body: JSON.stringify({ productId, quantity }) });

export const updateCartItem = (itemId: number, quantity: number) =>
  apiFetch<ApiCart>(`/api/cart/items/${itemId}`, { method: "PUT", body: JSON.stringify({ quantity }) });

export const removeCartItem = (itemId: number) => apiFetch<ApiCart>(`/api/cart/items/${itemId}`, { method: "DELETE" });

export const getWishlist = () => apiFetch<ApiProduct[]>("/api/wishlist");
export const addToWishlist = (productId: number) => apiFetch<ApiProduct[]>(`/api/wishlist/${productId}`, { method: "POST" });
export const removeFromWishlist = (productId: number) =>
  apiFetch<ApiProduct[]>(`/api/wishlist/${productId}`, { method: "DELETE" });

export const uploadImage = async (file: File): Promise<string> => {
  const form = new FormData();
  form.append("file", file);
  const res = await apiFetch<{ url: string }>("/api/admin/uploads", { method: "POST", body: form });
  return res.url;
};

export const PAYMENT_METHODS: Record<string, string> = {
  "Google Pay": "GOOGLE_PAY",
  PhonePe: "PHONEPE",
  "Cash on Delivery": "COD",
};
