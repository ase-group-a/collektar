import { fetcher } from "@/lib/fetcher";
import { getAccessToken as mockedGetAccessToken } from "@/lib/auth/AuthToken";

jest.mock("@/lib/auth/AuthToken", () => ({
    getAccessToken: jest.fn(),
    setAccessToken: jest.fn(),
}));

describe("fetcher", () => {
    const OLD_ENV = process.env;

    beforeEach(() => {
        jest.resetModules();
        jest.clearAllMocks();
        process.env = { ...OLD_ENV, NEXT_PUBLIC_API_URL: "http://test.api" };

        (mockedGetAccessToken as jest.Mock).mockReturnValue(null);

        (global as any).fetch = jest.fn();
    });

    afterEach(() => {
        process.env = OLD_ENV;
        jest.restoreAllMocks();
    });

    it("returns parsed JSON on success and sets headers (including Authorization when token present)", async () => {
        (mockedGetAccessToken as jest.Mock).mockReturnValue("my-token");

        const mockResponse = {
            ok: true,
            status: 200,
            headers: { get: jest.fn().mockReturnValue("123") }, // content-length "123"
            json: async () => ({ hello: "world" }),
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        const result = await fetcher("/test");

        expect(result).toEqual({ hello: "world" });
        expect(global.fetch).toHaveBeenCalledTimes(1);

        const [url, init] = (global.fetch as jest.Mock).mock.calls[0];
        expect(url).toBe("http://test.api/test");
        expect(init.credentials).toBe("include");

        expect(init.headers.get("Content-Type")).toBe("application/json");
        expect(init.headers.get("Authorization")).toBe("Bearer my-token");
    });

    it("normalizes path without leading slash", async () => {
        const mockResponse = {
            ok: true,
            status: 200,
            headers: { get: jest.fn().mockReturnValue("123") },
            json: async () => ({ ok: true }),
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        await fetcher("no-leading-slash");

        const [url] = (global.fetch as jest.Mock).mock.calls[0];
        expect(url).toBe("http://test.api/no-leading-slash");
    });

    it("throws error with data.message if server returns JSON error", async () => {
        const mockResponse = {
            ok: false,
            status: 400,
            json: async () => ({ message: "Invalid payload" }),
            text: async () => "ignored",
            headers: { get: jest.fn().mockReturnValue("5") },
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        await expect(fetcher("/bad")).rejects.toMatchObject({
            message: "Invalid payload",
            status: 400,
        });
    });

    it("falls back to text body if JSON parse fails", async () => {
        const mockResponse = {
            ok: false,
            status: 500,
            json: async () => { throw new Error("invalid json"); },
            text: async () => "Server crashed",
            headers: { get: jest.fn().mockReturnValue("7") },
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        await expect(fetcher("/error")).rejects.toMatchObject({
            message: "Server crashed",
            status: 500,
        });
    });

    it("falls back to default HTTP error message if no readable body", async () => {
        const mockResponse = {
            ok: false,
            status: 502,
            json: async () => { throw new Error("no json"); },
            text: async () => { throw new Error("no text"); },
            headers: { get: jest.fn().mockReturnValue(null) },
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        await expect(fetcher("/no-body")).rejects.toMatchObject({
            message: "HTTP error 502",
            status: 502,
        });
    });

    it("returns empty object for 204 No Content", async () => {
        const mockResponse = {
            ok: true,
            status: 204,
            headers: { get: jest.fn().mockReturnValue("0") },
            json: async () => { throw new Error("should not call json on 204"); },
        };
        (global.fetch as jest.Mock).mockResolvedValueOnce(mockResponse);

        const result = await fetcher("/no-content");
        expect(result).toEqual({});
    });
});
