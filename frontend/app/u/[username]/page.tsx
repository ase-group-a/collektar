"use client";

import { useAuth } from "@/lib/auth/AuthProvider";

export default function UserPage() {
    const { user, isAuthenticated } = useAuth();

    return (
        <div>
            <h1>{user?.username || "Undefined"}</h1>
        </div>
    )
}