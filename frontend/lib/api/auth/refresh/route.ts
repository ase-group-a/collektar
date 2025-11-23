import { NextRequest, NextResponse } from "next/server";

const AUTH_API_BASE = process.env.NEXT_PUBLIC_API_URL!;

export async function POST(req: NextRequest) {
    const refreshCookie = req.cookies.get("refresh_token")?.value;

    if (!refreshCookie) {
        return NextResponse.json({ message: "Refresh token missing" }, { status: 401 });
    }

    const backendRes = await fetch(`${AUTH_API_BASE}/api/auth/refresh`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken: refreshCookie }),
    });

    const data = await backendRes.json();

    if (!backendRes.ok) {
        const r = NextResponse.json(data, { status: backendRes.status });
        r.cookies.delete({
            name: "refresh_token",
            path: "/api/auth/refresh",
        });
        return r;
    }

    const res = NextResponse.json({
        access_token: data.access_token,
        expires_in: data.expires_in,
    });

    if (data.refresh_token) {
        const maxAge =
            typeof data.refresh_token_expires_in === "number"
                ? Math.floor(data.refresh_token_expires_in)
                : 60 * 60 * 24 * 30; // Fallback 30 days

        res.cookies.set({
            name: "refresh_token",
            value: data.refresh_token,
            httpOnly: true,
            secure: process.env.NODE_ENV === "production",
            sameSite: "lax",
            path: "/api/auth/refresh",
            maxAge,
        });
    }

    return res;
}