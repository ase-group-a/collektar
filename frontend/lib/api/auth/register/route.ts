import { NextRequest, NextResponse } from "next/server";
const AUTH_API_BASE = process.env.NEXT_PUBLIC_API_URL!;

export async function POST(req: NextRequest) {
    const body = await req.json();

    const backendRes = await fetch(`${AUTH_API_BASE}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });

    const data = await backendRes.json();

    if (!backendRes.ok) return NextResponse.json(data, { status: backendRes.status });

    const refreshToken = data.refresh_token;
    const res = NextResponse.json({
        access_token: data.access_token,
        expires_in: data.expires_in,
        user: data.user,
    });

    const maxAge = typeof data.refresh_token_expires_in === "number"
        ? Math.floor(data.refresh_token_expires_in)
        : 60 * 60 * 24 * 30;

    res.cookies.set({
        name: "refresh_token",
        value: refreshToken,
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
        path: "/api/auth/refresh",
        maxAge,
    });

    return res;
}
