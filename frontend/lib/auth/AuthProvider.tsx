"use client";

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import { authApi, User } from "@/lib/api/authApi";
import { setAccessToken as setGlobalAccessToken } from "@/lib/auth/AuthToken";

type AuthContextType = {
    accessToken: string | null;
    user: User | null;
    login: (username: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
    register: (email: string, username: string, display_name: string, password: string) => Promise<void>;
    isAuthenticated: boolean;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [accessToken, setAccessTokenState] = useState<string | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;
        const initAuth = async () => {
            try {
                const data = await authApi.refresh();
                setGlobalAccessToken(data.access_token);
                if (mounted) setAccessTokenState(data.access_token);
                if (mounted) setUser(data.user);
            } catch (err: any) {
                console.warn("Auto refresh failed", err);
                setGlobalAccessToken(null);
                if (mounted) {
                    setAccessTokenState(null);
                    setUser(null);
                }
            } finally {
                if (mounted) setLoading(false);
            }
        };
        initAuth();
        return () => { mounted = false; };
    }, []);

    const login = useCallback(async (username: string, password: string) => {
        const data = await authApi.login(username, password);
        setGlobalAccessToken(data.access_token);
        setAccessTokenState(data.access_token);
        setUser(data.user);
    }, []);

    const register = useCallback(async (email: string, username: string, display_name: string, password: string) => {
        const data = await authApi.register(email, username, display_name, password);
        setGlobalAccessToken(data.access_token);
        setAccessTokenState(data.access_token);
        setUser(data.user);
    }, []);

    const logout = useCallback(async () => {
        setGlobalAccessToken(null);
        setAccessTokenState(null);
        setUser(null);
        try {
            await authApi.logout();
        } catch (err) {
            console.warn("Logout API failed", err);
        }
    }, []);

    return (
        <AuthContext.Provider
            value={{
                accessToken,
                user,
                login,
                logout,
                register,
                isAuthenticated: !!accessToken,
            }}
        >
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
};
