"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";

export default function LoginPage() {
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
            router.push("/u/" + username);
        } catch (error: any) {
            setErr(error.message);
        }
    };

    return (
        <form onSubmit={handleLogin}>
            <div className="flex items-center justify-center my-40">
            <fieldset className="fieldset rounded-box w-xs space-y-2">
                <legend className="fieldset-legend text-3xl">Login</legend>

                {err && (
                    <div className="alert alert-error">
                        <div>{err}</div>
                    </div>
                )}

                <button
                    type="button"
                    className="btn btn-outline w-full flex items-center gap-2"
                >
                    <img src="/google-logo.svg" alt="Google logo" width={20} height={20} className="inline-block mr-2" />
                    Login with Google (coming soon)
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

                <button type="submit" data-testid="submit-login" className="btn btn-neutral ">Login</button>

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