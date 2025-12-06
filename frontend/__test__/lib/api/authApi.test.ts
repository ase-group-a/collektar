import { authApi, AuthResponse, User } from "@/lib/api/authApi";
import { fetcher } from "@/lib/fetcher";

jest.mock("@/lib/fetcher");

const mockedFetcher = fetcher as jest.MockedFunction<typeof fetcher>;

describe("authApi", () => {
    const OLD_ENV = process.env;

    beforeEach(() => {
        jest.resetModules();
        process.env = { ...OLD_ENV, NEXT_PUBLIC_API_URL: "http://test.api" };
        mockedFetcher.mockReset();
    });

    afterEach(() => {
        process.env = OLD_ENV;
        mockedFetcher.mockReset();
    });

    it("login calls fetcher with correct path and body and returns data", async () => {
        const user: User = { userId: "1", email: "a@b", username: "u" };
        const response: AuthResponse = { access_token: "t", user };
        mockedFetcher.mockResolvedValueOnce(response);

        const result = await authApi.login("u", "p");

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ username: "u", password: "p" }),
        });
        expect(result).toBe(response);
    });

    it("register calls fetcher with correct path and body and returns data", async () => {
        const user: User = { userId: "2", email: "x@y", username: "reg" };
        const response: AuthResponse = { access_token: "token-reg", user };
        mockedFetcher.mockResolvedValueOnce(response);

        const result = await authApi.register("x@y", "reg", "display", "pwd");

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ email: "x@y", username: "reg", display_name: "display", password: "pwd" }),
        });
        expect(result).toBe(response);
    });

    it("logout calls fetcher with correct path and method", async () => {
        mockedFetcher.mockResolvedValueOnce(undefined);

        const result = await authApi.logout();

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith("/api/auth/logout", { method: "POST" });
        expect(result).toBeUndefined();
    });

    it("refresh calls fetcher with correct path and method and returns data", async () => {
        const user: User = { userId: "3", email: "r@r", username: "ref" };
        const response: AuthResponse = { access_token: "token-refresh", user };
        mockedFetcher.mockResolvedValueOnce(response);

        const result = await authApi.refresh();

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith("/api/auth/refresh", { method: "POST" });
        expect(result).toBe(response);
    });

    it("verify calls fetcher with correct path, method and authorization header", async () => {
        mockedFetcher.mockResolvedValueOnce(undefined);
        const token = "access-token-xyz";

        const result = await authApi.verify(token);

        expect(mockedFetcher).toHaveBeenCalledTimes(1);
        expect(mockedFetcher).toHaveBeenCalledWith("/api/auth/verify", {
            method: "GET",
            headers: { Authorization: `Bearer ${token}` },
        });
        expect(result).toBeUndefined();
    });
});
