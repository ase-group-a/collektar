import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { AuthProvider, useAuth } from "@/lib/auth/AuthProvider";
import { authApi as mockedAuthApi } from "@/lib/api/authApi";

jest.mock("@/lib/api/authApi", () => {
    return {
        authApi: {
            refresh: jest.fn(),
            verify: jest.fn(),
            login: jest.fn(),
            register: jest.fn(),
            logout: jest.fn(),
        },
    };
});

const TestConsumer: React.FC = () => {
    const { accessToken, user, login, logout, register, isAuthenticated } = useAuth();

    return (
        <div>
            <div data-testid="auth-status">{"Authenticated: " + (isAuthenticated ? "true" : "false")}</div>
            <div data-testid="access-token">{accessToken ?? "no-token"}</div>
            <div data-testid="username">{user?.username ?? "no-user"}</div>

            <button
                onClick={() => login("joe", "pwd")}
                data-testid="btn-login"
            >
                login
            </button>

            <button
                onClick={() => register("e@e", "newuser", "Display", "pwd")}
                data-testid="btn-register"
            >
                register
            </button>

            <button
                onClick={() => logout()}
                data-testid="btn-logout"
            >
                logout
            </button>
        </div>
    );
};

describe("AuthProvider", () => {
    beforeEach(() => {
        jest.resetAllMocks();
    });

    it("performs auto-refresh on mount and sets accessToken + user when refresh succeeds", async () => {
        (mockedAuthApi.refresh as jest.Mock).mockResolvedValueOnce({
            access_token: "access-from-refresh",
            user: { username: "ref-user", email: "r@x" },
        });

        render(
            <AuthProvider>
                <TestConsumer />
            </AuthProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("true");
        });

        expect(screen.getByTestId("access-token").textContent).toBe("access-from-refresh");
        expect(screen.getByTestId("username").textContent).toBe("ref-user");
        expect(mockedAuthApi.refresh).toHaveBeenCalledTimes(1);
    });

    it("if auto-refresh fails, remains unauthenticated", async () => {
        (mockedAuthApi.refresh as jest.Mock).mockRejectedValueOnce(new Error("no cookie"));

        render(
            <AuthProvider>
                <TestConsumer />
            </AuthProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("false");
        });

        expect(screen.getByTestId("access-token").textContent).toBe("no-token");
        expect(screen.getByTestId("username").textContent).toBe("no-user");
        expect(mockedAuthApi.refresh).toHaveBeenCalledTimes(1);
    });

    it("login calls authApi.login and updates state", async () => {
        (mockedAuthApi.login as jest.Mock).mockResolvedValueOnce({
            access_token: "access-login",
            user: { username: "joe", email: "joe@x" },
        });

        (mockedAuthApi.refresh as jest.Mock).mockRejectedValueOnce(new Error("no cookie"));

        render(
            <AuthProvider>
                <TestConsumer />
            </AuthProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("false");
        });

        fireEvent.click(screen.getByTestId("btn-login"));

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("true");
        });

        expect(mockedAuthApi.login).toHaveBeenCalledWith("joe", "pwd");
        expect(screen.getByTestId("access-token").textContent).toBe("access-login");
        expect(screen.getByTestId("username").textContent).toBe("joe");
    });

    it("register calls authApi.register and updates state", async () => {
        (mockedAuthApi.register as jest.Mock).mockResolvedValueOnce({
            access_token: "access-register",
            user: { username: "newuser", email: "new@x" },
        });
        (mockedAuthApi.refresh as jest.Mock).mockRejectedValueOnce(new Error("no cookie"));

        render(
            <AuthProvider>
                <TestConsumer />
            </AuthProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("false");
        });

        fireEvent.click(screen.getByTestId("btn-register"));

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("true");
        });

        expect(mockedAuthApi.register).toHaveBeenCalledWith("e@e", "newuser", "Display", "pwd");
        expect(screen.getByTestId("access-token").textContent).toBe("access-register");
        expect(screen.getByTestId("username").textContent).toBe("newuser");
    });

    it("logout clears the state and calls authApi.logout", async () => {
        (mockedAuthApi.refresh as jest.Mock).mockResolvedValueOnce({
            access_token: "access-initial",
            user: { username: "logged", email: "l@x" },
        });
        (mockedAuthApi.logout as jest.Mock).mockResolvedValueOnce(undefined);

        render(
            <AuthProvider>
                <TestConsumer />
            </AuthProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("true");
        });

        fireEvent.click(screen.getByTestId("btn-logout"));

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("false");
        });

        expect(mockedAuthApi.logout).toHaveBeenCalledTimes(1);
        expect(screen.getByTestId("access-token").textContent).toBe("no-token");
        expect(screen.getByTestId("username").textContent).toBe("no-user");
    });
});
