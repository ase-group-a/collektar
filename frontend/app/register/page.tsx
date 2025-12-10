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
        if (!validate()) {
            return;
        }

        setLoading(true);
        setErr(null);

        try {
            await register(email, username, displayName, password);
            router.push("/u/" + username);
        } catch (error: any) {
            setErr(error?.message ?? "Registration failed");
        } finally {
            setLoading(false);
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
                        <img src="/google-logo.svg" alt="Google logo" width={20} height={20} className="inline-block mr-2" />
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
