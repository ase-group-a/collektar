"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";

export default function LoginForm() {
    const { login } = useAuth();
    const router = useRouter();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [err, setErr] = useState<string | null>(null);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setErr(null);
        try {
            await login(username, password);
            router.push("/");
        } catch (error: any) {
            setErr(error.message ?? "Login failed");
        }
    };

    return (
        <form onSubmit={handleLogin}>
            <div className="flex items-center justify-center my-40">
            <fieldset className="fieldset rounded-box w-xs space-y-2">
                <legend className="fieldset-legend text-3xl">Login</legend>

                <button
                    type="button"
                    className="btn btn-outline w-full flex items-center gap-2"
                    // TODO onClick={handleGoogleLogin}
                >
                    <svg width="20" height="20" viewBox="0 0 533.5 544.3">
                        <path fill="#4285F4" d="M533.5 278.4c0-17.4-1.6-34.1-4.7-50.3H272.1v95.2h147.7c-6.4 34.7-25.9 64-55.1 83.4v68h88.8c52.1-48 82-118.8 82-196.3z"/>
                        <path fill="#34A853" d="M272.1 544.3c74.4 0 136.9-24.5 182.5-66.7l-88.8-68c-24.7 16.6-56.4 26.3-93.7 26.3-72.1 0-133.1-48.7-155-114.1h-90.3v71.5c45.3 89.8 138.5 150.9 245.3 150.9z"/>
                        <path fill="#FBBC04" d="M117.1 321.8a162 162 0 010-103.6V146.7h-90.3a272.2 272.2 0 000 250.9l90.3-75.8z"/>
                        <path fill="#EA4335" d="M272.1 107.7c40.5 0 76.8 13.9 105.5 41.1l78.6-78.6C409 25.4 346.5 0 272.1 0 165.3 0 72.1 61.1 26.8 150.9l90.3 71.5c21.9-65.3 82.9-114.7 155-114.7z"/>
                    </svg>

                    Login with Google
                </button>

                <div className="flex items-center gap-4">
                    <div className="flex-grow h-px bg-primary/30"></div>
                    <span className="text-sm text-base-content/60">OR</span>
                    <div className="flex-grow h-px bg-primary/30"></div>
                </div>


                <div className="form-control">
                <label className="label">Username</label>
                <input className="input" value={username} onChange={e => setUsername(e.target.value)} placeholder="Username" />
                </div>

                <div className="form-control">
                <label className="label">Password</label>
                <input type="password" className="input" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" />
                    <label className="label">
                        <a className="label-text-alt link link-hover" href="/forgot-password">
                            Forgot password?
                        </a>
                    </label>
                </div>

                <button type="submit" className="btn btn-neutral ">Login</button>

                {/* Sign up link */}
                <p className="text-center text-sm mt-4">
                    Don’t have an account?{" "}
                    <a href="/register" className="link link-primary">
                        Sign Up
                    </a>
                </p>
            </fieldset>
            </div>
        </form>
    );
}