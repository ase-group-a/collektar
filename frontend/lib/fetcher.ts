export const fetcher = async <T = any>(path: string, init?: RequestInit): Promise<T> => {
    const base = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
    const normalized = path.startsWith("/") ? path : `/${path}`;
    const url = `${base}${normalized}`;

    const res = await fetch(url, {
        headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
        ...init,
    });

    if (!res.ok) {
        const error = new Error("An error occurred while fetching the data") as any;
        try {
            error.info = await res.json();
        } catch {
            error.info = await res.text().catch(() => null);
        }
        error.status = res.status;
        throw error;
    }

    return res.json();
};
