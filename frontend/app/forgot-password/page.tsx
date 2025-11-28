"use client";

import { useState } from "react";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        // TODO: add backend impl
    };

    return (
        <form onSubmit={handleSubmit}>
            <div className="flex items-center justify-center my-40">
                <fieldset className="fieldset rounded-box w-xs space-y-2">
                    <legend className="fieldset-legend text-3xl mb-2">Forgot Password</legend>

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

                    <button type="submit" className="btn btn-neutral w-full mt-2">
                        Send Reset Link
                    </button>

                    <a href="/login" className="text-sm text-center block mt-2 opacity-80 hover:opacity-100">
                        Back to Login
                    </a>
                </fieldset>
            </div>
        </form>
    );
}
