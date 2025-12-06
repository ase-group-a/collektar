"use client";

import React, {useEffect, useState} from "react";
import { useAuth } from "@/lib/auth/AuthProvider";

export default function AccountPage() {
    const { user } = useAuth();

    const [username, setUsername] = useState("");
    const [displayName, setDisplayName] = useState("");
    const [email, setEmail] = useState("");

    const [newPassword, setNewPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");

    useEffect(() => {
        if (user) {
            setUsername(user.username ?? "");
            setDisplayName(user.display_name ?? "");
            setEmail(user.email ?? "");
        }
    }, [user]);

    return (
        <div className="flex items-center justify-center my-30">
            <fieldset className="fieldset rounded-box w-xs space-y-4">
                <legend className="fieldset-legend text-2xl font-bold">Account Settings</legend>

                <div>
                    <label className="label">Username</label>
                    <div className="join w-full">
                        <input
                            type="text"
                            className="input join-item w-full"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Username"
                        />
                        <button className="btn join-item">Save</button>
                    </div>
                </div>

                <div>
                    <label className="label">Display Name</label>
                    <div className="join w-full">
                        <input
                            type="text"
                            className="input join-item w-full"
                            value={displayName}
                            onChange={(e) => setDisplayName(e.target.value)}
                            placeholder="Display Name"
                        />
                        <button className="btn join-item">Save</button>
                    </div>
                </div>

                <div>
                    <label className="label">Email</label>
                    <div className="join w-full">
                        <input
                            type="email"
                            className="input join-item w-full"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="email@example.com"
                        />
                        <button className="btn join-item">Save</button>
                    </div>
                </div>

                <div className="divider">Change Password</div>

                <div>
                    <label className="label">Current Password</label>
                    <input
                        type="password"
                        className="input w-full"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        placeholder="Current password"
                    />
                </div>

                <div>
                    <label className="label">New Password</label>
                    <input
                        type="password"
                        className="input w-full"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        placeholder="New password"
                    />
                </div>

                <div>
                    <label className="label">Repeat New Password</label>
                    <input
                        type="password"
                        className="input w-full"
                        value={repeatPassword}
                        onChange={(e) => setRepeatPassword(e.target.value)}
                        placeholder="Repeat new password"
                    />
                </div>

                <button className="btn btn-neutral w-full mt-2">
                    Change Password
                </button>
            </fieldset>
        </div>
    );
}