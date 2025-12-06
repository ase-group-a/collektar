export const fetcher = async <T = any>(
    path: string,
    init?: RequestInit,
    accessToken?: string
): Promise<T> => {
    const base = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost";
    const normalized = path.startsWith("/") ? path : `/${path}`;
    const url = `${base}${normalized}`;

    const headers = new Headers(init?.headers);
    headers.set("Content-Type", "application/json");
    if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

    const res = await fetch(url, { ...init, headers, credentials: "include" });

    if (!res.ok) {
        let errorMessage = `HTTP error ${res.status}`;
        try {
            const data = await res.json();
            if (data?.message) {
                errorMessage = data.message;
            }
        } catch {
            const text = await res.text().catch(() => "");
            if (text) errorMessage = text;
        }
        const error = new Error(errorMessage) as any;
        error.status = res.status;
        throw error;
    }

    const contentLength = res.headers.get("content-length");
    if (res.status === 204 || contentLength === "0") {
        return {} as T;
    }

    return res.json();
};