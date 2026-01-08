"use client";

import { useState } from "react";
import { useSearchParams } from "next/navigation";
import { authApi } from "@/lib/api/authApi";
import { useRouter } from "next/navigation";
import {router} from "next/client";

export default function ResetPasswordPage() {
    const searchParams = useSearchParams();
    const token = searchParams.get("token") ?? "";

    const [newPassword, setNewPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage(null);
        setError(null);

        if (!token) {
            setError("Invalid or missing reset token.");
            return;
        }

        if (newPassword !== repeatPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await authApi.resetPassword(token, newPassword);
            setMessage("Your password has been reset successfully.");

            setTimeout(() => {
                router.push("/login");
            }, 1500);
        } catch (err: any) {
            setError(
                err?.message ??
                "An error occurred while resetting your password."
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
                        Reset Password
                    </legend>

                    <p className="text-sm opacity-70 mb-2">
                        Enter your new password below.
                    </p>

                    <label htmlFor="newPassword" className="label">New Password</label>
                    <input
                        id="newPassword"
                        className="input"
                        type="password"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        required
                    />

                    <label htmlFor="repeatPassword" className="label">Repeat Password</label>
                    <input
                        id="repeatPassword"
                        className="input"
                        type="password"
                        value={repeatPassword}
                        onChange={(e) => setRepeatPassword(e.target.value)}
                        required
                    />

                    <button
                        type="submit"
                        className="btn btn-neutral w-full mt-2"
                        disabled={loading}
                    >
                        {loading ? "Resetting..." : "Reset Password"}
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
