const API = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8084/api/v1";
const TOKEN_KEY = "medibridge.token";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

type Envelope<T> = {
  success: boolean;
  message?: string;
  error?: string;
  data: T;
};

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API}${path}`, { ...init, headers });
  if (res.status === 204) return undefined as T;

  const json = (await res.json()) as Envelope<T>;
  if (!res.ok || json.success === false) {
    throw new ApiError(res.status, json.message || "Request failed");
  }
  return json.data;
}
