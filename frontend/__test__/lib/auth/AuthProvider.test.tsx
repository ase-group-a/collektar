import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { AuthProvider, useAuth } from "@/lib/auth/AuthProvider";
import { authApi as mockedAuthApi } from "@/lib/api/authApi";

jest.mock("@/lib/api/authApi", () => ({
    authApi: {
        refresh: jest.fn(),
        verify: jest.fn(),
        login: jest.fn(),
        register: jest.fn(),
        logout: jest.fn(),
    },
}));

const TestConsumer: React.FC = () => {
    const { accessToken, user, login, logout, register, isAuthenticated } = useAuth();

    return (
        <div>
            <div data-testid="auth-status">{"Authenticated: " + (isAuthenticated ? "true" : "false")}</div>
            <div data-testid="access-token">{accessToken ?? "no-token"}</div>
            <div data-testid="username">{user?.username ?? "no-user"}</div>

            <button onClick={() => login("joe", "pwd")} data-testid="btn-login">login</button>
            <button onClick={() => register("e@e", "newuser", "Display", "pwd")} data-testid="btn-register">register</button>
            <button onClick={() => logout()} data-testid="btn-logout">logout</button>
        </div>
    );
};

const renderAuth = (refreshReturn?: any) => {
    if (refreshReturn !== undefined) {
        (mockedAuthApi.refresh as jest.Mock).mockResolvedValueOnce(refreshReturn);
    }
    render(
        <AuthProvider>
            <TestConsumer />
        </AuthProvider>
    );
};

const testAuthAction = async (
    buttonTestId: string,
    apiMock: jest.Mock,
    apiReturn: any,
    expectedToken: string,
    expectedUser: string,
    apiArgs?: any[]
) => {
    (mockedAuthApi.refresh as jest.Mock).mockRejectedValueOnce(new Error("no cookie"));
    apiMock.mockResolvedValueOnce(apiReturn);

    render(
        <AuthProvider>
            <TestConsumer />
        </AuthProvider>
    );

    await waitFor(() => {
        expect(screen.getByTestId("auth-status").textContent).toContain("false");
    });

    fireEvent.click(screen.getByTestId(buttonTestId));

    await waitFor(() => {
        expect(screen.getByTestId("auth-status").textContent).toContain("true");
    });

    if (apiArgs) {
        expect(apiMock).toHaveBeenCalledWith(...apiArgs);
    } else {
        expect(apiMock).toHaveBeenCalled();
    }

    expect(screen.getByTestId("access-token")).toHaveTextContent(expectedToken);
    expect(screen.getByTestId("username")).toHaveTextContent(expectedUser);
};

describe("AuthProvider", () => {
    beforeEach(() => {
        jest.resetAllMocks();
    });

    it("performs auto-refresh on mount and sets accessToken + user when refresh succeeds", async () => {
        renderAuth({
            access_token: "access-from-refresh",
            user: { username: "ref-user", email: "r@x" },
        });

        await waitFor(() => {
            expect(screen.getByTestId("auth-status").textContent).toContain("true");
        });

        expect(screen.getByTestId("access-token")).toHaveTextContent("access-from-refresh");
        expect(screen.getByTestId("username")).toHaveTextContent("ref-user");
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

        expect(screen.getByTestId("access-token")).toHaveTextContent("no-token");
        expect(screen.getByTestId("username")).toHaveTextContent("no-user");
        expect(mockedAuthApi.refresh).toHaveBeenCalledTimes(1);
    });

    it("login calls authApi.login and updates state", async () => {
        await testAuthAction(
            "btn-login",
            mockedAuthApi.login as jest.Mock,
            { access_token: "access-login", user: { username: "joe", email: "joe@x" } },
            "access-login",
            "joe",
            ["joe", "pwd"]
        );
    });

    it("register calls authApi.register and updates state", async () => {
        await testAuthAction(
            "btn-register",
            mockedAuthApi.register as jest.Mock,
            { access_token: "access-register", user: { username: "newuser", email: "new@x" } },
            "access-register",
            "newuser",
            ["e@e", "newuser", "Display", "pwd"]
        );
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
        expect(screen.getByTestId("access-token")).toHaveTextContent("no-token");
        expect(screen.getByTestId("username")).toHaveTextContent("no-user");
    });
});