import {render, screen, fireEvent, waitFor} from "@testing-library/react";
import LoginPage from "@/app/login/page";
import { useAuth } from "@/lib/auth/AuthProvider";
import { useRouter } from "next/navigation";

jest.mock("@/lib/auth/AuthProvider", () => ({
    useAuth: jest.fn(),
}));

jest.mock("next/navigation", () => ({
    useRouter: jest.fn(),
}));

describe("LoginPage", () => {
    const pushMock = jest.fn();

    beforeEach(() => {
        (useRouter as jest.Mock).mockReturnValue({ push: pushMock });
        (useAuth as jest.Mock).mockReturnValue({ login: jest.fn() });
        jest.clearAllMocks();
    });

    it("renders login form", () => {
        render(<LoginPage />);
        expect(screen.getByPlaceholderText("Username")).toBeInTheDocument();
        expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
        expect(screen.getByTestId("submit-login")).toBeInTheDocument();
    });

    it("logs in successfully with correct credentials", async () => {
        const loginMock = jest.fn().mockResolvedValue(undefined);
        (useAuth as jest.Mock).mockReturnValue({ login: loginMock });

        render(<LoginPage />);
        fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "user1" } });
        fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "password123" } });

        const submitButton = screen.getByTestId("submit-login");
        fireEvent.click(submitButton);

        expect(loginMock).toHaveBeenCalledWith("user1", "password123");

        await waitFor(() => {
            expect(pushMock).toHaveBeenCalledWith("/u/user1");
        });
    });


    it("shows error message on failed login", async () => {
        const loginMock = jest.fn().mockRejectedValue(new Error("Invalid credentials"));
        (useAuth as jest.Mock).mockReturnValue({ login: loginMock });

        render(<LoginPage />);
        fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "user1" } });
        fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "wrongpass" } });

        const submitButton = screen.getByTestId("submit-login");
        fireEvent.click(submitButton);

        expect(loginMock).toHaveBeenCalledWith("user1", "wrongpass");
        expect(await screen.findByText("Invalid credentials")).toBeInTheDocument();
    });
});
