"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import { authApi } from "@/lib/api/authApi";
import { useRouter } from "next/navigation";

export default function AccountPage() {
    const { user, logout } = useAuth();
    const router = useRouter();

    const [username, setUsername] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");

    const [deletePassword, setDeletePassword] = useState("");

    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (user) {
            setUsername(user.username ?? "");
            setDisplayName(user.display_name ?? "");
            setEmail(user.email ?? "");
        }
    }, [user]);

    const handleChangePassword = async () => {
        setMessage(null);
        setError(null);

        if (newPassword !== repeatPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await authApi.changePassword(currentPassword, newPassword);
            setMessage("Password changed successfully.");
            setCurrentPassword("");
            setNewPassword("");
            setRepeatPassword("");
        } catch (err: any) {
            setError(
                err?.message ??
                "An error occurred while changing the password."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        setMessage(null);
        setError(null);

        if (!deletePassword) {
            setError("Please enter your password to delete your account.");
            return;
        }

        setLoading(true);

        try {
            await authApi.deleteAccount(deletePassword);
            await logout();
            router.replace("/login");
        } catch (err: any) {
            setError(
                err?.message ??
                "An error occurred while deleting the account."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex items-center justify-center my-30">
            <fieldset className="fieldset rounded-box w-xs space-y-4">
                <legend className="fieldset-legend text-2xl font-bold">
                    Account Settings
                </legend>

                <div>
                    <label className="label">Username</label>
                    <div className="join w-full">
                        <input
                            type="text"
                            className="input join-item w-full"
                            value={username}
                            disabled
                        />
                    </div>
                </div>

                <div>
                    <label className="label">Display Name</label>
                    <div className="join w-full">
                        <input
                            type="text"
                            className="input join-item w-full"
                            value={displayName}
                            disabled                        />
                    </div>
                </div>

                <div>
                    <label className="label">Email</label>
                    <div className="join w-full">
                        <input
                            type="email"
                            className="input join-item w-full"
                            value={email}
                            disabled
                        />
                    </div>
                </div>

                <div className="divider">Change Password</div>

                <div>
                    <label htmlFor="currentPassword" className="label">Current Password</label>
                    <input
                        id="currentPassword"
                        type="password"
                        className="input w-full"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="newPassword" className="label">New Password</label>
                    <input
                        id="newPassword"
                        type="password"
                        className="input w-full"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                    />
                </div>

                <div>
                    <label htmlFor="repeatPassword" className="label">Repeat New Password</label>
                    <input
                        id="repeatPassword"
                        type="password"
                        className="input w-full"
                        value={repeatPassword}
                        onChange={(e) => setRepeatPassword(e.target.value)}
                    />
                </div>

                <button
                    className="btn btn-neutral w-full"
                    onClick={handleChangePassword}
                    disabled={loading}
                >
                    Change Password
                </button>

                {message && <p className="text-sm text-green-600">{message}</p>}
                {error && <p className="text-sm text-red-600">{error}</p>}

                <div className="divider text-red-600">Deleting your Account</div>

                <div>
                    <label className="label">Password</label>
                    <input
                        type="password"
                        className="input w-full"
                        value={deletePassword}
                        onChange={(e) => setDeletePassword(e.target.value)}
                        placeholder="Enter your password"
                    />
                </div>

                <button
                    className="btn btn-error w-full"
                    onClick={handleDeleteAccount}
                    disabled={loading}
                >
                    Delete Account
                </button>
            </fieldset>
        </div>
    );
}
