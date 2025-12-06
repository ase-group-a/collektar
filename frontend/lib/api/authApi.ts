import { fetcher } from "../fetcher";

export type User = {
    userId?: string;
    email?: string;
    username?: string;
    display_name?: string;
};

export type AuthResponse = {
    access_token: string;
    user: User;
};

export const authApi = {
    login: (username: string, password: string) =>
        fetcher<AuthResponse>("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ username, password }),
        }),

    register: (email: string, username: string, display_name: string, password: string) =>
        fetcher<AuthResponse>("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ email, username, display_name, password }),
        }),

    logout: () =>
        fetcher<void>("/api/auth/logout", {
            method: "POST",
        }),

    refresh: () =>
        fetcher<AuthResponse>("/api/auth/refresh", {
            method: "POST",
        }),

    verify: (accessToken: string) =>
        fetcher<void>("/api/auth/verify", {
            method: "GET",
            headers: { Authorization: `Bearer ${accessToken}` },
        }),
};
