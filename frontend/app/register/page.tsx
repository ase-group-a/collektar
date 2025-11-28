"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

export default function RegisterPage() {
    const router = useRouter();
    const { register } = useAuth();

    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [password, setPassword] = useState("");

    const [repeatPassword, setRepeatPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [showRepeatPassword, setShowRepeatPassword] = useState(false);

    const [err, setErr] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [loading, setLoading] = useState(false);

    const validate = (): boolean => {
        const e: Record<string, string> = {};

        const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRe.test(email)) e.email = "Please enter a valid email.";

        if (!username || username.length < 3) e.username = "Username must be at least 3 characters.";
        if (!displayName || displayName.length < 1) e.displayName = "Please enter a display name.";

        if (!password || password.length < 8) e.password = "Password must be at least 8 characters.";
        if (password !== repeatPassword) e.repeatPassword = "Passwords do not match.";

        setFieldErrors(e);
        return Object.keys(e).length === 0;
    };

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await register(email, username, displayName, password);
            router.push("/u/" + username);
        } catch (error: any) {
            setErr(error.message);
        }
    };

    return (
        <form onSubmit={handleRegister}>
            <div className="flex items-center justify-center my-30">
                <fieldset className="fieldset rounded-box w-xs space-y-2">
                    <legend className="fieldset-legend text-2xl font-bold">Create account</legend>

                    {err && (
                        <div className="alert alert-error">
                            <div>{err}</div>
                        </div>
                    )}

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

                        Register with Google
                    </button>

                    <div className="flex items-center gap-4">
                        <div className="flex-grow h-px bg-primary/30"></div>
                        <span className="text-sm text-base-content/60">OR</span>
                        <div className="flex-grow h-px bg-primary/30"></div>
                    </div>

                    <div className="form-control">
                        <label className="label">
                            <span className="label-text">Email</span>
                        </label>
                        <input
                            type="email"
                            className="input input-bordered"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="email@example.com"
                            required
                            aria-invalid={!!fieldErrors.email}
                        />
                        {fieldErrors.email && <p className="text-sm text-red-500 mt-1">{fieldErrors.email}</p>}
                    </div>

                    <div className="form-control">
                        <label className="label">
                            <span className="label-text">Username</span>
                        </label>
                        <input
                            className="input input-bordered"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Your unique username"
                            required
                            aria-invalid={!!fieldErrors.username}
                        />
                        {fieldErrors.username && <p className="text-sm text-red-500 mt-1">{fieldErrors.username}</p>}
                    </div>

                    <div className="form-control">
                        <label className="label">
                            <span className="label-text">Display name</span>
                        </label>
                        <input
                            className="input input-bordered"
                            value={displayName}
                            onChange={(e) => setDisplayName(e.target.value)}
                            placeholder="Your display name"
                            required
                            aria-invalid={!!fieldErrors.displayName}
                        />
                        {fieldErrors.displayName && (
                            <p className="text-sm text-red-500 mt-1">{fieldErrors.displayName}</p>
                        )}
                    </div>

                    <div className="form-control">
                        <label className="label">
                            <span className="label-text">Password</span>
                        </label>
                        <div className="relative">
                            <input
                                type={showPassword ? "text" : "password"}
                                className="input input-bordered pr-12"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Create a password"
                                required
                                aria-invalid={!!fieldErrors.password}
                            />
                            <button
                                type="button"
                                aria-label={showPassword ? "Hide password" : "Show password"}
                                onClick={() => setShowPassword((s) => !s)}
                                className="absolute right-2 top-1/2 -translate-y-1/2 btn btn-ghost btn-sm p-1"
                            >
                                {showPassword ? (
                                    <EyeSlashIcon className="w-5 h-5" />
                                ) : (
                                    <EyeIcon className="w-5 h-5" />
                                )}
                            </button>
                        </div>
                        {fieldErrors.password && <p className="text-sm text-red-500 mt-1">{fieldErrors.password}</p>}
                    </div>

                    <div className="form-control">
                        <label className="label">
                            <span className="label-text">Repeat password</span>
                        </label>
                        <div className="relative">
                            <input
                                type={showRepeatPassword ? "text" : "password"}
                                className="input input-bordered pr-12"
                                value={repeatPassword}
                                onChange={(e) => setRepeatPassword(e.target.value)}
                                placeholder="Repeat your password"
                                required
                                aria-invalid={!!fieldErrors.repeatPassword}
                            />
                            <button
                                type="button"
                                aria-label={showRepeatPassword ? "Hide password" : "Show password"}
                                onClick={() => setShowRepeatPassword((s) => !s)}
                                className="absolute right-2 top-1/2 -translate-y-1/2 btn btn-ghost btn-sm p-1"
                            >
                                {showRepeatPassword ? (
                                    <EyeSlashIcon className="w-5 h-5" />
                                ) : (
                                    <EyeIcon className="w-5 h-5" />
                                )}
                            </button>
                        </div>
                        {fieldErrors.repeatPassword && (
                            <p className="text-sm text-red-500 mt-1">{fieldErrors.repeatPassword}</p>
                        )}
                    </div>

                    <div>
                        <button type="submit"
                            className={`btn btn-neutral w-full ${loading ? "opacity-70 pointer-events-none" : ""}`}
                        >
                            {loading ? "Creating account..." : "Create account"}
                        </button>
                    </div>

                    <p className="text-center text-sm mt-4">
                        Already have an account?{" "}
                        <a href="/login" className="link link-primary">
                            Login
                        </a>
                    </p>
                </fieldset>
            </div>
        </form>
    );
}
