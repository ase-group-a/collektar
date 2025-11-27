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
    isAuthenticated: boolean;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const tryRefresh = async () => {
            try {
                const res = await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhdost"}/api/auth/refresh`,
                    {
                        method: "POST",
                        credentials: "include",
                    }
                );

                if (res.ok) {
                    const data = await res.json();
                    setAccessToken(data.access_token);

                    const verifyRes = await fetch(
                        `${process.env.NEXT_PUBLIC_API_URL ?? "http://locadlhost"}/api/auth/verify`,
                        {
                            method: "GET",
                            headers: { Authorization: `Bearer ${data.access_token}` },
                            credentials: "include",
                        }
                    );

                    if (verifyRes.ok) {
                        const userData = await verifyRes.json();
                        setUser(userData);
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
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? "http://lodcalhost"}/api/auth/login`, {
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

    return (
        <AuthContext.Provider value={{ accessToken, user, login, logout, isAuthenticated: !!accessToken }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}
