"use client";

import React, { createContext, useContext, useState, useEffect } from "react";

type User = {
    userId?: string;
    email?: string;
    username?: string;
    display_name?: string;
};

type AuthContextType = {
    accessToken: string | null;
    user: User | null;
    login: (username: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    register: (email: string, username: string, displayName: string, password: string) => Promise<void>;
    isAuthenticated: boolean;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const API = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost";

        const tryRefresh = async () => {
            try {
                const res = await fetch(`${API}/api/auth/refresh`, {
                    method: "POST",
                    credentials: "include",
                });

                if (!res.ok) {
                    return;
                }

                const data: any = await res.json().catch(() => ({}));

                const accessToken = data?.access_token ?? data?.accessToken ?? null;
                if (!accessToken) {
                    return;
                }

                setAccessToken(accessToken);

                if (data?.user) {
                    setUser(data.user);
                    return;
                }

                const verifyRes = await fetch(`${API}/api/auth/verify`, {
                    method: "GET",
                    headers: { Authorization: `Bearer ${accessToken}` },
                    credentials: "include",
                });

                if (verifyRes.ok) {
                    // try to read user JSON
                    const userData = await verifyRes.json().catch(() => null);
                    if (userData) {
                        setUser(userData);
                        return;
                    }

                    try {
                        const payload = JSON.parse(atob(accessToken.split(".")[1]));
                        const inferredUser = {
                            username: payload.username ?? payload.userName ?? payload.sub ?? payload.email,
                            email: payload.email,
                            userId: payload.userId ?? payload.sub,
                            display_name: payload.displayName ?? payload.display_name,
                        } as any;
                        setUser(inferredUser);
                    } catch (e) {

                    }
                }
            } catch (e) {
                console.warn("Auto refresh failed", e);
            } finally {
                setLoading(false);
            }
        };

        tryRefresh();
    }, []);

    const login = async (username: string, password: string) => {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost"}/api/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, password }),
        });

        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error(data?.error ?? "Login failed");
        }

        const data = await res.json();
        setAccessToken(data.access_token);
        setUser(data.user);
    };

    const logout = async () => {
        await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost"}/api/auth/logout`, {
            method: "POST",
            credentials: "include"
        });

        setAccessToken(null);
        setUser(null);
    };

    const register = async (email: string, username: string, display_name: string, password: string) => {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost"}/api/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ email, username, display_name, password }),
        });

        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error(data?.message ?? "Registration failed");
        }

        const data = await res.json();
        setAccessToken(data.access_token);
        setUser(data.user);
    };


    return (
        <AuthContext.Provider value={{ accessToken, user, login, logout, register, isAuthenticated: !!accessToken }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}
