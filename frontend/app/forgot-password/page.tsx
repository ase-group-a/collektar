"use client";

import { useState } from "react";
import { authApi } from "@/lib/api/authApi";
import {router} from "next/client";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage(null);
        setError(null);
        setLoading(true);

        try {
            await authApi.forgotPassword(email);
            setMessage(
                "If an account with this email exists, a password reset link has been sent."
            );

            setTimeout(() => {
                router.push("/login");
            }, 1500);
        } catch (err: any) {
            setError(
                err?.message ??
                "An error occurred while requesting the password reset link."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <div className="flex items-center justify-center my-40">
                <fieldset className="fieldset rounded-box w-xs space-y-2">
                    <legend className="fieldset-legend text-3xl mb-2">
                        Forgot Password
                    </legend>

                    <p className="text-sm opacity-70 mb-2">
                        Enter your email to receive a password reset link.
                    </p>

                    <label className="label">Email</label>
                    <input
                        className="input"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="email@example.com"
                        required
                    />

                    <button
                        type="submit"
                        className="btn btn-neutral w-full mt-2"
                        disabled={loading}
                    >
                        {loading ? "Sending..." : "Send Reset Link"}
                    </button>

                    {message && (
                        <p className="text-sm text-green-600 mt-2">{message}</p>
                    )}
                    {error && (
                        <p className="text-sm text-red-600 mt-2">{error}</p>
                    )}

                    <a
                        href="/login"
                        className="text-sm text-center block mt-2 opacity-80 hover:opacity-100"
                    >
                        Back to Login
                    </a>
                </fieldset>
            </div>
        </form>
    );
}
