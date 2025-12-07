"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { authApi, User} from "@/lib/api/authApi";

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
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const initAuth = async () => {
            try {
            const data = await authApi.refresh();
            setAccessToken(data.access_token);
            setUser(data.user);
            } catch (err: any) {
                console.warn("Auto refresh failed", err);
                setAccessToken(null);
                setUser(null);
            } finally {
                setLoading(false);
            }
        };
        initAuth();
    }, []);

    const login = async (username: string, password: string) => {
        const data = await authApi.login(username, password);
        setAccessToken(data.access_token);
        setUser(data.user);
    };

    const register = async (email: string, username: string, display_name: string, password: string) => {
        const data = await authApi.register(email, username, display_name, password);
        setAccessToken(data.access_token);
        setUser(data.user);
    };

    const logout = async () => {
        setAccessToken(null);
        setUser(null);
        await authApi.logout();
    };

    return (
        <AuthContext.Provider value={{ accessToken, user, login, logout, register, isAuthenticated: !!accessToken }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
};
